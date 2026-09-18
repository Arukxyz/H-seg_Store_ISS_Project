-- =====================================================================
-- SEGITD-HÖSÉG · Corrección de fechas de lotes de demostración
-- 03_datos_adicionales.sql crea un lote ENTREGADO con
-- fecha_despacho = now() - 2 días, pero fecha_creacion toma el DEFAULT
-- now(), así que el lote queda "entregado antes de crearse" y se nota
-- en la hoja Historial de despachos del reporte.
--
-- Idempotente: solo toca lotes cuya entrega sea anterior a su creación
-- y los deja con 2 días de logística, coherente con el resto del seed.
-- =====================================================================

UPDATE lote_donacion
   SET fecha_creacion = fecha_despacho - interval '2 days'
 WHERE fecha_despacho IS NOT NULL
   AND fecha_despacho < fecha_creacion;

-- ---------- Verificación (solo lectura) ----------------------------------
-- Debe devolver 0 filas: ningún lote entregado antes de ser creado.
SELECT codigo_lote, fecha_creacion, fecha_despacho
  FROM lote_donacion
 WHERE fecha_despacho < fecha_creacion;

-- Debe devolver 0 filas: el stock comprometido de cada producto coincide
-- con la suma de sus donaciones aún no entregadas (regla 9.3).
SELECT p.codigo, p.stock_comprometido,
       COALESCE(SUM(d.cantidad) FILTER (WHERE d.estado IN ('PENDIENTE', 'ASIGNADA')), 0) AS esperado
  FROM producto p
  LEFT JOIN donacion d ON d.codigo_producto = p.codigo
 GROUP BY p.codigo, p.stock_comprometido
HAVING p.stock_comprometido <> COALESCE(SUM(d.cantidad) FILTER (WHERE d.estado IN ('PENDIENTE', 'ASIGNADA')), 0);
