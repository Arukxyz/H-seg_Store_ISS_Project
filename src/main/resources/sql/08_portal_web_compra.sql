-- =====================================================================
-- SEGITD-HÖSÉG · Portal web: integridad, cuenta del cliente y envío
-- Lo que necesitan los bloques de compra y cuenta del portal web:
--   1. Integridad que el modelo ya supone y la base aún no forzaba.
--   2. Cliente: aceptación de términos y bloqueo por intentos fallidos.
--   3. Productos destacados en la Home (los elige el panel web).
--   4. Envío del pedido: recojo en tienda o delivery, con seguimiento.
--   5. Tokens para recuperar la contraseña del cliente.
--   6. Reseñas de productos, solo de compras pagadas.
--
-- No hay tabla de pagos: el portal no cobra en línea y venta.estado
-- (PENDIENTE → PAGADO, lo confirma el encargado) sigue siendo el pago.
--
-- Aditivo: no renombra ni cambia columnas que lea el escritorio.
-- Idempotente y en una transacción: si algo falla, no queda nada a medias.
-- Ejecutar ANTES de desplegar el código del escritorio y de la web que
-- usa la tabla envio (ddl-auto=validate falla si falta una columna).
-- =====================================================================

BEGIN;

-- ---------- 1. Integridad ---------------------------------------------

-- 1.1 Una línea de venta genera como máximo una donación (1 : 0..1).
-- El escritorio ya lo respeta al confirmar (solo crea la que falta);
-- ahora lo garantiza la base. El índice único reemplaza al de 06.
CREATE UNIQUE INDEX IF NOT EXISTS uq_donacion_detalle ON donacion (id_detalle_venta);
DROP INDEX IF EXISTS idx_donacion_detalle;

-- 1.2 Un producto aparece una sola vez por venta: el carrito suma la
-- cantidad en la misma línea en vez de repetirla.
CREATE UNIQUE INDEX IF NOT EXISTS uq_detalle_venta_linea ON detalle_venta (id_venta, codigo_producto);

-- 1.3 Importes y stocks no negativos.
ALTER TABLE venta DROP CONSTRAINT IF EXISTS venta_total_check;
ALTER TABLE venta ADD CONSTRAINT venta_total_check CHECK (total >= 0);

ALTER TABLE producto DROP CONSTRAINT IF EXISTS producto_stock_minimo_check;
ALTER TABLE producto ADD CONSTRAINT producto_stock_minimo_check CHECK (stock_minimo >= 0);

-- 1.4 Comunidades y ONG no se repiten: el portal las lista por nombre.
ALTER TABLE comunidad DROP CONSTRAINT IF EXISTS comunidad_nombre_key;
ALTER TABLE comunidad ADD CONSTRAINT comunidad_nombre_key UNIQUE (nombre);

ALTER TABLE ong DROP CONSTRAINT IF EXISTS ong_nombre_key;
ALTER TABLE ong ADD CONSTRAINT ong_nombre_key UNIQUE (nombre);

-- 1.5 Formato mínimo de email y teléfono del cliente. La validación
-- fina (móvil peruano, etc.) la hace el formulario; esto frena datos
-- que ninguna pantalla debería dejar pasar.
ALTER TABLE cliente DROP CONSTRAINT IF EXISTS cliente_email_check;
ALTER TABLE cliente ADD CONSTRAINT cliente_email_check
    CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$');

ALTER TABLE cliente DROP CONSTRAINT IF EXISTS cliente_telefono_check;
ALTER TABLE cliente ADD CONSTRAINT cliente_telefono_check
    CHECK (telefono IS NULL OR telefono ~ '^\+?[0-9 ]{9,16}$');


-- ---------- 2. Cliente: términos y bloqueo ----------------------------
-- terminos_aceptados_en: cuándo aceptó términos y privacidad al
-- registrarse (NULL en los compradores históricos de la semilla).
-- intentos_fallidos / bloqueado_hasta: el mismo bloqueo por fuerza bruta
-- que el escritorio aplica a usuario.
ALTER TABLE cliente
    ADD COLUMN IF NOT EXISTS terminos_aceptados_en TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS intentos_fallidos     INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS bloqueado_hasta       TIMESTAMPTZ;

