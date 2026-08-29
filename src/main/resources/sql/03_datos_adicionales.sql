-- =====================================================================
-- SEGITD-HÖSÉG · Datos adicionales de demostración
-- Ejecutar después de 01_schema.sql y 02_datos_prueba.sql, sobre la
-- base ya cargada (no se puede correr dos veces sin limpiar antes,
-- igual que 02_datos_prueba.sql, por los códigos únicos).
--
-- Agrega más productos, proveedores, clientes y pedidos web en
-- distintos estados (PENDIENTE, PAGADO, ANULADO) con donaciones en
-- distintas etapas (PENDIENTE, ASIGNADA, ENTREGADA), para que
-- Despacho y Reportes de Impacto también tengan datos que mostrar
-- sin tener que recorrer todo el flujo a mano primero.
-- =====================================================================

-- ---------- Más productos -------------------------------------------------
-- HSG-CAS-006 y HSG-ACC-004 arrancan bajo su stock mínimo a propósito,
-- para ver el resaltado en rojo de GestionProductosJFrame de inmediato.
INSERT INTO producto
    (codigo, nombre, marca, categoria, coleccion, talla, descripcion, precio,
     stock_comercial, stock_minimo, aplica_triple_impacto, tipo_compromiso, visible_web, activo)
VALUES
    ('HSG-CAS-004', 'Casaca Älpafill Cusco',      'Höség', 'Casacas',    'Älpafill',  'XL',    'Casaca de abrigo con relleno sintético Älpafill, impermeable.',   259.90, 18, 5,  TRUE,  'ABRIGO', TRUE, TRUE),
    ('HSG-CAS-005', 'Poncho Älpafill',             'Höség', 'Casacas',    'Älpafill',  'Única', 'Poncho impermeable de doble capa, ideal para lluvia intensa.',     189.90, 22, 5,  TRUE,  'ABRIGO', TRUE, TRUE),
    ('HSG-CAS-006', 'Chompa Älpafill',             'Höség', 'Casacas',    'Älpafill',  'M',     'Chompa tejida con relleno térmico ligero.',                        149.90, 4,  6,  TRUE,  'ABRIGO', TRUE, TRUE),
    ('HSG-CAS-007', 'Cortavientos Älpafill',       'Höség', 'Casacas',    'Älpafill',  'L',     'Cortavientos ultraligero, plegable.',                              129.90, 27, 5,  TRUE,  'ABRIGO', TRUE, TRUE),
    ('HSG-ACC-003', 'Guantes de lana Omacha',      'Höség', 'Accesorios', 'Comunidad', 'Única', 'Guantes tejidos a mano por artesanas de Omacha.',                   34.90, 30, 8,  TRUE,  'ARBOL',  TRUE, TRUE),
    ('HSG-ACC-004', 'Chalina Ccatca',              'Höség', 'Accesorios', 'Comunidad', 'Única', 'Chalina de lana de alpaca, tejido tradicional de Ccatca.',          44.90, 3,  8,  TRUE,  'ARBOL',  TRUE, TRUE),
    ('HSG-ACC-005', 'Chullo andino',               'Höség', 'Accesorios', 'Comunidad', 'Única', 'Chullo tradicional con orejeras, tejido a mano.',                   29.90, 45, 10, TRUE,  'ARBOL',  TRUE, TRUE),
    ('HSG-ACC-006', 'Medias de alpaca',            'Höség', 'Accesorios', 'Comunidad', 'Única', 'Medias térmicas de fibra de alpaca.',                               19.90, 60, 10, TRUE,  'ARBOL',  TRUE, TRUE),
    ('HSG-ACC-007', 'Mochila artesanal Höség',     'Höség', 'Accesorios', 'Urbana',    'Única', 'Mochila de algodón orgánico, línea urbana sin compromiso social.',  79.90, 15, 5,  FALSE, NULL,     TRUE, TRUE),
    ('HSG-ACC-008', 'Botella reutilizable Höség',  'Höség', 'Accesorios', 'Urbana',    'Única', 'Botella de acero inoxidable, línea urbana sin compromiso social.',  39.90, 40, 10, FALSE, NULL,     TRUE, TRUE);

