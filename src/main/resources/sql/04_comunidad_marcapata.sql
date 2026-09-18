-- =====================================================================
-- SEGITD-HÖSÉG · Comunidad Marcapata
-- Script incremental para una base ya cargada con 02_datos_prueba.sql
-- antes de que Marcapata se agregara a la semilla. Es idempotente: se
-- puede ejecutar varias veces sin duplicar la fila.
-- =====================================================================

INSERT INTO comunidad (nombre, distrito, provincia, region)
SELECT 'Marcapata', 'Marcapata', 'Quispicanchi', 'Cusco'
 WHERE NOT EXISTS (SELECT 1 FROM comunidad WHERE nombre = 'Marcapata');
