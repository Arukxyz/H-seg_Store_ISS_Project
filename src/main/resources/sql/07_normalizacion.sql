-- =====================================================================
-- SEGITD-HÖSÉG · Normalización e integridad del esquema compartido
-- Corrige lo que encontró la revisión del esquema (escritorio y web):
--   1. Donación de una venta aún no pagada: nuevo estado EN_ESPERA.
--   2. Categoría y colección pasan de texto libre a tablas de referencia.
--   3. aplica_triple_impacto deja de ser un dato aparte: se deriva de
--      tipo_compromiso (columna generada).
--   4. movimiento_inventario.referencia (texto 'VENTA:12') se reemplaza
--      por claves foráneas reales.
--   5. lote_donacion: fecha_despacho pasa a ser la salida (EN_RUTA) y se
--      agrega fecha_entrega (ENTREGADO).
--   6. Acabado: nombre/apellido de clientes, validaciones de documento y
--      RUC, coherencia de importes, índices de claves foráneas y permisos
--      de la API pública de Supabase.
--
-- Requiere el código del escritorio y de la web adaptado a estos cambios
-- (misma versión que este archivo): ejecutar ambos juntos.
-- Idempotente: las migraciones de datos solo se aplican la primera vez
-- (cada bloque comprueba el estado del esquema antes de actuar).
-- Todo corre en una transacción: si algo falla, no queda nada a medias.
-- =====================================================================

BEGIN;

-- ---------- 1. Donación en espera de pago ----------------------------
-- La web registra la donación al confirmar el carrito, cuando la venta
-- aún está PENDIENTE de pago. Hasta ahora quedaba PENDIENTE, el mismo
-- estado de una donación ya comprometida, y el escritorio podía meterla
-- en un lote de un pedido que nunca se pague. Ciclo de vida nuevo:
--   EN_ESPERA (venta sin pagar) → PENDIENTE (pago confirmado, lista para
--   lote) → ASIGNADA → ENTREGADA
ALTER TABLE donacion DROP CONSTRAINT IF EXISTS donacion_estado_check;
ALTER TABLE donacion ADD CONSTRAINT donacion_estado_check
    CHECK (estado IN ('EN_ESPERA', 'PENDIENTE', 'ASIGNADA', 'ENTREGADA'));
ALTER TABLE donacion ALTER COLUMN estado SET DEFAULT 'EN_ESPERA';

UPDATE donacion d
   SET estado = 'EN_ESPERA'
  FROM detalle_venta dv
  JOIN venta v ON v.id = dv.id_venta
 WHERE dv.id = d.id_detalle_venta
   AND d.estado = 'PENDIENTE'
   AND v.estado <> 'PAGADO';

-- Solo las donaciones asignadas o entregadas tienen lote.
ALTER TABLE donacion DROP CONSTRAINT IF EXISTS donacion_lote_segun_estado_check;
ALTER TABLE donacion ADD CONSTRAINT donacion_lote_segun_estado_check
    CHECK ((estado IN ('EN_ESPERA', 'PENDIENTE')) = (id_lote IS NULL));


-- ---------- 2. Categoría y colección como tablas de referencia -------
-- Eran texto libre y ya había 'Accesorio' junto a 'Accesorios'. La clave
-- es el propio nombre: producto.categoria sigue siendo texto, pero solo
-- admite valores registrados, y renombrar una categoría se propaga.
CREATE TABLE IF NOT EXISTS categoria (
    nombre VARCHAR(60) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS coleccion (
    nombre VARCHAR(80) PRIMARY KEY
);

UPDATE producto SET categoria = 'Accesorios' WHERE categoria = 'Accesorio';

INSERT INTO categoria (nombre) VALUES ('Casacas'), ('Accesorios'), ('Café')
ON CONFLICT DO NOTHING;
INSERT INTO categoria (nombre) SELECT DISTINCT categoria FROM producto
ON CONFLICT DO NOTHING;

INSERT INTO coleccion (nombre) VALUES ('Älpafill'), ('Comunidad'), ('Urbana')
ON CONFLICT DO NOTHING;
INSERT INTO coleccion (nombre) SELECT DISTINCT coleccion FROM producto WHERE coleccion IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE producto DROP CONSTRAINT IF EXISTS producto_categoria_fkey;
ALTER TABLE producto ADD CONSTRAINT producto_categoria_fkey
    FOREIGN KEY (categoria) REFERENCES categoria (nombre) ON UPDATE CASCADE;

ALTER TABLE producto DROP CONSTRAINT IF EXISTS producto_coleccion_fkey;
ALTER TABLE producto ADD CONSTRAINT producto_coleccion_fkey
    FOREIGN KEY (coleccion) REFERENCES coleccion (nombre) ON UPDATE CASCADE;

ALTER TABLE categoria ENABLE ROW LEVEL SECURITY;
ALTER TABLE coleccion ENABLE ROW LEVEL SECURITY;


-- ---------- 3. aplica_triple_impacto derivado de tipo_compromiso -----
-- Era una bandera independiente que decía lo mismo que
-- "tipo_compromiso IS NOT NULL" y se podía contradecir (la Botella quedó
-- con TRUE y sin tipo). Como columna generada la calcula la base: se
-- sigue pudiendo leer, pero ya no se escribe.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_attribute
                WHERE attrelid = 'producto'::regclass
                  AND attname = 'aplica_triple_impacto'
                  AND attgenerated = '') THEN
        ALTER TABLE producto DROP COLUMN aplica_triple_impacto;
        ALTER TABLE producto ADD COLUMN aplica_triple_impacto BOOLEAN
            GENERATED ALWAYS AS (tipo_compromiso IS NOT NULL) STORED;
    END IF;