ALTER TABLE cliente DROP CONSTRAINT IF EXISTS cliente_intentos_fallidos_check;
ALTER TABLE cliente ADD CONSTRAINT cliente_intentos_fallidos_check CHECK (intentos_fallidos >= 0);


-- ---------- 3. Destacados de la Home -----------------------------------
-- Lo marca el panel web (CU-17); el máximo de 8 lo controla el servicio.
-- Mientras no haya ninguno marcado, la Home elige sola.
ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS destacado BOOLEAN NOT NULL DEFAULT FALSE;


-- ---------- 4. Envío del pedido ----------------------------------------
-- Una fila por venta web, creada junto con la venta. Los datos de entrega
-- son una FOTO tomada al comprar: el cliente puede cambiar su dirección
-- después sin alterar pedidos ya hechos.
-- El estado del envío vive aquí; venta.estado no cambia de significado.
--   PENDIENTE   → venta sin pagar
--   PREPARANDO  → el escritorio confirmó el pago
--   EN_CAMINO   → salió con el courier (solo DELIVERY)
--   ENTREGADO   → recibido por el cliente o recogido en tienda
--   CANCELADO   → la venta se anuló
-- costo es aparte: venta.total sigue siendo solo mercadería, para no
-- inflar el monto vendido de los reportes. Monto a pagar = total + costo.
-- Las ventas anteriores a este script no tienen envío.
CREATE TABLE IF NOT EXISTS envio (
    id                 SERIAL PRIMARY KEY,
    id_venta           INTEGER       NOT NULL UNIQUE REFERENCES venta (id),
    tipo               VARCHAR(15)   NOT NULL CHECK (tipo IN ('RECOJO_TIENDA', 'DELIVERY')),
    estado             VARCHAR(12)   NOT NULL DEFAULT 'PENDIENTE'
                       CHECK (estado IN ('PENDIENTE', 'PREPARANDO', 'EN_CAMINO', 'ENTREGADO', 'CANCELADO')),
    nombre_receptor    VARCHAR(150)  NOT NULL,
    telefono_receptor  VARCHAR(20),
    direccion          VARCHAR(255),
    referencia         VARCHAR(255),
    distrito           VARCHAR(80),
    provincia          VARCHAR(80),
    departamento       VARCHAR(80),
    costo              NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (costo >= 0),
    courier            VARCHAR(80),
    codigo_seguimiento VARCHAR(60),
    fecha_despacho     TIMESTAMPTZ,
    fecha_entrega      TIMESTAMPTZ,
    creado_en          TIMESTAMPTZ   NOT NULL DEFAULT now(),

    -- Delivery necesita a dónde ir; el recojo no se cobra ni sale a ruta.
    CONSTRAINT envio_delivery_check CHECK (
        tipo = 'RECOJO_TIENDA' OR (direccion IS NOT NULL AND distrito IS NOT NULL)
    ),
    CONSTRAINT envio_recojo_check CHECK (
        tipo = 'DELIVERY' OR (costo = 0 AND estado <> 'EN_CAMINO')
    ),
    -- Mismas reglas de fechas que lote_donacion.
    CONSTRAINT envio_fechas_segun_estado_check CHECK (
        (estado <> 'EN_CAMINO' OR fecha_despacho IS NOT NULL) AND
        (estado <> 'ENTREGADO' OR fecha_entrega IS NOT NULL)
    ),
    CONSTRAINT envio_orden_fechas_check CHECK (fecha_despacho <= fecha_entrega)
);

CREATE INDEX IF NOT EXISTS idx_envio_estado ON envio (estado);


