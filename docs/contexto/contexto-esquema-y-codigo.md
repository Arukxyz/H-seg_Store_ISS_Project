# Contexto técnico · SEGITD-HÖSÉG y portal web

Documento generado a partir del estado real del proyecto, para dar contexto a otra sesión de documentación. Tiene cuatro partes:

1. **DDL** de la base de datos en Supabase (esquema `public`), reconstruido desde el catálogo de PostgreSQL.
2. **Modelo** del escritorio (`pe.edu.utp.segitd.modelo`): clases, enums y records con sus atributos y métodos públicos.
3. **Servicios** del escritorio (`pe.edu.utp.segitd.servicio`): firmas públicas y lo que hace cada una.
4. **Reglas de negocio** implementadas, con la clase donde viven.

**Estado de la base:** el script `07_normalizacion.sql` ya está aplicado en Supabase. Lo comprobé con los datos: la tabla `categoria` tiene 3 filas, existen donaciones en estado `EN_ESPERA`, la columna `referencia` ya no existe y los 7 clientes tienen apellido. El DDL de este documento describe, por tanto, el esquema **después del 07**.

**Estado del código:** los cambios del 07 en el escritorio y en la web están sin commitear en sus ramas de trabajo.

---

## 1. DDL de la base de datos (Supabase, schema `public`)

El DDL se reconstruyó consultando `pg_catalog` e `information_schema`. Las restricciones van al final como `ALTER TABLE ... ADD CONSTRAINT` para no depender del orden de creación. Las secuencias de las columnas `SERIAL` aparecen en la sección de secuencias.

-- DDL reconstruido desde el catálogo de PostgreSQL (schema public)

-- ===== TIPOS ENUM =====
-- ninguno (los estados se guardan como VARCHAR con CHECK)

-- ===== SECUENCIAS =====
CREATE SEQUENCE IF NOT EXISTS cliente_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS comunidad_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS detalle_venta_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS donacion_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS lote_donacion_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS movimiento_inventario_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS ong_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS pedido_proveedor_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS proveedor_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS seq_comprobante_web START WITH 0 INCREMENT BY 1 MINVALUE 0 MAXVALUE 9223372036854775807 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS usuario_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS venta_id_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 NO CYCLE;

-- ===== TABLA categoria =====
CREATE TABLE categoria (
    nombre character varying(60) NOT NULL
);
-- ===== TABLA cliente =====
CREATE TABLE cliente (
    id integer DEFAULT nextval('cliente_id_seq'::regclass) NOT NULL,
    nombre character varying(150) NOT NULL,
    tipo_doc character varying(10),
    num_doc character varying(20),
    email character varying(150) NOT NULL,
    telefono character varying(20),
    creado_en timestamp with time zone DEFAULT now() NOT NULL,
    apellido character varying(100) NOT NULL,
    direccion character varying(255),
    password_hash character varying(72)
);
-- ===== TABLA coleccion =====
CREATE TABLE coleccion (
    nombre character varying(80) NOT NULL
);
-- ===== TABLA comunidad =====
CREATE TABLE comunidad (
    id integer DEFAULT nextval('comunidad_id_seq'::regclass) NOT NULL,
    nombre character varying(120) NOT NULL,
    distrito character varying(80),
    provincia character varying(80),
    region character varying(80) DEFAULT 'Cusco'::character varying NOT NULL
);
-- ===== TABLA detalle_venta =====
CREATE TABLE detalle_venta (
    id integer DEFAULT nextval('detalle_venta_id_seq'::regclass) NOT NULL,
    id_venta integer NOT NULL,
    codigo_producto character varying(30) NOT NULL,
    cantidad integer NOT NULL,
    precio_unitario numeric(10,2) NOT NULL,
    subtotal numeric(10,2) NOT NULL
);
-- ===== TABLA donacion =====
CREATE TABLE donacion (
    id integer DEFAULT nextval('donacion_id_seq'::regclass) NOT NULL,
    id_detalle_venta integer NOT NULL,
    codigo_producto character varying(30) NOT NULL,
    cantidad integer NOT NULL,
    tipo character varying(10) NOT NULL,
    estado character varying(12) DEFAULT 'EN_ESPERA'::character varying NOT NULL,
    id_lote integer,
    creado_en timestamp with time zone DEFAULT now() NOT NULL
);
-- ===== TABLA lote_donacion =====
CREATE TABLE lote_donacion (
    id integer DEFAULT nextval('lote_donacion_id_seq'::regclass) NOT NULL,
    codigo_lote character varying(30) NOT NULL,
    id_comunidad integer NOT NULL,
    id_ong integer NOT NULL,
    id_usuario_responsable integer,
    fecha_creacion timestamp with time zone DEFAULT now() NOT NULL,
    fecha_despacho timestamp with time zone,
    estado character varying(12) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    observaciones text,
    fecha_entrega timestamp with time zone
);
-- ===== TABLA movimiento_inventario =====
CREATE TABLE movimiento_inventario (
    id integer DEFAULT nextval('movimiento_inventario_id_seq'::regclass) NOT NULL,
    codigo_producto character varying(30) NOT NULL,
    tipo_stock character varying(15) NOT NULL,
    tipo_movimiento character varying(10) NOT NULL,
    cantidad integer NOT NULL,
    motivo character varying(150),
    origen_sistema character varying(12) DEFAULT 'ESCRITORIO'::character varying NOT NULL,
    id_usuario integer,
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    id_venta integer,
    id_lote integer,
    id_pedido_proveedor integer
);
-- ===== TABLA ong =====
CREATE TABLE ong (
    id integer DEFAULT nextval('ong_id_seq'::regclass) NOT NULL,
    nombre character varying(120) NOT NULL,
    contacto character varying(120),
    telefono character varying(20)
);
-- ===== TABLA pedido_proveedor =====
CREATE TABLE pedido_proveedor (
    id integer DEFAULT nextval('pedido_proveedor_id_seq'::regclass) NOT NULL,
    id_proveedor integer NOT NULL,
    descripcion character varying(255) NOT NULL,
    cantidad integer NOT NULL,
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    estado character varying(12) DEFAULT 'SOLICITADO'::character varying NOT NULL,
    id_usuario integer,
    codigo_producto character varying(30)
);
-- ===== TABLA producto =====
CREATE TABLE producto (
    codigo character varying(30) NOT NULL,
    nombre character varying(150) NOT NULL,
    marca character varying(80),
    categoria character varying(60) NOT NULL,
    coleccion character varying(80),
    talla character varying(10),
    descripcion text,
    url_imagen text,
    precio numeric(10,2) NOT NULL,
    stock_comercial integer DEFAULT 0 NOT NULL,
    stock_comprometido integer DEFAULT 0 NOT NULL,
    stock_minimo integer DEFAULT 5 NOT NULL,
    tipo_compromiso character varying(10),
    visible_web boolean DEFAULT true NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    creado_en timestamp with time zone DEFAULT now() NOT NULL,
    aplica_triple_impacto boolean GENERATED ALWAYS AS ((tipo_compromiso IS NOT NULL)) STORED
);
-- ===== TABLA proveedor =====
CREATE TABLE proveedor (
    id integer DEFAULT nextval('proveedor_id_seq'::regclass) NOT NULL,
    nombre_taller character varying(150) NOT NULL,
    ruc character varying(11),
    contacto character varying(120),
    telefono character varying(20),
    activo boolean DEFAULT true NOT NULL
);
-- ===== TABLA usuario =====
CREATE TABLE usuario (
    id integer DEFAULT nextval('usuario_id_seq'::regclass) NOT NULL,
    nombre character varying(120) NOT NULL,
    username character varying(50) NOT NULL,
    password_hash character varying(64) NOT NULL,
    salt character varying(32) NOT NULL,
    rol character varying(20) NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    intentos_fallidos integer DEFAULT 0 NOT NULL,
    bloqueado_hasta timestamp with time zone,
    creado_en timestamp with time zone DEFAULT now() NOT NULL
);
-- ===== TABLA venta =====
CREATE TABLE venta (
    id integer DEFAULT nextval('venta_id_seq'::regclass) NOT NULL,
    codigo_comprobante character varying(30) NOT NULL,
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    id_cliente integer,
    id_usuario integer,
    origen character varying(12) DEFAULT 'WEB'::character varying NOT NULL,
    estado character varying(12) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    total numeric(10,2) DEFAULT 0 NOT NULL
);