-- ---------- Más proveedores -------------------------------------------------
INSERT INTO proveedor (nombre_taller, ruc, contacto, telefono) VALUES
    ('Manos de Ausangate E.I.R.L.', '20512345678', 'Rosa Huamán',    '+51 984 555 333'),
    ('Textiles Vilcanota S.A.C.',   '20498765432', 'Edwin Ccahuana', '+51 984 555 444');

-- ---------- Más clientes -----------------------------------------------------
INSERT INTO cliente (nombre, tipo_doc, num_doc, email, telefono) VALUES
    ('Camila Rodríguez Paredes',    'DNI', '48912345', 'camila.rodriguez@example.com',   '+51 987 444 555'),
    ('Diego Alonso Vega',           'DNI', '46123789', 'diego.vega@example.com',         '+51 987 555 666'),
    ('Valeria Cusihuamán Quispe',   'DNI', '47234891', 'valeria.cusihuaman@example.com', '+51 987 666 777'),
    ('Sebastián Flores Mamani',     'DNI', '45678234', 'sebastian.flores@example.com',   '+51 987 777 888');

-- ---------- Venta 4: PENDIENTE, sin confirmar (aún sin donación) -----------
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, origen, estado, total)
    SELECT 'WEB-000104', c.id, 'WEB', 'PENDIENTE', 259.90
    FROM cliente c WHERE c.num_doc = '48912345'
    RETURNING id
)
INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
SELECT v.id, 'HSG-CAS-004', 1, 259.90, 259.90 FROM v;

-- ---------- Venta 5: PENDIENTE, dos líneas, sin confirmar -------------------
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, origen, estado, total)
    SELECT 'WEB-000105', c.id, 'WEB', 'PENDIENTE', 259.70
    FROM cliente c WHERE c.num_doc = '46123789'
    RETURNING id
)
INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
SELECT v.id, 'HSG-CAS-005', 1, 189.90, 189.90 FROM v
UNION ALL
SELECT v.id, 'HSG-ACC-003', 2, 34.90, 69.80 FROM v;

-- ---------- Venta 6: PAGADO, donación PENDIENTE (lista para despachar) -----
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, id_usuario, origen, estado, total)
    SELECT 'WEB-000106', c.id, u.id, 'WEB', 'PAGADO', 89.70
    FROM cliente c, usuario u
    WHERE c.num_doc = '47234891' AND u.username = 'admin'
    RETURNING id
)
INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
SELECT v.id, 'HSG-ACC-005', 3, 29.90, 89.70 FROM v;

UPDATE producto SET stock_comercial = stock_comercial - 3, stock_comprometido = stock_comprometido + 3
 WHERE codigo = 'HSG-ACC-005';

INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-ACC-005', 'COMERCIAL', 'SALIDA', -3, 'VENTA:' || v.id::text, 'ESCRITORIO', u.id
FROM venta v, usuario u WHERE v.codigo_comprobante = 'WEB-000106' AND u.username = 'admin';

INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-ACC-005', 'COMPROMETIDO', 'INGRESO', 3, 'VENTA:' || v.id::text, 'ESCRITORIO', u.id
FROM venta v, usuario u WHERE v.codigo_comprobante = 'WEB-000106' AND u.username = 'admin';

INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado)
SELECT dv.id, dv.codigo_producto, dv.cantidad, 'ARBOL', 'PENDIENTE'
FROM detalle_venta dv JOIN venta v ON v.id = dv.id_venta
WHERE v.codigo_comprobante = 'WEB-000106';