-- ---------- 5. Recuperación de contraseña ------------------------------
-- Enlace de un solo uso que la web envía por correo (CU-12). Se guarda
-- el SHA-256 del token (64 caracteres hex), nunca el token: quien lea la
-- tabla no puede usar los enlaces.
CREATE TABLE IF NOT EXISTS token_recuperacion (
    id          SERIAL PRIMARY KEY,
    id_cliente  INTEGER     NOT NULL REFERENCES cliente (id),
    token_hash  VARCHAR(64) NOT NULL UNIQUE CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    expira_en   TIMESTAMPTZ NOT NULL,
    usado_en    TIMESTAMPTZ,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT token_recuperacion_vigencia_check CHECK (expira_en > creado_en)
);

CREATE INDEX IF NOT EXISTS idx_token_cliente ON token_recuperacion (id_cliente);


-- ---------- 6. Reseñas de productos ------------------------------------
-- Cada reseña nace de una línea de venta: así solo opina quien compró
-- ("compra verificada"), una vez por compra, y el producto y el cliente
-- salen de detalle_venta → venta sin repetirse aquí.
-- El detalle de producto agrupa las reseñas de todas las tallas del
-- mismo modelo (mismo producto.nombre).
-- Reglas que aplica el servicio web, no la base:
--   · solo se reseña una línea de una venta PAGADO;
--   · se muestran solo las PUBLICADA de ventas que siguen PAGADO (si el
--     escritorio anula el pedido, la reseña deja de verse sola);
--   · un cliente reseña cada modelo una sola vez aunque lo compre de nuevo.
-- Se publica al instante; el administrador web puede ocultarla.
CREATE TABLE IF NOT EXISTS resena (
    id                SERIAL PRIMARY KEY,
    id_detalle_venta  INTEGER      NOT NULL UNIQUE REFERENCES detalle_venta (id),
    calificacion      SMALLINT     NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
    titulo            VARCHAR(100),
    comentario        VARCHAR(1000) NOT NULL CHECK (btrim(comentario) <> ''),
    estado            VARCHAR(10)  NOT NULL DEFAULT 'PUBLICADA' CHECK (estado IN ('PUBLICADA', 'OCULTA')),
    creado_en         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en    TIMESTAMPTZ,
    oculta_en         TIMESTAMPTZ,
    id_usuario_modera INTEGER REFERENCES usuario (id),
    CONSTRAINT resena_moderacion_check CHECK ((estado = 'OCULTA') = (oculta_en IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS idx_resena_estado_fecha ON resena (estado, creado_en);
CREATE INDEX IF NOT EXISTS idx_resena_usuario      ON resena (id_usuario_modera);


-- ---------- 7. Permisos -------------------------------------------------
-- Igual que 07 (6.6): solo postgres usa estas tablas; la API pública de
-- Supabase (anon, authenticated) no ve nada.
ALTER TABLE envio              ENABLE ROW LEVEL SECURITY;
ALTER TABLE token_recuperacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE resena             ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON envio, token_recuperacion, resena FROM anon, authenticated;
REVOKE ALL ON SEQUENCE envio_id_seq, token_recuperacion_id_seq, resena_id_seq FROM anon, authenticated;

COMMIT;


-- ---------- Verificación (solo lectura) --------------------------------
-- Deben devolver 0 filas.
SELECT id_detalle_venta, COUNT(*) FROM donacion GROUP BY 1 HAVING COUNT(*) > 1;
SELECT id_venta, codigo_producto, COUNT(*) FROM detalle_venta GROUP BY 1, 2 HAVING COUNT(*) > 1;

-- Columnas nuevas: una fila por cada una (4).
SELECT table_name, column_name, data_type
  FROM information_schema.columns
 WHERE (table_name = 'cliente'  AND column_name IN ('terminos_aceptados_en', 'intentos_fallidos', 'bloqueado_hasta'))
    OR (table_name = 'producto' AND column_name = 'destacado')
 ORDER BY table_name, column_name;

-- Tablas nuevas con RLS activo: 3 filas con relrowsecurity = true.
SELECT relname, relrowsecurity
  FROM pg_class
 WHERE relname IN ('envio', 'token_recuperacion', 'resena')
 ORDER BY relname;