-- ===== RESTRICCIONES (PK, UNIQUE, CHECK, FK) =====
ALTER TABLE categoria ADD CONSTRAINT categoria_pkey PRIMARY KEY (nombre);
ALTER TABLE cliente ADD CONSTRAINT cliente_documento_check CHECK ((((tipo_doc IS NULL) AND (num_doc IS NULL)) OR (((tipo_doc)::text = 'DNI'::text) AND ((num_doc)::text ~ '^[0-9]{8}$'::text)) OR (((tipo_doc)::text = 'RUC'::text) AND ((num_doc)::text ~ '^[0-9]{11}$'::text)) OR (((tipo_doc)::text = 'CE'::text) AND ((num_doc)::text ~ '^[A-Za-z0-9]{9,12}$'::text)) OR (((tipo_doc)::text = 'PASAPORTE'::text) AND ((num_doc)::text ~ '^[A-Za-z0-9]{6,12}$'::text))));
ALTER TABLE cliente ADD CONSTRAINT cliente_pkey PRIMARY KEY (id);
ALTER TABLE cliente ADD CONSTRAINT cliente_documento_key UNIQUE (tipo_doc, num_doc);
ALTER TABLE coleccion ADD CONSTRAINT coleccion_pkey PRIMARY KEY (nombre);
ALTER TABLE comunidad ADD CONSTRAINT comunidad_pkey PRIMARY KEY (id);
ALTER TABLE detalle_venta ADD CONSTRAINT detalle_venta_cantidad_check CHECK ((cantidad > 0));
ALTER TABLE detalle_venta ADD CONSTRAINT detalle_venta_importes_check CHECK (((precio_unitario >= (0)::numeric) AND (subtotal = ((cantidad)::numeric * precio_unitario))));
ALTER TABLE detalle_venta ADD CONSTRAINT detalle_venta_codigo_producto_fkey FOREIGN KEY (codigo_producto) REFERENCES producto(codigo);
ALTER TABLE detalle_venta ADD CONSTRAINT detalle_venta_id_venta_fkey FOREIGN KEY (id_venta) REFERENCES venta(id) ON DELETE CASCADE;
ALTER TABLE detalle_venta ADD CONSTRAINT detalle_venta_pkey PRIMARY KEY (id);
ALTER TABLE donacion ADD CONSTRAINT donacion_cantidad_check CHECK ((cantidad > 0));
ALTER TABLE donacion ADD CONSTRAINT donacion_estado_check CHECK (((estado)::text = ANY ((ARRAY['EN_ESPERA'::character varying, 'PENDIENTE'::character varying, 'ASIGNADA'::character varying, 'ENTREGADA'::character varying])::text[])));
ALTER TABLE donacion ADD CONSTRAINT donacion_lote_segun_estado_check CHECK ((((estado)::text = ANY ((ARRAY['EN_ESPERA'::character varying, 'PENDIENTE'::character varying])::text[])) = (id_lote IS NULL)));
ALTER TABLE donacion ADD CONSTRAINT donacion_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ABRIGO'::character varying, 'ARBOL'::character varying])::text[])));
ALTER TABLE donacion ADD CONSTRAINT donacion_codigo_producto_fkey FOREIGN KEY (codigo_producto) REFERENCES producto(codigo);
ALTER TABLE donacion ADD CONSTRAINT donacion_id_detalle_venta_fkey FOREIGN KEY (id_detalle_venta) REFERENCES detalle_venta(id);
ALTER TABLE donacion ADD CONSTRAINT donacion_id_lote_fkey FOREIGN KEY (id_lote) REFERENCES lote_donacion(id);
ALTER TABLE donacion ADD CONSTRAINT donacion_pkey PRIMARY KEY (id);
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'EN_RUTA'::character varying, 'ENTREGADO'::character varying])::text[])));
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_fechas_segun_estado_check CHECK (((((estado)::text = 'PENDIENTE'::text) AND (fecha_despacho IS NULL) AND (fecha_entrega IS NULL)) OR (((estado)::text = 'EN_RUTA'::text) AND (fecha_despacho IS NOT NULL) AND (fecha_entrega IS NULL)) OR (((estado)::text = 'ENTREGADO'::text) AND (fecha_despacho IS NOT NULL) AND (fecha_entrega IS NOT NULL))));
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_orden_fechas_check CHECK (((fecha_creacion <= fecha_despacho) AND (fecha_despacho <= fecha_entrega)));
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_id_comunidad_fkey FOREIGN KEY (id_comunidad) REFERENCES comunidad(id);
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_id_ong_fkey FOREIGN KEY (id_ong) REFERENCES ong(id);
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_id_usuario_responsable_fkey FOREIGN KEY (id_usuario_responsable) REFERENCES usuario(id);
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_pkey PRIMARY KEY (id);
ALTER TABLE lote_donacion ADD CONSTRAINT lote_donacion_codigo_lote_key UNIQUE (codigo_lote);
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_origen_sistema_check CHECK (((origen_sistema)::text = ANY ((ARRAY['ESCRITORIO'::character varying, 'WEB'::character varying])::text[])));
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_tipo_movimiento_check CHECK (((tipo_movimiento)::text = ANY ((ARRAY['INGRESO'::character varying, 'SALIDA'::character varying, 'AJUSTE'::character varying])::text[])));
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_tipo_stock_check CHECK (((tipo_stock)::text = ANY ((ARRAY['COMERCIAL'::character varying, 'COMPROMETIDO'::character varying])::text[])));
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_un_origen_check CHECK ((num_nonnulls(id_venta, id_lote, id_pedido_proveedor) <= 1));
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_codigo_producto_fkey FOREIGN KEY (codigo_producto) REFERENCES producto(codigo);
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_id_lote_fkey FOREIGN KEY (id_lote) REFERENCES lote_donacion(id);
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_id_pedido_proveedor_fkey FOREIGN KEY (id_pedido_proveedor) REFERENCES pedido_proveedor(id);
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario(id);
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_id_venta_fkey FOREIGN KEY (id_venta) REFERENCES venta(id);
ALTER TABLE movimiento_inventario ADD CONSTRAINT movimiento_inventario_pkey PRIMARY KEY (id);
ALTER TABLE ong ADD CONSTRAINT ong_pkey PRIMARY KEY (id);
ALTER TABLE pedido_proveedor ADD CONSTRAINT pedido_proveedor_cantidad_check CHECK ((cantidad > 0));
ALTER TABLE pedido_proveedor ADD CONSTRAINT pedido_proveedor_estado_check CHECK (((estado)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'RECIBIDO'::character varying, 'ANULADO'::character varying])::text[])));
ALTER TABLE pedido_proveedor ADD CONSTRAINT pedido_proveedor_codigo_producto_fkey FOREIGN KEY (codigo_producto) REFERENCES producto(codigo);
ALTER TABLE pedido_proveedor ADD CONSTRAINT pedido_proveedor_id_proveedor_fkey FOREIGN KEY (id_proveedor) REFERENCES proveedor(id);
ALTER TABLE pedido_proveedor ADD CONSTRAINT pedido_proveedor_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario(id);
ALTER TABLE pedido_proveedor ADD CONSTRAINT pedido_proveedor_pkey PRIMARY KEY (id);
ALTER TABLE producto ADD CONSTRAINT producto_precio_check CHECK ((precio >= (0)::numeric));
ALTER TABLE producto ADD CONSTRAINT producto_stock_comercial_check CHECK ((stock_comercial >= 0));
ALTER TABLE producto ADD CONSTRAINT producto_stock_comprometido_check CHECK ((stock_comprometido >= 0));
ALTER TABLE producto ADD CONSTRAINT producto_tipo_compromiso_check CHECK (((tipo_compromiso)::text = ANY ((ARRAY['ABRIGO'::character varying, 'ARBOL'::character varying])::text[])));
ALTER TABLE producto ADD CONSTRAINT producto_categoria_fkey FOREIGN KEY (categoria) REFERENCES categoria(nombre) ON UPDATE CASCADE;
ALTER TABLE producto ADD CONSTRAINT producto_coleccion_fkey FOREIGN KEY (coleccion) REFERENCES coleccion(nombre) ON UPDATE CASCADE;
ALTER TABLE producto ADD CONSTRAINT producto_pkey PRIMARY KEY (codigo);
ALTER TABLE proveedor ADD CONSTRAINT proveedor_ruc_check CHECK (((ruc)::text ~ '^[0-9]{11}$'::text));
ALTER TABLE proveedor ADD CONSTRAINT proveedor_pkey PRIMARY KEY (id);
ALTER TABLE proveedor ADD CONSTRAINT proveedor_ruc_key UNIQUE (ruc);
ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check CHECK (((rol)::text = ANY ((ARRAY['ADMINISTRADOR'::character varying, 'ENCARGADO'::character varying])::text[])));
ALTER TABLE usuario ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);
ALTER TABLE usuario ADD CONSTRAINT usuario_username_key UNIQUE (username);
ALTER TABLE venta ADD CONSTRAINT venta_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'PAGADO'::character varying, 'ANULADO'::character varying])::text[])));
ALTER TABLE venta ADD CONSTRAINT venta_origen_check CHECK (((origen)::text = ANY ((ARRAY['WEB'::character varying, 'PRESENCIAL'::character varying])::text[])));
ALTER TABLE venta ADD CONSTRAINT venta_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES cliente(id);
ALTER TABLE venta ADD CONSTRAINT venta_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario(id);
ALTER TABLE venta ADD CONSTRAINT venta_pkey PRIMARY KEY (id);
ALTER TABLE venta ADD CONSTRAINT venta_codigo_comprobante_key UNIQUE (codigo_comprobante);