-- ---------- Venta 7: PAGADO, donación ASIGNADA a un lote EN_RUTA -----------
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, id_usuario, origen, estado, total)
    SELECT 'WEB-000107', c.id, u.id, 'WEB', 'PAGADO', 79.60
    FROM cliente c, usuario u
    WHERE c.num_doc = '45678234' AND u.username = 'admin'
    RETURNING id
)
INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
SELECT v.id, 'HSG-ACC-006', 4, 19.90, 79.60 FROM v;

UPDATE producto SET stock_comercial = stock_comercial - 4, stock_comprometido = stock_comprometido + 4
 WHERE codigo = 'HSG-ACC-006';

INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-ACC-006', 'COMERCIAL', 'SALIDA', -4, 'VENTA:' || v.id::text, 'ESCRITORIO', u.id
FROM venta v, usuario u WHERE v.codigo_comprobante = 'WEB-000107' AND u.username = 'admin';

INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-ACC-006', 'COMPROMETIDO', 'INGRESO', 4, 'VENTA:' || v.id::text, 'ESCRITORIO', u.id
FROM venta v, usuario u WHERE v.codigo_comprobante = 'WEB-000107' AND u.username = 'admin';

-- el código de lote se genera igual que LoteDAO.crear en Java: a partir
-- de la misma secuencia del id, para que sea correlativo de verdad.
WITH id_gen AS (
    SELECT nextval(pg_get_serial_sequence('lote_donacion', 'id')) AS id
),
nuevo_lote AS (
    INSERT INTO lote_donacion (id, codigo_lote, id_comunidad, id_ong, id_usuario_responsable, estado)
    SELECT id_gen.id, 'HSG-L' || lpad(id_gen.id::text, 3, '0'), com.id, o.id, u.id, 'EN_RUTA'
    FROM id_gen, comunidad com, ong o, usuario u
    WHERE com.nombre = 'Ccatca' AND o.nombre = 'Pachamama Raymi' AND u.username = 'admin'
    RETURNING id
)
INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado, id_lote)
SELECT dv.id, dv.codigo_producto, dv.cantidad, 'ARBOL', 'ASIGNADA', nuevo_lote.id
FROM detalle_venta dv
JOIN venta v ON v.id = dv.id_venta
CROSS JOIN nuevo_lote
WHERE v.codigo_comprobante = 'WEB-000107';

-- ---------- Venta 8: PAGADO, donación ya ENTREGADA (impacto real) ----------
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, id_usuario, origen, estado, total)
    SELECT 'WEB-000108', c.id, u.id, 'WEB', 'PAGADO', 299.80
    FROM cliente c, usuario u
    WHERE c.num_doc = '48912345' AND u.username = 'admin'
    RETURNING id
)
INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
SELECT v.id, 'HSG-CAS-006', 2, 149.90, 299.80 FROM v;

UPDATE producto SET stock_comercial = stock_comercial - 2, stock_comprometido = stock_comprometido + 2
 WHERE codigo = 'HSG-CAS-006';

INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-CAS-006', 'COMERCIAL', 'SALIDA', -2, 'VENTA:' || v.id::text, 'ESCRITORIO', u.id
FROM venta v, usuario u WHERE v.codigo_comprobante = 'WEB-000108' AND u.username = 'admin';

INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-CAS-006', 'COMPROMETIDO', 'INGRESO', 2, 'VENTA:' || v.id::text, 'ESCRITORIO', u.id
FROM venta v, usuario u WHERE v.codigo_comprobante = 'WEB-000108' AND u.username = 'admin';

WITH id_gen AS (
    SELECT nextval(pg_get_serial_sequence('lote_donacion', 'id')) AS id
),
nuevo_lote AS (
    INSERT INTO lote_donacion (id, codigo_lote, id_comunidad, id_ong, id_usuario_responsable, estado, fecha_despacho)
    SELECT id_gen.id, 'HSG-L' || lpad(id_gen.id::text, 3, '0'), com.id, o.id, u.id, 'ENTREGADO', now() - interval '2 days'
    FROM id_gen, comunidad com, ong o, usuario u
    WHERE com.nombre = 'Paucartambo' AND o.nombre = 'Pachamama Raymi' AND u.username = 'admin'
    RETURNING id, codigo_lote
)
INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado, id_lote)
SELECT dv.id, dv.codigo_producto, dv.cantidad, 'ABRIGO', 'ENTREGADA', nuevo_lote.id
FROM detalle_venta dv
JOIN venta v ON v.id = dv.id_venta
CROSS JOIN nuevo_lote
WHERE v.codigo_comprobante = 'WEB-000108';