END $$;


-- ---------- 4. Bitácora de inventario con claves foráneas ------------
-- 'VENTA:12', 'ANULACION:12', 'LOTE:HSG-L004' y 'PEDIDO_PROV:3' eran
-- claves foráneas escritas como texto que la base no podía verificar.
-- Cada movimiento apunta ahora como máximo a un origen; el tipo de
-- operación queda descrito en motivo.
ALTER TABLE movimiento_inventario
    ADD COLUMN IF NOT EXISTS id_venta            INTEGER REFERENCES venta (id),
    ADD COLUMN IF NOT EXISTS id_lote             INTEGER REFERENCES lote_donacion (id),
    ADD COLUMN IF NOT EXISTS id_pedido_proveedor INTEGER REFERENCES pedido_proveedor (id);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_name = 'movimiento_inventario' AND column_name = 'referencia') THEN

        UPDATE movimiento_inventario
           SET id_venta = split_part(referencia, ':', 2)::integer,
               motivo   = COALESCE(motivo, CASE split_part(referencia, ':', 1)
                                               WHEN 'VENTA' THEN 'Venta web confirmada'
                                               ELSE 'Anulación de venta web' END)
         WHERE split_part(referencia, ':', 1) IN ('VENTA', 'ANULACION');

        UPDATE movimiento_inventario m
           SET id_lote = l.id,
               motivo  = COALESCE(m.motivo, 'Entrega de lote de donación')
          FROM lote_donacion l
         WHERE split_part(m.referencia, ':', 1) = 'LOTE'
           AND l.codigo_lote = split_part(m.referencia, ':', 2);

        UPDATE movimiento_inventario
           SET id_pedido_proveedor = split_part(referencia, ':', 2)::integer,
               motivo = COALESCE(motivo, 'Recepción de pedido a proveedor')
         WHERE split_part(referencia, ':', 1) = 'PEDIDO_PROV';

        -- Cualquier otra referencia se conserva como texto en motivo.
        UPDATE movimiento_inventario
           SET motivo = COALESCE(motivo, referencia)
         WHERE referencia IS NOT NULL
           AND id_venta IS NULL AND id_lote IS NULL AND id_pedido_proveedor IS NULL;

        ALTER TABLE movimiento_inventario DROP COLUMN referencia;
    END IF;
END $$;

ALTER TABLE movimiento_inventario DROP CONSTRAINT IF EXISTS movimiento_inventario_un_origen_check;
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_un_origen_check
    CHECK (num_nonnulls(id_venta, id_lote, id_pedido_proveedor) <= 1);


-- ---------- 5. Fechas de despacho y de entrega del lote --------------
-- fecha_despacho se llenaba al ENTREGAR, así que en realidad era la fecha
-- de entrega y un lote EN_RUTA no tenía fecha de salida. Ahora:
--   fecha_despacho → al pasar a EN_RUTA      fecha_entrega → al ENTREGAR
-- Los lotes anteriores no registraron su salida: se aproxima con su
-- fecha de creación (única fecha conocida anterior al despacho).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                    WHERE table_name = 'lote_donacion' AND column_name = 'fecha_entrega') THEN
        ALTER TABLE lote_donacion ADD COLUMN fecha_entrega TIMESTAMPTZ;

        UPDATE lote_donacion SET fecha_entrega = fecha_despacho WHERE estado = 'ENTREGADO';
        UPDATE lote_donacion SET fecha_despacho = fecha_creacion WHERE estado IN ('EN_RUTA', 'ENTREGADO');
        UPDATE lote_donacion SET fecha_despacho = NULL WHERE estado = 'PENDIENTE';
    END IF;