-- ===== ÍNDICES (sin los que respaldan restricciones) =====
CREATE INDEX idx_detalle_venta_producto ON public.detalle_venta USING btree (codigo_producto);
CREATE INDEX idx_detalle_venta_venta ON public.detalle_venta USING btree (id_venta);
CREATE INDEX idx_donacion_detalle ON public.donacion USING btree (id_detalle_venta);
CREATE INDEX idx_donacion_estado ON public.donacion USING btree (estado);
CREATE INDEX idx_donacion_lote ON public.donacion USING btree (id_lote);
CREATE INDEX idx_donacion_producto ON public.donacion USING btree (codigo_producto);
CREATE INDEX idx_lote_comunidad ON public.lote_donacion USING btree (id_comunidad);
CREATE INDEX idx_lote_ong ON public.lote_donacion USING btree (id_ong);
CREATE INDEX idx_lote_responsable ON public.lote_donacion USING btree (id_usuario_responsable);
CREATE INDEX idx_mov_lote ON public.movimiento_inventario USING btree (id_lote);
CREATE INDEX idx_mov_pedido_proveedor ON public.movimiento_inventario USING btree (id_pedido_proveedor);
CREATE INDEX idx_mov_producto_fecha ON public.movimiento_inventario USING btree (codigo_producto, fecha);
CREATE INDEX idx_mov_usuario ON public.movimiento_inventario USING btree (id_usuario);
CREATE INDEX idx_mov_venta ON public.movimiento_inventario USING btree (id_venta);
CREATE INDEX idx_pedido_prov_producto ON public.pedido_proveedor USING btree (codigo_producto);
CREATE INDEX idx_pedido_prov_proveedor ON public.pedido_proveedor USING btree (id_proveedor);
CREATE INDEX idx_pedido_prov_usuario ON public.pedido_proveedor USING btree (id_usuario);
CREATE INDEX idx_producto_activo ON public.producto USING btree (activo);
CREATE INDEX idx_producto_categoria ON public.producto USING btree (categoria);
CREATE INDEX idx_producto_coleccion ON public.producto USING btree (coleccion);
CREATE INDEX idx_venta_cliente ON public.venta USING btree (id_cliente);
CREATE INDEX idx_venta_estado_fecha ON public.venta USING btree (estado, fecha);
CREATE INDEX idx_venta_usuario ON public.venta USING btree (id_usuario);
CREATE UNIQUE INDEX uq_cliente_email ON public.cliente USING btree (lower((email)::text));

-- ===== ROW LEVEL SECURITY =====
ALTER TABLE categoria ENABLE ROW LEVEL SECURITY;
ALTER TABLE cliente ENABLE ROW LEVEL SECURITY;
ALTER TABLE coleccion ENABLE ROW LEVEL SECURITY;
ALTER TABLE comunidad ENABLE ROW LEVEL SECURITY;
ALTER TABLE detalle_venta ENABLE ROW LEVEL SECURITY;
ALTER TABLE donacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE lote_donacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE movimiento_inventario ENABLE ROW LEVEL SECURITY;
ALTER TABLE ong ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedido_proveedor ENABLE ROW LEVEL SECURITY;
ALTER TABLE producto ENABLE ROW LEVEL SECURITY;
ALTER TABLE proveedor ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE venta ENABLE ROW LEVEL SECURITY;
-- políticas RLS: ninguna

-- ===== VISTAS =====
-- ninguna

-- ===== FUNCIONES =====
-- ninguna

-- ===== TRIGGERS =====
-- ninguno

-- ===== PRIVILEGIOS DE ROLES DE LA API (anon, authenticated) =====
-- sin privilegios


---

## 2. Modelo y servicios del escritorio

Las firmas se leyeron del código fuente. La descripción de cada clase o método es el javadoc del código; si no tiene, aparece sin descripción.


### `CategoriaDemanda` (enum)
Valores: ALTA, NORMAL, SIN_MOVIMIENTO

### `Cliente` (class)
Cliente del e-commerce. NO confundir con Usuario (personal interno).
Atributos:
- `id`: `Integer`
- `nombre`: `String`
- `tipoDoc`: `String`
- `numDoc`: `String`
- `email`: `String`
- `telefono`: `String`
- `creadoEn`: `OffsetDateTime`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getNombre()`
- `public void setNombre(String nombre)`
- `public String getTipoDoc()`
- `public void setTipoDoc(String tipoDoc)`
- `public String getNumDoc()`
- `public void setNumDoc(String numDoc)`
- `public String getEmail()`
- `public void setEmail(String email)`
- `public String getTelefono()`
- `public void setTelefono(String telefono)`
- `public OffsetDateTime getCreadoEn()`
- `public void setCreadoEn(OffsetDateTime creadoEn)`

### `Comunidad` (class)
Atributos:
- `id`: `Integer`
- `nombre`: `String`
- `distrito`: `String`
- `provincia`: `String`
- `region`: `String`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getNombre()`
- `public void setNombre(String nombre)`
- `public String getDistrito()`
- `public void setDistrito(String distrito)`
- `public String getProvincia()`
- `public void setProvincia(String provincia)`
- `public String getRegion()`
- `public void setRegion(String region)`
- `public String toString()`

