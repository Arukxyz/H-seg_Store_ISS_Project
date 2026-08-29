-- =====================================================================
-- SEGITD-HÖSÉG · Esquema de base de datos
-- PostgreSQL / Supabase
-- Fuente única de verdad, compartida con el equipo web: ninguna tabla
-- ni columna se crea a mano desde el dashboard. Todo cambio pasa por
-- este archivo versionado.
-- =====================================================================

-- ---------- Usuarios internos (personal de Höség) --------------------
-- NO confundir con los clientes del e-commerce (tabla cliente).
CREATE TABLE usuario (
    id                SERIAL PRIMARY KEY,
    nombre            VARCHAR(120) NOT NULL,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(64)  NOT NULL,          -- SHA-256 en hexadecimal
    salt              VARCHAR(32)  NOT NULL,
    rol               VARCHAR(20)  NOT NULL
                      CHECK (rol IN ('ADMINISTRADOR','ENCARGADO')),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos INTEGER      NOT NULL DEFAULT 0,
    bloqueado_hasta   TIMESTAMPTZ,
    creado_en         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------- Catálogo de productos ------------------------------------
-- Editado desde el escritorio, consumido también por la web.
CREATE TABLE producto (
    codigo               VARCHAR(30) PRIMARY KEY,
    nombre               VARCHAR(150) NOT NULL,
    marca                VARCHAR(80),
    categoria            VARCHAR(60)  NOT NULL,
    coleccion            VARCHAR(80),
    talla                VARCHAR(10),
    descripcion          TEXT,
    url_imagen           TEXT,
    precio               NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    stock_comercial      INTEGER NOT NULL DEFAULT 0 CHECK (stock_comercial     >= 0),
    stock_comprometido   INTEGER NOT NULL DEFAULT 0 CHECK (stock_comprometido  >= 0),
    stock_minimo         INTEGER NOT NULL DEFAULT 5,
    aplica_triple_impacto BOOLEAN NOT NULL DEFAULT TRUE,
    tipo_compromiso      VARCHAR(10)
                         CHECK (tipo_compromiso IN ('ABRIGO','ARBOL')),
    visible_web          BOOLEAN NOT NULL DEFAULT TRUE,
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en            TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- Clientes del e-commerce ----------------------------------
CREATE TABLE cliente (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(150) NOT NULL,
    tipo_doc  VARCHAR(10),
    num_doc   VARCHAR(20),
    email     VARCHAR(150),
    telefono  VARCHAR(20),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- Ventas / pedidos -----------------------------------------
-- Insertadas por la web (origen='WEB'); gestionadas desde el escritorio.
CREATE TABLE venta (
    id                 SERIAL PRIMARY KEY,
    codigo_comprobante VARCHAR(30) NOT NULL UNIQUE,
    fecha              TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_cliente         INTEGER REFERENCES cliente(id),
    id_usuario         INTEGER REFERENCES usuario(id),   -- quién la confirmó
    origen             VARCHAR(12) NOT NULL DEFAULT 'WEB'
                       CHECK (origen IN ('WEB','PRESENCIAL')),
    estado             VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE'
                       CHECK (estado IN ('PENDIENTE','PAGADO','ANULADO')),
    total              NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE detalle_venta (
    id              SERIAL PRIMARY KEY,
    id_venta        INTEGER NOT NULL REFERENCES venta(id) ON DELETE CASCADE,
    codigo_producto VARCHAR(30) NOT NULL REFERENCES producto(codigo),
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10,2) NOT NULL,
    subtotal        NUMERIC(10,2) NOT NULL
);

-- ---------- Destinos de donación -------------------------------------
CREATE TABLE comunidad (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    distrito  VARCHAR(80),
    provincia VARCHAR(80),
    region    VARCHAR(80) NOT NULL DEFAULT 'Cusco'
);

CREATE TABLE ong (
    id       SERIAL PRIMARY KEY,
    nombre   VARCHAR(120) NOT NULL,
    contacto VARCHAR(120),
    telefono VARCHAR(20)
);

CREATE TABLE lote_donacion (
    id                     SERIAL PRIMARY KEY,
    codigo_lote            VARCHAR(30) NOT NULL UNIQUE,
    id_comunidad           INTEGER NOT NULL REFERENCES comunidad(id),
    id_ong                 INTEGER NOT NULL REFERENCES ong(id),
    id_usuario_responsable INTEGER REFERENCES usuario(id),
    fecha_creacion         TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_despacho         TIMESTAMPTZ,
    estado                 VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE'
                           CHECK (estado IN ('PENDIENTE','EN_RUTA','ENTREGADO')),
    observaciones          TEXT
);

-- ---------- Donación: la tabla bisagra del sistema -------------------
-- Cada compromiso social nace de una línea de venta y termina en un lote.
-- Es lo que resuelve el problema: qué venta generó qué donación.
CREATE TABLE donacion (
    id               SERIAL PRIMARY KEY,
    id_detalle_venta INTEGER NOT NULL REFERENCES detalle_venta(id),
    codigo_producto  VARCHAR(30) NOT NULL REFERENCES producto(codigo),
    cantidad         INTEGER NOT NULL CHECK (cantidad > 0),
    tipo             VARCHAR(10) NOT NULL CHECK (tipo IN ('ABRIGO','ARBOL')),
    estado           VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE'
                     CHECK (estado IN ('PENDIENTE','ASIGNADA','ENTREGADA')),
    id_lote          INTEGER REFERENCES lote_donacion(id),
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- Proveedores ----------------------------------------------
CREATE TABLE proveedor (
    id            SERIAL PRIMARY KEY,
    nombre_taller VARCHAR(150) NOT NULL,
    ruc           VARCHAR(11),
    contacto      VARCHAR(120),
    telefono      VARCHAR(20),
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE pedido_proveedor (
    id              SERIAL PRIMARY KEY,
    id_proveedor    INTEGER NOT NULL REFERENCES proveedor(id),
    codigo_producto VARCHAR(30) REFERENCES producto(codigo),  -- a qué producto del catálogo repone este pedido
    descripcion     VARCHAR(255) NOT NULL,
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now(),
    estado          VARCHAR(12) NOT NULL DEFAULT 'SOLICITADO'
                    CHECK (estado IN ('SOLICITADO','RECIBIDO','ANULADO')),
    id_usuario      INTEGER REFERENCES usuario(id)
);

-- ---------- Bitácora de inventario -----------------------------------
-- Ningún stock se modifica sin registrar aquí. Sustenta la auditoría B
-- y demuestra que ambos sistemas escriben sobre la misma base.
CREATE TABLE movimiento_inventario (
    id              SERIAL PRIMARY KEY,
    codigo_producto VARCHAR(30) NOT NULL REFERENCES producto(codigo),
    tipo_stock      VARCHAR(15) NOT NULL
                    CHECK (tipo_stock IN ('COMERCIAL','COMPROMETIDO')),
    tipo_movimiento VARCHAR(10) NOT NULL
                    CHECK (tipo_movimiento IN ('INGRESO','SALIDA','AJUSTE')),
    cantidad        INTEGER NOT NULL,
    motivo          VARCHAR(150),
    referencia      VARCHAR(50),        -- p.ej. 'VENTA:12', 'LOTE:HSG-L004'
    origen_sistema  VARCHAR(12) NOT NULL DEFAULT 'ESCRITORIO'
                    CHECK (origen_sistema IN ('ESCRITORIO','WEB')),
    id_usuario      INTEGER REFERENCES usuario(id),
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- Índices ---------------------------------------------------
CREATE INDEX idx_donacion_estado    ON donacion(estado);
CREATE INDEX idx_donacion_lote      ON donacion(id_lote);
CREATE INDEX idx_venta_estado_fecha ON venta(estado, fecha);
CREATE INDEX idx_mov_producto_fecha ON movimiento_inventario(codigo_producto, fecha);
CREATE INDEX idx_producto_activo    ON producto(activo);
