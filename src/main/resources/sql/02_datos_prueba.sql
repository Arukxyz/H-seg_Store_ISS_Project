-- =====================================================================
-- SEGITD-HÖSÉG · Datos de prueba
-- Ejecutar una sola vez, sobre una base vacía, inmediatamente después
-- de 01_schema.sql.
-- =====================================================================

-- ---------- Usuarios internos -------------------------------------------
-- Hash y salt generados con la utilidad util.GeneradorHash (sección 7 de
-- la especificación) — ningún hash se escribe a mano.
-- Login de prueba: admin / Admin#2026  ·  encargado / Encargado#2026
INSERT INTO usuario (nombre, username, password_hash, salt, rol) VALUES
    ('Administrador General', 'admin',     '3dec1f94ec26fc37269cbce15511ed51ca268ff46d5e0da9ebaa88f3344ecf3c', '105fcbe92d91615124787a57e50b728b', 'ADMINISTRADOR'),
    ('Encargado de Almacén',  'encargado', 'd7ee60cf804514edcce056e94a746ecf6edcbf72a4bbcfe92204daf871237264', 'b121f545f8f72c864ae3b41979be29ad', 'ENCARGADO');

-- ---------- Catálogo: colección Höség (casacas Älpafill + accesorios) ---
INSERT INTO producto
    (codigo, nombre, marca, categoria, coleccion, talla, descripcion, precio,
     stock_comercial, stock_minimo, aplica_triple_impacto, tipo_compromiso, visible_web, activo)
VALUES
    ('HSG-CAS-001', 'Casaca Älpafill Cusco', 'Höség', 'Casacas',    'Älpafill',  'M',  'Casaca de abrigo con relleno sintético Älpafill, impermeable.',       259.90, 30, 5, TRUE, 'ABRIGO', TRUE, TRUE),
    ('HSG-CAS-002', 'Casaca Älpafill Cusco', 'Höség', 'Casacas',    'Älpafill',  'L',  'Casaca de abrigo con relleno sintético Älpafill, impermeable.',       259.90, 25, 5, TRUE, 'ABRIGO', TRUE, TRUE),
    ('HSG-CAS-003', 'Chaleco Älpafill',      'Höség', 'Casacas',    'Älpafill',  'S',  'Chaleco acolchado, capa intermedia de la línea Älpafill.',            219.90, 20, 5, TRUE, 'ABRIGO', TRUE, TRUE),
    ('HSG-ACC-001', 'Gorro de lana Layo',    'Höség', 'Accesorios', 'Comunidad', NULL, 'Gorro tejido a mano por artesanas de la comunidad de Layo.',           49.90, 40, 8, TRUE, 'ARBOL',  TRUE, TRUE),
    ('HSG-ACC-002', 'Bufanda Paucartambo',   'Höség', 'Accesorios', 'Comunidad', NULL, 'Bufanda de lana de oveja, tejido tradicional cusqueño.',                39.90, 35, 8, TRUE, 'ARBOL',  TRUE, TRUE);

-- ---------- ONG aliada ----------------------------------------------------
INSERT INTO ong (nombre, contacto, telefono) VALUES
    ('Pachamama Raymi', 'Rocío Injante', '+51 984 123 456');

-- ---------- Comunidades altoandinas de Cusco -----------------------------
INSERT INTO comunidad (nombre, distrito, provincia, region) VALUES
    ('Omacha',      'Omacha',      'Paruro',       'Cusco'),
    ('Ccatca',      'Ccatca',      'Quispicanchi', 'Cusco'),
    ('Paucartambo', 'Paucartambo', 'Paucartambo',  'Cusco'),
    ('Layo',        'Layo',        'Canas',        'Cusco');

-- ---------- Proveedores (talleres textiles) -------------------------------
INSERT INTO proveedor (nombre_taller, ruc, contacto, telefono) VALUES
    ('Textiles Ausangate S.A.C.',    '20456789123', 'Marco Quispe', '+51 984 555 111'),
    ('Confecciones Andina E.I.R.L.', '20489321456', 'Lucía Ttito',  '+51 984 555 222');

-- ---------- Clientes del e-commerce ---------------------------------------
INSERT INTO cliente (nombre, tipo_doc, num_doc, email, telefono) VALUES
    ('María Fernanda Gutiérrez', 'DNI', '45678912', 'maria.gutierrez@example.com', '+51 987 111 222'),
    ('Jorge Luis Ramírez',       'DNI', '41234567', 'jorge.ramirez@example.com',   '+51 987 222 333'),
    ('Ana Belén Torres',         'DNI', '47812365', 'ana.torres@example.com',      '+51 987 333 444');

-- ---------- 3 ventas WEB con detalle y donaciones PENDIENTE --------------
-- Simulan pedidos ya recibidos desde el portal, para poder demostrar el
-- flujo (RF-04) sin depender de que la web esté levantada.
-- Se usan CTEs con RETURNING en vez de IDs fijos, para no asumir en qué
-- número arrancan los SERIAL.

-- Venta 1: 1 casaca -> 1 donación ABRIGO
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, origen, estado, total)
    SELECT 'WEB-000101', c.id, 'WEB', 'PENDIENTE', 259.90
    FROM cliente c WHERE c.num_doc = '45678912'
    RETURNING id
), dv AS (
    INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
    SELECT v.id, 'HSG-CAS-001', 1, 259.90, 259.90 FROM v
    RETURNING id, codigo_producto
)
INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado)
SELECT dv.id, dv.codigo_producto, 1, 'ABRIGO', 'PENDIENTE' FROM dv;

-- Venta 2: 2 gorros -> 1 donación ARBOL (cantidad 2)
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, origen, estado, total)
    SELECT 'WEB-000102', c.id, 'WEB', 'PENDIENTE', 99.80
    FROM cliente c WHERE c.num_doc = '41234567'
    RETURNING id
), dv AS (
    INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
    SELECT v.id, 'HSG-ACC-001', 2, 49.90, 99.80 FROM v
    RETURNING id, codigo_producto
)
INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado)
SELECT dv.id, dv.codigo_producto, 2, 'ARBOL', 'PENDIENTE' FROM dv;

-- Venta 3: 1 chaleco + 1 bufanda -> 1 donación ABRIGO + 1 donación ARBOL
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, origen, estado, total)
    SELECT 'WEB-000103', c.id, 'WEB', 'PENDIENTE', 259.80
    FROM cliente c WHERE c.num_doc = '47812365'
    RETURNING id
), dv AS (
    INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
    SELECT v.id, 'HSG-CAS-003', 1, 219.90, 219.90 FROM v
    UNION ALL
    SELECT v.id, 'HSG-ACC-002', 1, 39.90, 39.90 FROM v
    RETURNING id, codigo_producto
)
INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado)
SELECT dv.id, dv.codigo_producto, 1,
       CASE dv.codigo_producto WHEN 'HSG-CAS-003' THEN 'ABRIGO' ELSE 'ARBOL' END,
       'PENDIENTE'
FROM dv;