### `DetalleVenta` (class)
Atributos:
- `id`: `Integer`
- `idVenta`: `int`
- `codigoProducto`: `String`
- `nombreProducto`: `String`
- `cantidad`: `int`
- `precioUnitario`: `BigDecimal`
- `subtotal`: `BigDecimal`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public int getIdVenta()`
- `public void setIdVenta(int idVenta)`
- `public String getCodigoProducto()`
- `public void setCodigoProducto(String codigoProducto)`
- `public String getNombreProducto()`
- `public void setNombreProducto(String nombreProducto)`
- `public int getCantidad()`
- `public void setCantidad(int cantidad)`
- `public BigDecimal getPrecioUnitario()`
- `public void setPrecioUnitario(BigDecimal precioUnitario)`
- `public BigDecimal getSubtotal()`
- `public void setSubtotal(BigDecimal subtotal)`

### `Donacion` (class)
Tabla bisagra del sistema: une la línea de venta que originó el compromiso social con el lote que finalmente lo entrega.
Atributos:
- `id`: `Integer`
- `idDetalleVenta`: `int`
- `codigoProducto`: `String`
- `nombreProducto`: `String`
- `cantidad`: `int`
- `tipo`: `TipoCompromiso`
- `estado`: `EstadoDonacion`
- `idLote`: `Integer`
- `creadoEn`: `OffsetDateTime`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public int getIdDetalleVenta()`
- `public void setIdDetalleVenta(int idDetalleVenta)`
- `public String getCodigoProducto()`
- `public void setCodigoProducto(String codigoProducto)`
- `public String getNombreProducto()`
- `public void setNombreProducto(String nombreProducto)`
- `public int getCantidad()`
- `public void setCantidad(int cantidad)`
- `public TipoCompromiso getTipo()`
- `public void setTipo(TipoCompromiso tipo)`
- `public EstadoDonacion getEstado()`
- `public void setEstado(EstadoDonacion estado)`
- `public Integer getIdLote()`
- `public void setIdLote(Integer idLote)`
- `public OffsetDateTime getCreadoEn()`
- `public void setCreadoEn(OffsetDateTime creadoEn)`

### `EstadoDonacion` (enum)
Ciclo de vida de una donación (07_normalizacion.sql): EN_ESPERA (la web la registró y la venta aún no está pagada) → PENDIENTE (pago confirmado, lista para un lote) → ASIGNADA → ENTREGADA.
Valores: EN_ESPERA, PENDIENTE, ASIGNADA, ENTREGADA

### `EstadoLote` (enum)
Valores: PENDIENTE, EN_RUTA, ENTREGADO

### `EstadoPedidoProveedor` (enum)
Valores: SOLICITADO, RECIBIDO, ANULADO

### `EstadoStock` (enum)
Clasificación visual del nivel de stock comercial de un producto, usada en el reporte de stock (RF-03). Se determina comparando el stock comercial con el mínimo
Valores: SIN_STOCK("Sin stock", "No quedan unidades disponibles para la venta."), CRITICO("Crítico", "El stock está en el mínimo configurado o por debajo: reponer con urgencia."), PROXIMO_A_AGOTAR("Próximo a crítico", "El stock se acerca al mínimo: pronto pasará a estado crítico si no se repone."), DISPONIBLE("Disponible", "El stock está en un nivel saludable.")
Atributos:
- `etiqueta`: `String`
- `descripcion`: `String`
Métodos públicos:
- `public String getEtiqueta()`
- `public String getDescripcion()`
- `public String toString()`

### `EstadoVenta` (enum)
Valores: PENDIENTE, PAGADO, ANULADO

### `FilaDemandaProducto` (record)
Clasificación de demanda de un producto según su consumo real (salidas comerciales) de los últimos 30 días. SIN_MOVIMIENTO es la señal de "no tiene caso reponer esto ahora", aunque esté bajo su stock mínimo.
Componentes: String codigoProducto, String nombreProducto, int stockComercial, int stockMinimoActual, double consumoPromedioDiario, CategoriaDemanda categoria

### `FilaDonacionRiesgo` (record)
Una donación (compromiso de triple impacto) que lleva más del umbral de días sin avanzar de estado: PENDIENTE sin asignar a un lote, o ASIGNADA a un lote que aún no se despacha. Es lo que conecta el reporte con la razón de ser social de Höség: un compromiso atrasado es una promesa incumplida con una comunidad o una ONG.
Componentes: int idDonacion, String nombreProducto, int cantidad, TipoCompromiso tipo, EstadoDonacion estado, int diasEnRiesgo, String codigoLote, String comunidadNombre

### `FilaHistorialDespacho` (record)
Una fila de la hoja "Historial de despachos" del reporte de impacto (RF-07): un lote entregado a una comunidad de Cusco, con el consolidado de lo que llevaba.
Componentes: String codigoLote, String comunidad, String distrito, String provincia, String ong, String responsable, OffsetDateTime fechaCreacion, OffsetDateTime fechaDespacho, OffsetDateTime fechaEntrega, EstadoLote estado, int donaciones, int prendasAbrigo, int arboles

### `FilaRankingProducto` (record)
Una fila del ranking de más/menos vendidos del dashboard de Ventas y Demanda.
Componentes: String codigoProducto, String nombreProducto, int unidadesVendidas, BigDecimal ingresoGenerado

### `FilaReporteStock` (record)
Fila de solo lectura del reporte de stock (RF-03): se usa tanto para pintar las tablas de ReporteStockJFrame como para las hojas del Excel exportado por ExcelExporter.
Componentes: String codigo, String nombre, String categoria, String talla, BigDecimal precio, int stockComercial, int stockMinimo, EstadoStock estado

### `FilaTrazabilidad` (record)
Una fila de la hoja "Trazabilidad" del reporte de impacto (RF-07): qué venta generó qué donación y en qué lote terminó entregándose. Es la respuesta directa al problema planteado en la sección 1.
Componentes: String comprobanteVenta, OffsetDateTime fechaVenta, String producto, int cantidad, TipoCompromiso tipo, EstadoDonacion estado, String codigoLote, String comunidad, OffsetDateTime fechaEntrega

### `IndicadorVentasPeriodo` (record)
Indicadores de ventas (no financieros/contables) para el dashboard de Ventas y Demanda.
Componentes: int pedidosConfirmados, int unidadesVendidas, BigDecimal montoVendido, BigDecimal ticketPromedio

