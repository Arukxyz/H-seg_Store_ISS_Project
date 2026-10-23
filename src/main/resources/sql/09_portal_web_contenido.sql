-- =====================================================================
-- SEGITD-HÖSÉG · Portal web: contenido, Höség Café y comunicación
-- Tablas que solo usa el portal web (el escritorio no las lee):
--   1. Contenido editable desde el panel web (banners, textos, políticas).
--   2. Höség Café: carta y locales; café en grano en el catálogo.
--   3. Mensajes de contacto y suscriptores del boletín.
--   4. Libro de Reclamaciones (obligatorio para tiendas en línea en Perú).
--
-- Requiere 08. Idempotente y en una transacción.
-- =====================================================================

BEGIN;

-- ---------- 1. Contenido editable ---------------------------------------
-- Bloques de texto e imagen que el administrador cambia sin tocar código
-- (CU-19). La clave la conoce la plantilla que muestra el bloque; si un
-- bloque no existe o está vacío, la plantilla usa su texto por defecto.
CREATE TABLE IF NOT EXISTS contenido_web (
    clave          VARCHAR(60)  PRIMARY KEY CHECK (clave ~ '^[a-z0-9]+([.-][a-z0-9]+)*$'),
    titulo         VARCHAR(200),
    cuerpo         TEXT,
    imagen_url     TEXT,
    actualizado_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    id_usuario     INTEGER REFERENCES usuario (id),

    -- La cifra de árboles del café se publica como número.
    CONSTRAINT contenido_web_cafe_arboles_check
        CHECK (clave <> 'cafe.arboles' OR cuerpo ~ '^[0-9]+$')
);

CREATE INDEX IF NOT EXISTS idx_contenido_usuario ON contenido_web (id_usuario);

-- Bloques iniciales con los textos que hoy tiene la Home, para que el
-- panel tenga qué editar. ON CONFLICT: no pisa lo que ya editó el admin.
INSERT INTO contenido_web (clave, titulo, cuerpo) VALUES
    ('home.banner',
     'Abriga a alguien más con cada compra.',
     'Empresa peruana de ropa de abrigo con triple impacto. Cada prenda llega a dos personas: a ti y a un niño de una comunidad altoandina de Cusco.'),
    ('nosotros.historia',
     'Ropa que calienta en los dos sentidos.',
     'HÖSÉG nació en Perú con una pregunta simple: ¿puede vender ropa de abrigo y al mismo tiempo asegurarse de que quienes más la necesitan también la tengan? La respuesta es sí, y lo probamos con cada pedido.'),
    ('legal.terminos',     'Términos y condiciones', NULL),
    ('legal.politica-rsu', 'Política de responsabilidad social', NULL),
    -- Cifra publicada en el sitio anterior de Höség Café.
    ('cafe.arboles',       'Árboles sembrados por Höség Café', '5018')
ON CONFLICT (clave) DO NOTHING;