END $$;

ALTER TABLE lote_donacion DROP CONSTRAINT IF EXISTS lote_donacion_fechas_segun_estado_check;
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_fechas_segun_estado_check
    CHECK (
        (estado = 'PENDIENTE' AND fecha_despacho IS NULL     AND fecha_entrega IS NULL) OR
        (estado = 'EN_RUTA'   AND fecha_despacho IS NOT NULL AND fecha_entrega IS NULL) OR
        (estado = 'ENTREGADO' AND fecha_despacho IS NOT NULL AND fecha_entrega IS NOT NULL)
    );

ALTER TABLE lote_donacion DROP CONSTRAINT IF EXISTS lote_donacion_orden_fechas_check;
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_orden_fechas_check
    CHECK (fecha_creacion <= fecha_despacho AND fecha_despacho <= fecha_entrega);


-- ---------- 6. Acabado ------------------------------------------------

-- 6.1 Clientes: la semilla guardó el nombre completo en "nombre" porque
-- "apellido" se agregó después (06). Se separan los clientes de demo y,
-- si queda alguno sin apellido, se toma la última palabra del nombre.
UPDATE cliente c
   SET nombre = s.nombre, apellido = s.apellido
  FROM (VALUES
        ('45678912', 'María Fernanda', 'Gutiérrez'),
        ('41234567', 'Jorge Luis',     'Ramírez'),
        ('47812365', 'Ana Belén',      'Torres'),
        ('48912345', 'Camila',         'Rodríguez Paredes'),
        ('46123789', 'Diego Alonso',   'Vega'),
        ('47234891', 'Valeria',        'Cusihuamán Quispe'),
        ('45678234', 'Sebastián',      'Flores Mamani')
       ) AS s (num_doc, nombre, apellido)
 WHERE c.num_doc = s.num_doc
   AND c.apellido IS NULL;

UPDATE cliente
   SET apellido = regexp_replace(nombre, '^.*\s', ''),
       nombre   = regexp_replace(nombre, '\s+\S+$', '')
 WHERE apellido IS NULL
   AND nombre ~ '\s';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM cliente WHERE apellido IS NULL) THEN
        RAISE NOTICE 'Hay clientes sin apellido: cliente.apellido queda opcional hasta completarlos.';
    ELSE
        ALTER TABLE cliente ALTER COLUMN apellido SET NOT NULL;
    END IF;
END $$;

-- 6.2 Documento de identidad: tipo válido, formato según tipo, ambos o
-- ninguno, y sin repetir.
ALTER TABLE cliente DROP CONSTRAINT IF EXISTS cliente_documento_check;
ALTER TABLE cliente ADD CONSTRAINT cliente_documento_check
    CHECK (
        (tipo_doc IS NULL AND num_doc IS NULL) OR
        (tipo_doc = 'DNI'       AND num_doc ~ '^[0-9]{8}$') OR
        (tipo_doc = 'RUC'       AND num_doc ~ '^[0-9]{11}$') OR
        (tipo_doc = 'CE'        AND num_doc ~ '^[A-Za-z0-9]{9,12}$') OR
        (tipo_doc = 'PASAPORTE' AND num_doc ~ '^[A-Za-z0-9]{6,12}$')
    );

ALTER TABLE cliente DROP CONSTRAINT IF EXISTS cliente_documento_key;
ALTER TABLE cliente ADD CONSTRAINT cliente_documento_key UNIQUE (tipo_doc, num_doc);

-- 6.3 RUC del proveedor: 11 dígitos y único.
ALTER TABLE proveedor DROP CONSTRAINT IF EXISTS proveedor_ruc_check;
ALTER TABLE proveedor ADD CONSTRAINT proveedor_ruc_check CHECK (ruc ~ '^[0-9]{11}$');

ALTER TABLE proveedor DROP CONSTRAINT IF EXISTS proveedor_ruc_key;
ALTER TABLE proveedor ADD CONSTRAINT proveedor_ruc_key UNIQUE (ruc);