### `LoteDonacion` (class)
Atributos:
- `id`: `Integer`
- `codigoLote`: `String`
- `idComunidad`: `int`
- `comunidadNombre`: `String`
- `idOng`: `int`
- `ongNombre`: `String`
- `idUsuarioResponsable`: `Integer`
- `fechaCreacion`: `OffsetDateTime`
- `fechaDespacho`: `OffsetDateTime`
- `fechaEntrega`: `OffsetDateTime`
- `estado`: `EstadoLote`
- `observaciones`: `String`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getCodigoLote()`
- `public void setCodigoLote(String codigoLote)`
- `public int getIdComunidad()`
- `public void setIdComunidad(int idComunidad)`
- `public String getComunidadNombre()`
- `public void setComunidadNombre(String comunidadNombre)`
- `public int getIdOng()`
- `public void setIdOng(int idOng)`
- `public String getOngNombre()`
- `public void setOngNombre(String ongNombre)`
- `public Integer getIdUsuarioResponsable()`
- `public void setIdUsuarioResponsable(Integer idUsuarioResponsable)`
- `public OffsetDateTime getFechaCreacion()`
- `public void setFechaCreacion(OffsetDateTime fechaCreacion)`
- `public OffsetDateTime getFechaDespacho()`
- `public void setFechaDespacho(OffsetDateTime fechaDespacho)`
- `public OffsetDateTime getFechaEntrega()`
- `public void setFechaEntrega(OffsetDateTime fechaEntrega)`
- `public EstadoLote getEstado()`
- `public void setEstado(EstadoLote estado)`
- `public String getObservaciones()`
- `public void setObservaciones(String observaciones)`

### `MovimientoInventario` (class)
Bitácora de inventario: ningún stock se modifica sin una fila aquí (RNF-06 / sección 9.3).
Atributos:
- `id`: `Integer`
- `codigoProducto`: `String`
- `tipoStock`: `TipoStock`
- `tipoMovimiento`: `TipoMovimiento`
- `cantidad`: `int`
- `motivo`: `String`
- `idVenta`: `Integer`
- `idLote`: `Integer`
- `idPedidoProveedor`: `Integer`
- `origenSistema`: `OrigenSistema`
- `idUsuario`: `Integer`
- `fecha`: `OffsetDateTime`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getCodigoProducto()`
- `public void setCodigoProducto(String codigoProducto)`
- `public TipoStock getTipoStock()`
- `public void setTipoStock(TipoStock tipoStock)`
- `public TipoMovimiento getTipoMovimiento()`
- `public void setTipoMovimiento(TipoMovimiento tipoMovimiento)`
- `public int getCantidad()`
- `public void setCantidad(int cantidad)`
- `public String getMotivo()`
- `public void setMotivo(String motivo)`
- `public Integer getIdVenta()`
- `public void setIdVenta(Integer idVenta)`
- `public Integer getIdLote()`
- `public void setIdLote(Integer idLote)`
- `public Integer getIdPedidoProveedor()`
- `public void setIdPedidoProveedor(Integer idPedidoProveedor)`
- `public OrigenSistema getOrigenSistema()`
- `public void setOrigenSistema(OrigenSistema origenSistema)`
- `public Integer getIdUsuario()`
- `public void setIdUsuario(Integer idUsuario)`
- `public OffsetDateTime getFecha()`
- `public void setFecha(OffsetDateTime fecha)`

### `Ong` (class)
Atributos:
- `id`: `Integer`
- `nombre`: `String`
- `contacto`: `String`
- `telefono`: `String`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getNombre()`
- `public void setNombre(String nombre)`
- `public String getContacto()`
- `public void setContacto(String contacto)`
- `public String getTelefono()`
- `public void setTelefono(String telefono)`
- `public String toString()`

### `OrigenSistema` (enum)
Valores: ESCRITORIO, WEB

### `OrigenVenta` (enum)
Valores: WEB, PRESENCIAL

### `PedidoProveedor` (class)
Atributos:
- `id`: `Integer`
- `idProveedor`: `int`
- `nombreTaller`: `String`
- `codigoProducto`: `String`
- `nombreProducto`: `String`
- `descripcion`: `String`
- `cantidad`: `int`
- `fecha`: `OffsetDateTime`
- `estado`: `EstadoPedidoProveedor`
- `idUsuario`: `Integer`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public int getIdProveedor()`
- `public void setIdProveedor(int idProveedor)`
- `public String getNombreTaller()`
- `public void setNombreTaller(String nombreTaller)`
- `public String getCodigoProducto()`
- `public void setCodigoProducto(String codigoProducto)`
- `public String getNombreProducto()`
- `public void setNombreProducto(String nombreProducto)`
- `public String getDescripcion()`
- `public void setDescripcion(String descripcion)`
- `public int getCantidad()`
- `public void setCantidad(int cantidad)`
- `public OffsetDateTime getFecha()`
- `public void setFecha(OffsetDateTime fecha)`
- `public EstadoPedidoProveedor getEstado()`
- `public void setEstado(EstadoPedidoProveedor estado)`
- `public Integer getIdUsuario()`
- `public void setIdUsuario(Integer idUsuario)`

### `Producto` (class)
Atributos:
- `codigo`: `String`
- `nombre`: `String`
- `marca`: `String`
- `categoria`: `String`
- `coleccion`: `String`
- `talla`: `String`
- `descripcion`: `String`
- `urlImagen`: `String`
- `precio`: `BigDecimal`
- `stockComercial`: `int`
- `stockComprometido`: `int`
- `stockMinimo`: `int`
- `aplicaTripleImpacto`: `boolean`
- `tipoCompromiso`: `TipoCompromiso`
- `visibleWeb`: `boolean`
- `activo`: `boolean`
- `creadoEn`: `OffsetDateTime`
Métodos públicos:
- `public String getCodigo()`
- `public void setCodigo(String codigo)`
- `public String getNombre()`
- `public void setNombre(String nombre)`
- `public String getMarca()`
- `public void setMarca(String marca)`
- `public String getCategoria()`
- `public void setCategoria(String categoria)`
- `public String getColeccion()`
- `public void setColeccion(String coleccion)`
- `public String getTalla()`
- `public void setTalla(String talla)`
- `public String getDescripcion()`
- `public void setDescripcion(String descripcion)`
- `public String getUrlImagen()`
- `public void setUrlImagen(String urlImagen)`
- `public BigDecimal getPrecio()`
- `public void setPrecio(BigDecimal precio)`
- `public int getStockComercial()`
- `public void setStockComercial(int stockComercial)`
- `public int getStockComprometido()`
- `public void setStockComprometido(int stockComprometido)`
- `public int getStockMinimo()`
- `public void setStockMinimo(int stockMinimo)`
- `public boolean isAplicaTripleImpacto()`
- `public void setAplicaTripleImpacto(boolean aplicaTripleImpacto)`
- `public TipoCompromiso getTipoCompromiso()`
- `public void setTipoCompromiso(TipoCompromiso tipoCompromiso)`
- `public boolean isVisibleWeb()`
- `public void setVisibleWeb(boolean visibleWeb)`
- `public boolean isActivo()`
- `public void setActivo(boolean activo)`
- `public OffsetDateTime getCreadoEn()`
- `public void setCreadoEn(OffsetDateTime creadoEn)`
- `public String toString()`

### `Proveedor` (class)
Atributos:
- `id`: `Integer`
- `nombreTaller`: `String`
- `ruc`: `String`
- `contacto`: `String`
- `telefono`: `String`
- `activo`: `boolean`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getNombreTaller()`
- `public void setNombreTaller(String nombreTaller)`
- `public String getRuc()`
- `public void setRuc(String ruc)`
- `public String getContacto()`
- `public void setContacto(String contacto)`
- `public String getTelefono()`
- `public void setTelefono(String telefono)`
- `public boolean isActivo()`
- `public void setActivo(boolean activo)`
- `public String toString()`

### `ResumenVentas` (record)
Consolidado de ventas web confirmadas (PAGADO) en un periodo, para la comparación "ventas versus donaciones" del reporte de impacto (RF-07). {@code unidadesConCompromiso} cuenta solo prendas y accesorios con aplica_triple_impacto: cada una debió generar una donación.
Componentes: int pedidosConfirmados, int unidadesVendidas, int unidadesConCompromiso, BigDecimal montoVendido

### `RolUsuario` (enum)
Valores: ADMINISTRADOR, ENCARGADO

### `TipoCompromiso` (enum)
Valores: ABRIGO, ARBOL

### `TipoMovimiento` (enum)
Valores: INGRESO, SALIDA, AJUSTE

### `TipoStock` (enum)
Valores: COMERCIAL, COMPROMETIDO

### `Usuario` (class)
Atributos:
- `id`: `Integer`
- `nombre`: `String`
- `username`: `String`
- `passwordHash`: `String`
- `salt`: `String`
- `rol`: `RolUsuario`
- `activo`: `boolean`
- `intentosFallidos`: `int`
- `bloqueadoHasta`: `OffsetDateTime`
- `creadoEn`: `OffsetDateTime`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getNombre()`
- `public void setNombre(String nombre)`
- `public String getUsername()`
- `public void setUsername(String username)`
- `public String getPasswordHash()`
- `public void setPasswordHash(String passwordHash)`
- `public String getSalt()`
- `public void setSalt(String salt)`
- `public RolUsuario getRol()`
- `public void setRol(RolUsuario rol)`
- `public boolean isActivo()`
- `public void setActivo(boolean activo)`
- `public int getIntentosFallidos()`
- `public void setIntentosFallidos(int intentosFallidos)`
- `public OffsetDateTime getBloqueadoHasta()`
- `public void setBloqueadoHasta(OffsetDateTime bloqueadoHasta)`
- `public OffsetDateTime getCreadoEn()`
- `public void setCreadoEn(OffsetDateTime creadoEn)`