-- ---------- 2. Höség Café ------------------------------------------------
-- Carta de la cafetería (bebidas y postres que se consumen en el local).
-- No se venden en línea: el café en grano sí, y vive en producto.
-- activo = FALSE es la baja lógica; disponible = FALSE es "agotado hoy".
CREATE TABLE IF NOT EXISTS cafe_carta (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(120)  NOT NULL,
    descripcion VARCHAR(255),
    categoria   VARCHAR(40)   NOT NULL,
    precio      NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    disponible  BOOLEAN       NOT NULL DEFAULT TRUE,
    activo      BOOLEAN       NOT NULL DEFAULT TRUE,
    orden       INTEGER       NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_cafe_carta_categoria ON cafe_carta (categoria, orden);

CREATE TABLE IF NOT EXISTS cafe_local (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL UNIQUE,
    direccion VARCHAR(255) NOT NULL,
    horario   VARCHAR(150),
    telefono  VARCHAR(20),
    url_mapa  TEXT,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO cafe_local (nombre, direccion, horario)
VALUES ('Höség Café Cusco', 'Calle Palacio 110 int. 2, Cusco', 'Lunes a domingo, de 8:30 a 19:30')
ON CONFLICT (nombre) DO NOTHING;

-- Café en grano: se compra en la tienda y genera un árbol. Sin talla
-- (el detalle de producto no muestra selector) ni colección de ropa.
-- La carta (cafe_carta) no se siembra: se carga desde el panel con los
-- datos reales del local.
INSERT INTO producto
    (codigo, nombre, marca, categoria, coleccion, talla, descripcion, precio,
     stock_comercial, stock_minimo, tipo_compromiso, visible_web, activo)
VALUES
    ('HSG-CAF-001', 'Café Bourbon · Yesica Llanqui', 'Höség', 'Café', NULL, NULL,
     'Café de especialidad en grano, variedad Bourbon, de la productora Yesica Llanqui. Bolsa de 250 g.',
     40.00, 50, 10, 'ARBOL', TRUE, TRUE),
    ('HSG-CAF-002', 'Café Typica · Eulogia Kehuarucho', 'Höség', 'Café', NULL, NULL,
     'Café de especialidad en grano, variedad Typica, de la productora Eulogia Kehuarucho. Bolsa de 250 g.',
     40.00, 50, 10, 'ARBOL', TRUE, TRUE)
ON CONFLICT (codigo) DO NOTHING;


-- ---------- 3. Contacto y boletín ----------------------------------------
-- Mensajes del formulario de contacto (CU-08), atendidos desde el panel
-- (CU-20). Quién y cuándo atendió van juntos con el estado.
CREATE TABLE IF NOT EXISTS mensaje_contacto (
    id                 SERIAL PRIMARY KEY,
    nombre             VARCHAR(150) NOT NULL,
    email              VARCHAR(150) NOT NULL CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    asunto             VARCHAR(150) NOT NULL,
    mensaje            TEXT         NOT NULL,
    estado             VARCHAR(10)  NOT NULL DEFAULT 'NUEVO' CHECK (estado IN ('NUEVO', 'ATENDIDO')),
    creado_en          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atendido_en        TIMESTAMPTZ,
    id_usuario_atiende INTEGER REFERENCES usuario (id),
    CONSTRAINT mensaje_contacto_atencion_check
        CHECK ((estado = 'ATENDIDO') = (atendido_en IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS idx_mensaje_estado_fecha ON mensaje_contacto (estado, creado_en);
CREATE INDEX IF NOT EXISTS idx_mensaje_usuario      ON mensaje_contacto (id_usuario_atiende);

-- Suscriptores del boletín Changemaker (CU-09). Darse de baja no borra:
-- marca activo = FALSE, y volver a suscribirse reactiva la misma fila.
CREATE TABLE IF NOT EXISTS suscriptor_boletin (
    id          SERIAL PRIMARY KEY,
    email       VARCHAR(150) NOT NULL CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    suscrito_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    baja_en     TIMESTAMPTZ,
    CONSTRAINT suscriptor_baja_check CHECK (activo = (baja_en IS NULL))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_suscriptor_email ON suscriptor_boletin (lower(email));


-- ---------- 4. Libro de Reclamaciones -------------------------------------
-- Hoja de reclamación virtual. El número de hoja lo da la base con su
-- secuencia (LR-000001, LR-000002…), igual que los comprobantes WEB.
-- RECLAMO: disconformidad con el producto o servicio.
-- QUEJA:   malestar con la atención, sin relación con el producto.
-- Documento con las mismas reglas que cliente (07, 6.2).
CREATE SEQUENCE IF NOT EXISTS seq_reclamacion START WITH 1;

CREATE TABLE IF NOT EXISTS libro_reclamaciones (
    id                 SERIAL PRIMARY KEY,
    numero_hoja        VARCHAR(20)   NOT NULL UNIQUE
                       DEFAULT 'LR-' || lpad(nextval('seq_reclamacion')::text, 6, '0'),
    tipo               VARCHAR(10)   NOT NULL CHECK (tipo IN ('RECLAMO', 'QUEJA')),
    nombre             VARCHAR(150)  NOT NULL,
    tipo_doc           VARCHAR(10)   NOT NULL,
    num_doc            VARCHAR(20)   NOT NULL,
    domicilio          VARCHAR(255)  NOT NULL,
    email              VARCHAR(150)  NOT NULL CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    telefono           VARCHAR(20),
    tipo_bien          VARCHAR(10)   NOT NULL CHECK (tipo_bien IN ('PRODUCTO', 'SERVICIO')),
    monto_reclamado    NUMERIC(10,2) CHECK (monto_reclamado >= 0),
    descripcion_bien   VARCHAR(255)  NOT NULL,
    detalle            TEXT          NOT NULL,
    pedido_consumidor  TEXT          NOT NULL,
    codigo_comprobante VARCHAR(30)   REFERENCES venta (codigo_comprobante),
    estado             VARCHAR(12)   NOT NULL DEFAULT 'REGISTRADO' CHECK (estado IN ('REGISTRADO', 'ATENDIDO')),
    respuesta          TEXT,
    creado_en          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atendido_en        TIMESTAMPTZ,
    id_usuario_atiende INTEGER REFERENCES usuario (id),

    CONSTRAINT libro_reclamaciones_documento_check CHECK (
        (tipo_doc = 'DNI'       AND num_doc ~ '^[0-9]{8}$') OR
        (tipo_doc = 'RUC'       AND num_doc ~ '^[0-9]{11}$') OR
        (tipo_doc = 'CE'        AND num_doc ~ '^[A-Za-z0-9]{9,12}$') OR
        (tipo_doc = 'PASAPORTE' AND num_doc ~ '^[A-Za-z0-9]{6,12}$')
    ),
    -- Atender es responder: los tres datos van juntos.
    CONSTRAINT libro_reclamaciones_atencion_check CHECK (
        (estado = 'ATENDIDO') = (atendido_en IS NOT NULL AND respuesta IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_reclamo_estado_fecha ON libro_reclamaciones (estado, creado_en);
CREATE INDEX IF NOT EXISTS idx_reclamo_comprobante  ON libro_reclamaciones (codigo_comprobante);
CREATE INDEX IF NOT EXISTS idx_reclamo_usuario      ON libro_reclamaciones (id_usuario_atiende);


-- ---------- 5. Permisos ---------------------------------------------------
ALTER TABLE contenido_web       ENABLE ROW LEVEL SECURITY;
ALTER TABLE cafe_carta          ENABLE ROW LEVEL SECURITY;
ALTER TABLE cafe_local          ENABLE ROW LEVEL SECURITY;
ALTER TABLE mensaje_contacto    ENABLE ROW LEVEL SECURITY;
ALTER TABLE suscriptor_boletin  ENABLE ROW LEVEL SECURITY;
ALTER TABLE libro_reclamaciones ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON contenido_web, cafe_carta, cafe_local, mensaje_contacto,
              suscriptor_boletin, libro_reclamaciones
    FROM anon, authenticated;
REVOKE ALL ON SEQUENCE cafe_carta_id_seq, cafe_local_id_seq, mensaje_contacto_id_seq,
                       suscriptor_boletin_id_seq, libro_reclamaciones_id_seq, seq_reclamacion
    FROM anon, authenticated;

COMMIT;


-- ---------- Verificación (solo lectura) ----------------------------------
-- 5 bloques de contenido iniciales.
SELECT clave, titulo FROM contenido_web ORDER BY clave;

-- Los 2 cafés en grano, visibles en la tienda y con su árbol.
SELECT codigo, nombre, precio, tipo_compromiso, visible_web
  FROM producto WHERE categoria = 'Café' ORDER BY codigo;

-- Tablas nuevas con RLS activo: 6 filas con relrowsecurity = true.
SELECT relname, relrowsecurity
  FROM pg_class
 WHERE relname IN ('contenido_web', 'cafe_carta', 'cafe_local',
                   'mensaje_contacto', 'suscriptor_boletin', 'libro_reclamaciones')
 ORDER BY relname;