-- 6.4 Importes de la línea de venta coherentes.
ALTER TABLE detalle_venta DROP CONSTRAINT IF EXISTS detalle_venta_importes_check;
ALTER TABLE detalle_venta ADD CONSTRAINT detalle_venta_importes_check
    CHECK (precio_unitario >= 0 AND subtotal = cantidad * precio_unitario);

-- 6.5 Índices de claves foráneas. PostgreSQL no los crea solo; sin ellos
-- cada JOIN o borrado en la tabla referenciada recorre la tabla entera.
CREATE INDEX IF NOT EXISTS idx_venta_cliente              ON venta (id_cliente);
CREATE INDEX IF NOT EXISTS idx_venta_usuario              ON venta (id_usuario);
CREATE INDEX IF NOT EXISTS idx_detalle_venta_venta        ON detalle_venta (id_venta);
CREATE INDEX IF NOT EXISTS idx_detalle_venta_producto     ON detalle_venta (codigo_producto);
CREATE INDEX IF NOT EXISTS idx_lote_comunidad             ON lote_donacion (id_comunidad);
CREATE INDEX IF NOT EXISTS idx_lote_ong                   ON lote_donacion (id_ong);
CREATE INDEX IF NOT EXISTS idx_lote_responsable           ON lote_donacion (id_usuario_responsable);
CREATE INDEX IF NOT EXISTS idx_donacion_producto          ON donacion (codigo_producto);
CREATE INDEX IF NOT EXISTS idx_pedido_prov_producto       ON pedido_proveedor (codigo_producto);
CREATE INDEX IF NOT EXISTS idx_pedido_prov_proveedor      ON pedido_proveedor (id_proveedor);
CREATE INDEX IF NOT EXISTS idx_pedido_prov_usuario        ON pedido_proveedor (id_usuario);
CREATE INDEX IF NOT EXISTS idx_mov_usuario                ON movimiento_inventario (id_usuario);
CREATE INDEX IF NOT EXISTS idx_mov_venta                  ON movimiento_inventario (id_venta);
CREATE INDEX IF NOT EXISTS idx_mov_lote                   ON movimiento_inventario (id_lote);
CREATE INDEX IF NOT EXISTS idx_mov_pedido_proveedor       ON movimiento_inventario (id_pedido_proveedor);
CREATE INDEX IF NOT EXISTS idx_producto_categoria         ON producto (categoria);
CREATE INDEX IF NOT EXISTS idx_producto_coleccion         ON producto (coleccion);

-- 6.6 API pública de Supabase. El escritorio y la web se conectan como
-- postgres; los roles anon y authenticated (la clave pública del
-- proyecto) no necesitan nada. Hasta ahora solo los frenaba el RLS sin
-- políticas: si alguien lo desactivaba en una tabla, quedaba abierta.
REVOKE ALL ON ALL TABLES    IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public REVOKE ALL ON TABLES    FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public REVOKE ALL ON SEQUENCES FROM anon, authenticated;

COMMIT;


-- ---------- Verificación (solo lectura) --------------------------------
-- Debe devolver 0 filas: el stock comprometido coincide con las
-- donaciones de ventas pagadas que aún no se entregan (regla 9.3).
SELECT p.codigo, p.stock_comprometido,
       COALESCE(SUM(d.cantidad) FILTER (WHERE d.estado IN ('PENDIENTE', 'ASIGNADA')), 0) AS esperado
  FROM producto p
  LEFT JOIN donacion d ON d.codigo_producto = p.codigo
 GROUP BY p.codigo, p.stock_comprometido
HAVING p.stock_comprometido <> COALESCE(SUM(d.cantidad) FILTER (WHERE d.estado IN ('PENDIENTE', 'ASIGNADA')), 0);

-- Debe devolver 0 filas: ninguna donación lista para lote viene de una venta sin pagar.
SELECT d.id, v.codigo_comprobante, v.estado
  FROM donacion d
  JOIN detalle_venta dv ON dv.id = d.id_detalle_venta
  JOIN venta v ON v.id = dv.id_venta
 WHERE d.estado <> 'EN_ESPERA' AND v.estado <> 'PAGADO';

-- Una fila por categoría con cuántos productos tiene.
SELECT c.nombre AS categoria, COUNT(p.codigo) AS productos
  FROM categoria c
  LEFT JOIN producto p ON p.categoria = c.nombre
 GROUP BY c.nombre
 ORDER BY c.nombre;