### `Venta` (class)
Atributos:
- `id`: `Integer`
- `codigoComprobante`: `String`
- `fecha`: `OffsetDateTime`
- `idCliente`: `Integer`
- `clienteNombre`: `String`
- `clienteTipoDoc`: `String`
- `clienteNumDoc`: `String`
- `idUsuario`: `Integer`
- `origen`: `OrigenVenta`
- `estado`: `EstadoVenta`
- `total`: `BigDecimal`
Métodos públicos:
- `public Integer getId()`
- `public void setId(Integer id)`
- `public String getCodigoComprobante()`
- `public void setCodigoComprobante(String codigoComprobante)`
- `public OffsetDateTime getFecha()`
- `public void setFecha(OffsetDateTime fecha)`
- `public Integer getIdCliente()`
- `public void setIdCliente(Integer idCliente)`
- `public String getClienteNombre()`
- `public void setClienteNombre(String clienteNombre)`
- `public String getClienteTipoDoc()`
- `public void setClienteTipoDoc(String clienteTipoDoc)`
- `public String getClienteNumDoc()`
- `public void setClienteNumDoc(String clienteNumDoc)`
- `public Integer getIdUsuario()`
- `public void setIdUsuario(Integer idUsuario)`
- `public OrigenVenta getOrigen()`
- `public void setOrigen(OrigenVenta origen)`
- `public EstadoVenta getEstado()`
- `public void setEstado(EstadoVenta estado)`
- `public BigDecimal getTotal()`
- `public void setTotal(BigDecimal total)`

## SERVICIOS (paquete `servicio`)

### `AnalisisVentasService`
Dashboard de Ventas y Demanda (valor agregado): indicadores de ventas del periodo, ranking de productos más/menos vendidos y clasificación de demanda real por producto (sección 8, pantalla nueva). Reutiliza el mismo consumo histórico de movimiento_inventario que InventarioService usa para el stock mínimo sugerido, aquí a nivel de todo el catálogo.
- `public IndicadorVentasPeriodo calcularIndicadores(OffsetDateTime desde, OffsetDateTime hasta)`
- `public List<FilaRankingProducto> rankingProductos(OffsetDateTime desde, OffsetDateTime hasta)`
- `public List<FilaDemandaProducto> analizarDemanda()` — Ordenado de mayor a menor consumo: arriba lo de más demanda, al fondo lo SIN_MOVIMIENTO.

### `AuthService`
Autenticación de usuarios internos (RF-01). El contador de intentos fallidos y el bloqueo temporal viven en la base de datos, no en memoria (sección 7), para que sobrevivan a un reinicio de la aplicación.
- `public Usuario autenticar(String username, char[] password)`

### `BackupService`
Respaldo de tablas críticas a CSV (RNF-05). Se dispara desde el menú (solo ADMINISTRADOR) o automáticamente al cerrar la aplicación si pasaron más de 24 horas desde el último respaldo exitoso.
- `public Path ejecutarBackup()`
- `public boolean debeRespaldarAutomaticamente()` — true si nunca se respaldó o si pasaron más de 24 horas desde el último respaldo exitoso.

### `BoletaService`
Emisión de la boleta digital con trazabilidad QR (módulo 5). Regla de negocio: solo se emite boleta de un pedido PAGADO. Un pedido PENDIENTE aún no movió stock ni tiene donaciones definitivas, y uno ANULADO ya no representa una venta. El QR de cada donación enlaza al portal público de consulta con el ID único de la donación, que es lo que permite al cliente verificar el estado de la prenda que financió.
- `public File emitirBoleta(int idVenta)`

### `DashboardService`
Indicadores en vivo y estado de conexión para MenuPrincipalJFrame.
- `public IndicadoresDashboard obtenerIndicadores()`
- `public boolean hayConexion()`

### `DatosBoleta`
Todo lo que necesita el generador de PDF para emitir una boleta con trazabilidad RSU (módulo 5): la venta, sus líneas y las donaciones que generó. {@code urlPortal} es la base del portal público de consulta al que apunta cada QR.
(sin métodos públicos de instancia: ver la firma en el tipo)

### `DespachoService`
Gestión de lotes y destinos de donación (RF-05). El stock_comprometido solo se descuenta cuando un lote pasa a ENTREGADO (sección 9.3); las transiciones de estado siguen el flujo PENDIENTE → EN_RUTA → ENTREGADO.
- `public List<Comunidad> listarComunidades()`
- `public List<Ong> listarOngs()`
- `public List<Donacion> listarDonacionesPendientes()`
- `public List<LoteDonacion> listarLotes()`
- `public List<Donacion> listarDonacionesPorLote(int idLote)`
- `public LoteDonacion crearLote(int idComunidad, int idOng, List<Integer> idsDonacion, int idUsuario)`
- `public void cambiarEstadoLote(int idLote, EstadoLote nuevoEstado, int idUsuario)`

### `IndicadoresDashboard`
DTO de solo lectura con los indicadores en vivo del panel principal (sección 8, pantalla 2).
(sin métodos públicos de instancia: ver la firma en el tipo)

### `InventarioService`
Reglas de negocio del catálogo y del inventario dual (RF-02 / RF-03, sección 9.3). En el flujo normal el stock_comprometido lo mueven PedidoWebService (sube al confirmar un pedido) y DespachoService (baja al entregar un lote). Desde aquí solo se admite un ajuste manual de INGRESO sobre él (p. ej. corrección tras un conteo físico); nunca una reducción, porque ese saldo está bloqueado para las comunidades y no puede "transferirse" de vuelta a la venta comercial.
- `public List<Producto> listarProductos()`
- `public List<String> listarCategorias()`
- `public List<String> listarColecciones()`
- `public void crearProducto(Producto producto)`
- `public void actualizarProducto(Producto producto)`
- `public void desactivarProducto(String codigo)`
- `public void ajustarStock(String codigoProducto, TipoStock tipoStock, int delta, String motivo, int idUsuario)` — Ajuste manual de stock: registra el movimiento de tipo AJUSTE en la misma transacción que el cambio de stock (patrón de la sección 9.2). delta puede ser positivo (ingreso) o negativo (salida), salvo sobre el stock comprometido, donde solo se admite ingreso.

### `PedidoWebService`
Gestión de pedidos provenientes de la web (RF-04). El escritorio no crea ventas ("regla de oro" de la sección 1): solo las consulta, confirma o anula. Confirmar mueve el stock y genera las donaciones pendientes; anular revierte lo que corresponda (secciones 9.1, 9.2 y 9.3). La web registra la donación en estado EN_ESPERA al confirmar el carrito (también la semilla, para simular pedidos recién llegados). Confirmar el pago la pasa a PENDIENTE, y solo crea una nueva si esa línea de detalle todavía no tiene donación.
- `public List<Venta> listarPedidos(EstadoVenta estado, OffsetDateTime desde, OffsetDateTime hasta)`
- `public List<DetalleVenta> listarDetalle(int idVenta)`
- `public List<Donacion> listarDonaciones(int idVenta)`
- `public void confirmarPedido(int idVenta, int idUsuario)`
- `public void anularPedido(int idVenta, int idUsuario)`