-- descuenta el comprometido al "entregar" y registra el movimiento,
-- igual que hace DespachoService al pasar un lote a ENTREGADO.
UPDATE producto SET stock_comprometido = stock_comprometido - 2 WHERE codigo = 'HSG-CAS-006';

WITH lote_venta8 AS (
    SELECT l.codigo_lote
      FROM lote_donacion l
      JOIN donacion d ON d.id_lote = l.id
      JOIN detalle_venta dv ON dv.id = d.id_detalle_venta
      JOIN venta v ON v.id = dv.id_venta
     WHERE v.codigo_comprobante = 'WEB-000108'
)
INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-CAS-006', 'COMPROMETIDO', 'SALIDA', -2, 'LOTE:' || lote_venta8.codigo_lote, 'ESCRITORIO', u.id
FROM lote_venta8, usuario u WHERE u.username = 'admin';

-- ---------- Venta 9: ANULADO (rechazada antes de confirmar) ----------------
WITH v AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, id_usuario, origen, estado, total)
    SELECT 'WEB-000109', c.id, u.id, 'WEB', 'ANULADO', 79.90
    FROM cliente c, usuario u
    WHERE c.num_doc = '46123789' AND u.username = 'admin'
    RETURNING id
)
INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
SELECT v.id, 'HSG-ACC-007', 1, 79.90, 79.90 FROM v;

-- ---------- Pedidos a proveedores en distintos estados ----------------------
-- SOLICITADO
INSERT INTO pedido_proveedor (id_proveedor, codigo_producto, descripcion, cantidad, estado, id_usuario)
SELECT p.id, 'HSG-CAS-004', 'Reposición de casacas talla XL para temporada de heladas', 20, 'SOLICITADO', u.id
FROM proveedor p, usuario u WHERE p.nombre_taller = 'Manos de Ausangate E.I.R.L.' AND u.username = 'admin';

-- ANULADO
INSERT INTO pedido_proveedor (id_proveedor, codigo_producto, descripcion, cantidad, estado, id_usuario)
SELECT p.id, 'HSG-ACC-004', 'Pedido duplicado, se anula', 15, 'ANULADO', u.id
FROM proveedor p, usuario u WHERE p.nombre_taller = 'Textiles Ausangate S.A.C.' AND u.username = 'admin';

-- RECIBIDO (ya reflejado en el stock, como haría ProveedorService.marcarRecibido)
WITH nuevo_pedido AS (
    INSERT INTO pedido_proveedor (id_proveedor, codigo_producto, descripcion, cantidad, estado, id_usuario)
    SELECT p.id, 'HSG-ACC-006', 'Reposición de medias de alpaca', 30, 'RECIBIDO', u.id
    FROM proveedor p, usuario u WHERE p.nombre_taller = 'Textiles Vilcanota S.A.C.' AND u.username = 'admin'
    RETURNING id
)
INSERT INTO movimiento_inventario (codigo_producto, tipo_stock, tipo_movimiento, cantidad, referencia, origen_sistema, id_usuario)
SELECT 'HSG-ACC-006', 'COMERCIAL', 'INGRESO', 30, 'PEDIDO_PROV:' || nuevo_pedido.id::text, 'ESCRITORIO', u.id
FROM nuevo_pedido, usuario u WHERE u.username = 'admin';

UPDATE producto SET stock_comercial = stock_comercial + 30 WHERE codigo = 'HSG-ACC-006';