### `ProveedorService`
Proveedores y pedidos de reposición (RF-06). Al marcar un pedido como RECIBIDO, suma al stock_comercial del producto asociado y registra el movimiento de INGRESO correspondiente, todo en una sola transacción.
- `public List<Proveedor> listarProveedores()`
- `public List<Producto> listarProductos()`
- `public void crearProveedor(Proveedor proveedor)`
- `public void actualizarProveedor(Proveedor proveedor)`
- `public void desactivarProveedor(int id)`
- `public List<PedidoProveedor> listarPedidos()`
- `public void crearPedido(int idProveedor, String codigoProducto, String descripcion, int cantidad, int idUsuario)` — "Valida que proveedor y cantidad no estén vacíos" (sección 8, pantalla 7).
- `public void marcarRecibido(int idPedido, int idUsuario)`
- `public void anularPedido(int idPedido)`

### `ReporteService`
Consultas para el reporte de impacto (RF-07). La hoja de trazabilidad que arma este servicio es la respuesta directa al problema planteado en la sección 1: qué venta generó qué donación entregada. Dos ejes temporales distintos, a propósito: <ul> <li><b>Fecha de venta</b>: trazabilidad y "ventas versus donaciones" (qué pasó con lo vendido en el periodo).</li> <li><b>Fecha de despacho</b>: historial de despachos e indicadores de impacto (qué se entregó a las comunidades en el periodo).</li> </ul>
- `public List<Comunidad> listarComunidades()`
- `public List<FilaTrazabilidad> generarTrazabilidad(OffsetDateTime desde, OffsetDateTime hasta, Integer idComunidad)`
- `public List<FilaHistorialDespacho> generarHistorialDespachos(OffsetDateTime desde, OffsetDateTime hasta, Integer idComunidad)`
- `public List<Producto> listarInventario()`
- `public ResumenImpacto calcularResumen(OffsetDateTime desde, OffsetDateTime hasta, List<FilaHistorialDespacho> historial)` — Indicadores de impacto a partir del historial de despachos del periodo, más el consolidado de ventas versus donaciones. El filtro de comunidad aplica al historial; las ventas no tienen comunidad hasta que se despachan, así que ese bloque siempre es global al periodo.

### `ReporteStockService`
Clasifica el catálogo activo según su nivel de stock comercial (RF-03) en tres grupos independientes: <ul> <li>{@code sinStock}: no quedan unidades comerciales (stock = 0).</li> <li>{@code criticos}: el stock está en el mínimo configurado o por debajo.</li> <li>{@code disponibles}: el resto (incluye {@code PROXIMO_A_AGOTAR} y {@code DISPONIBLE}).</li> </ul> Tanto "críticos" como "disponibles" se ordenan por el <b>margen</b> sobre el mínimo ({@code stockComercial - stockMinimo}), de menor a mayor — no por el stock comercial en bruto. Así, un producto con margen 4 se prioriza antes que uno con margen 5, aunque este último tenga menos unidades en términos absolutos: lo que importa es qué tan cerca está de su propio mínimo, no la cantidad pura. El margen de "próximo a crítico" ({@link #MARGEN_PROXIMO_A_AGOTAR_UNIDADES}) es puramente interno: no se muestra ningún número en la interfaz, solo el resaltado ámbar y la etiqueta de estado.
- `public ReporteStock generarReporte()`
- `public record ReporteStock(List<FilaReporteStock> sinStock, List<FilaReporteStock> criticos,` — Resultado ya clasificado, en tres grupos independientes.

### `ResumenImpacto`
DTO con los totales de la hoja "Resumen de impacto" del reporte (RF-07). Bloque 1 (impacto en comunidades) se calcula sobre lo ENTREGADO en el periodo (fecha de despacho). Bloque 2 (ventas versus donaciones) se calcula sobre las ventas del periodo (fecha de venta) y muestra en qué estado están las donaciones que esas ventas generaron.
- `public double porcentajeCumplimiento()` — Porcentaje de unidades ya entregadas sobre las generadas en el periodo (0 si no hubo).

### `ServicioException`
Excepción de negocio con mensaje en español listo para mostrar al usuario final. Las excepciones SQL nunca llegan crudas a la vista: se traducen aquí (sección 9.4 de la especificación).
- `public ServicioException(String mensaje)`
- `public ServicioException(String mensaje, Throwable causa)`

### `StockMinimoSugerido`
Resultado del cálculo de stock mínimo sugerido (ver InventarioService). {@code conHistorial} indica si el cálculo usó ventas reales, o si el producto es nuevo / no tiene ventas en la ventana analizada, en cuyo caso se devuelve un valor de referencia fijo en lugar de un cálculo.
(sin métodos públicos de instancia: ver la firma en el tipo)

### `UsuarioService`
Administración de usuarios internos (RF-08). Solo ADMINISTRADOR.
- `public List<Usuario> listarUsuarios()`
- `public void crearUsuario(String nombre, String username, String password, RolUsuario rol)`
- `public void actualizarUsuario(int id, String nombre, RolUsuario rol)`
- `public void desactivarUsuario(int idUsuarioObjetivo, int idUsuarioActual)` — "Un administrador no puede desactivarse a sí mismo" (sección 8, pantalla 8).
- `public void resetearPassword(int id, String nuevaPassword)`

---

## 3. Reglas de negocio implementadas

Cada regla indica la clase donde vive. Los números son los que están en el código, no los de la especificación.

### Triple impacto y donaciones

- **Qué genera compromiso.** Solo los productos con `tipo_compromiso` (ABRIGO o ARBOL) generan donación. `aplica_triple_impacto` es una columna generada: vale `tipo_compromiso IS NOT NULL` y nunca se escribe. (`ProductoDAO`, esquema)
- **Cantidad.** Una donación por línea de venta, con `cantidad` igual a la de la línea y `tipo` igual a `producto.tipo_compromiso`. Una casaca comprada 2 veces = 1 donación de cantidad 2, no 2 donaciones. (`PedidoWebService.confirmarPedido`)
- **Ciclo de vida de la donación:** `EN_ESPERA` (la web la registra con la venta aún sin pagar) → `PENDIENTE` (el encargado confirma el pago) → `ASIGNADA` (entra a un lote) → `ENTREGADA` (su lote pasa a ENTREGADO).
- **Donación sin lote:** solo `EN_ESPERA` y `PENDIENTE` pueden tener `id_lote` NULL. `ASIGNADA` y `ENTREGADA` siempre lo tienen. Lo garantiza un CHECK de la base.
- **Impacto de la web (público).** Abrigos = suma de `cantidad` de donaciones ENTREGADA con tipo ABRIGO; árboles igual con ARBOL; comunidades = número distinto de comunidades con algún lote ENTREGADO; pedidos con impacto = ventas PAGADO con al menos una donación. (`ImpactoService`, web)
- **Cumplimiento del reporte.** `porcentajeCumplimiento = 100 × entregadas / generadas`, y vale 0 si no hubo donaciones. (`ResumenImpacto`)
- **Donaciones en riesgo.** Se listan las donaciones PENDIENTE o ASIGNADA con 15 días o más sin avanzar. Para PENDIENTE el reloj corre desde la creación de la donación; para ASIGNADA, desde la creación del lote. (`DespachoService.DIAS_UMBRAL_RIESGO_DONACION = 15`, `DonacionDAO.listarEnRiesgo`)

### Ventas web (escritorio)

- **Quién crea ventas.** Solo la web. El escritorio solo lista, confirma o anula. (`PedidoWebService`)
- **Confirmar pedido** (solo si está PENDIENTE). En una transacción:
  1. Descuenta `stock_comercial` con `UPDATE ... WHERE stock_comercial >= cantidad`. Si no alcanza, todo se revierte y el mensaje es "Stock comercial insuficiente para …".
  2. Registra un movimiento SALIDA sobre COMERCIAL con `motivo = "Venta web confirmada"` e `id_venta`.
  3. Si la línea tiene compromiso: suma `stock_comprometido` (INGRESO sobre COMPROMETIDO) y crea la donación en PENDIENTE solo si esa línea no tiene ya una.
  4. Pasa las donaciones de EN_ESPERA a PENDIENTE y luego la venta a PAGADO.
- **Anular pedido** (no si ya está ANULADO). Se rechaza si alguna donación ya está ASIGNADA o ENTREGADA. Si estaba PAGADO, devuelve el stock comercial (INGRESO, motivo "Anulación de venta web"); descuenta el comprometido solo si alcanza. Luego elimina las donaciones EN_ESPERA y PENDIENTE de la venta y la pasa a ANULADO.
- **Boleta digital.** Solo se emite para pedidos PAGADO. El QR apunta a `<portal>?donacion=<id>`. (`BoletaService`)

### Stock

- **Stock comercial** baja al confirmar un pedido web y sube al anularlo, al recibir un pedido de proveedor o con un ajuste manual de INGRESO.
- **Stock comprometido** sube al confirmar un pedido con compromiso. Baja **solo** cuando un lote pasa a ENTREGADO, por cada donación del lote.
- **Protección del comprometido.** Un ajuste manual sobre el comprometido solo puede ser de INGRESO. Una reducción manual se rechaza con "El stock comprometido no puede reducirse manualmente". (`InventarioService.validarProteccionStockComprometido`)
- **Ajuste manual.** No puede ser cero. Un ajuste negativo sobre el comercial descuenta solo si hay stock suficiente. Se registra como movimiento AJUSTE en la misma transacción. (`InventarioService.ajustarStock`)
- **Bitácora.** Cada cambio de stock deja una fila en `movimiento_inventario`. Un movimiento apunta como máximo a un origen (venta, lote o pedido de proveedor), según el CHECK de la base.
- **Consumo promedio diario.** Salidas comerciales de los últimos 30 días ÷ 30. Si no hay salidas, es 0. (`MovimientoDAO.consumoPromedioDiario`)
- **Stock mínimo sugerido.** `ceil(consumo × 7 días de reposición × 1,3 de seguridad)`. Sin historial (consumo 0), devuelve 5 con `conHistorial = false`. (`InventarioService.calcularStockMinimoSugerido`)
- **Alerta de stock bajo.** Producto activo con `stock_comercial <= stock_minimo`. (`ProductoDAO.contarBajoStockMinimo`)
- **Clasificación del reporte de stock** (en este orden):
  - `SIN_STOCK`: stock comercial ≤ 0.
  - `CRITICO`: stock comercial ≤ stock mínimo.
  - `PROXIMO_A_AGOTAR`: stock comercial ≤ stock mínimo + 5.
  - `DISPONIBLE`: el resto.
  
  Dentro de cada grupo se ordena por margen (stock comercial − stock mínimo), de menor a mayor. (`ReporteStockService`)
- **Demanda.** Consumo promedio de los últimos 30 días: ≥ 0,5 u/día = ALTA; mayor que 0 = NORMAL; 0 = SIN_MOVIMIENTO. Se ordena de mayor a menor consumo. (`AnalisisVentasService`)

### Lotes de donación

- **Crear lote.** Requiere al menos una donación. Todas las donaciones elegidas deben seguir PENDIENTE; si alguna ya no lo está, se revierte todo. El lote nace PENDIENTE. (`DespachoService.crearLote`)
- **Transiciones permitidas:** PENDIENTE → EN_RUTA → ENTREGADO. ENTREGADO es final. Cualquier otro cambio se rechaza con "No se puede pasar el lote de X a Y". (`DespachoService.validarTransicion`)
- **Pasar a EN_RUTA.** Registra `fecha_despacho`.
- **Pasar a ENTREGADO.** Por cada donación del lote descuenta el stock comprometido (si no alcanza, todo se revierte), registra un movimiento SALIDA sobre COMPROMETIDO con `motivo = "Entrega de lote de donación"` e `id_lote`, pasa las donaciones a ENTREGADA y registra `fecha_entrega`.
- **Historial de despachos (reporte).** Solo lotes ENTREGADO, filtrados por `fecha_entrega`.

### Ventas versus donaciones (reporte de impacto)

- **Trazabilidad.** Se filtra por fecha de **venta**: muestra también donaciones aún PENDIENTE o ASIGNADA.
- **Historial de despachos.** Se filtra por fecha de **entrega** y solo incluye lotes ENTREGADO.
- Ambos ejes son distintos a propósito: lo vendido en un periodo y lo entregado en un periodo no son el mismo conjunto. (`ReporteService`)

### Usuarios y acceso (escritorio)

- **Contraseñas.** SHA-256 en hexadecimal sobre contraseña + salt de 16 bytes. Está en `usuario.password_hash` y `usuario.salt`. (`HashUtil`). Los clientes de la web usan BCrypt; son tablas distintas.
- **Login.** Un usuario inactivo no entra. Cada intento fallido suma 1; al llegar a 3 bloquea la cuenta 5 minutos (`bloqueado_hasta`, calculado con `now()` de la base). Un login correcto reinicia el contador.
- **Gestión de usuarios.** Solo ADMINISTRADOR. Nombre, usuario y contraseña son obligatorios; el usuario es único. Nadie puede desactivarse a sí mismo. (`UsuarioService`)

### Proveedores

- **Crear pedido.** Exige un proveedor válido (ID > 0) y los campos obligatorios.
- **Marcar RECIBIDO.** Solo desde SOLICITADO, y exige que el pedido tenga un producto de catálogo asociado. Suma `cantidad` al stock comercial y registra un INGRESO con `motivo = "Recepción de pedido a proveedor"` e `id_pedido_proveedor`, todo en una transacción. (`ProveedorService`)
- **Anular pedido.** Solo desde SOLICITADO.

### Productos (escritorio)

- **Validación al guardar.** Código, nombre y categoría son obligatorios. El precio no puede ser negativo. (`InventarioService.validarProducto`)
- **Categoría y colección** solo pueden ser valores de las tablas `categoria` y `coleccion`. La colección puede quedar vacía.
- **Baja de producto.** Es lógica: `activo = false`. No se borra.

### Respaldo

- **Respaldo automático.** Al cerrar la aplicación, si pasaron más de 24 horas desde el último respaldo exitoso. (`BackupService`)

### Portal web (`hoseg-web`)

- **Catálogo público.** Solo productos con `activo = true` y `visible_web = true`, agrupados por nombre: una tarjeta por producto con todas sus tallas. (`CatalogoService.tarjetas`)
- **Carrusel de la Home.** Máximo 8. Se intercalan productos ABRIGO y el resto (no hay columna `destacado` en el esquema). (`CatalogoService.destacados`)
- **Carrito.** Vive en la sesión HTTP, no en la base. Agregar un código que ya está suma la cantidad. El badge muestra el total de unidades. (`CarritoService`)
- **Acceso.** Públicas: Home, tienda, carrito, consulta de impacto, login, registro, Café, páginas institucionales, boletín. Requieren sesión: `/carrito/confirmar` y `/cuenta/**`. (`SecurityConfig`)
- **Pendiente en la web.** Todavía no existen el checkout (`VentaService`), el registro, el login de clientes, la página de cuenta, la tienda ni un controlador de carrito. Hoy solo hay controlador para la Home; `CarritoService` existe como servicio de sesión, sin rutas que lo usen.
