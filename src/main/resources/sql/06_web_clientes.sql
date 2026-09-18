-- =====================================================================
-- SEGITD-HÖSÉG · Soporte para el portal web e-commerce
-- Lo que la web (Spring Boot) necesita del esquema compartido y que el
-- escritorio no usa: autenticación de clientes, correlativo de boletas
-- WEB y un índice para la consulta pública de impacto.
--
-- Idempotente: se puede ejecutar varias veces sobre una base ya cargada
-- con 01..05. No toca ninguna columna que lea el escritorio.
-- =====================================================================

-- ---------- Clientes: registro e inicio de sesión ----------------------
-- password_hash guarda BCrypt (60 caracteres); nunca texto plano.
-- Los clientes cargados por 02/03 quedan con password_hash NULL: existen
-- como compradores históricos pero no pueden iniciar sesión hasta que se
-- registren con su mismo email.
ALTER TABLE cliente
    ADD COLUMN IF NOT EXISTS apellido      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS direccion     VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(72);

-- El email pasa a ser la credencial de acceso: obligatorio y único.
-- (La semilla de 02/03 ya cumple ambas condiciones.)
ALTER TABLE cliente
    ALTER COLUMN email SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_cliente_email ON cliente (lower(email));

-- ---------- Correlativo de comprobantes WEB ----------------------------
-- La web genera 'WEB-' || lpad(nextval(...), 6, '0'). Usar una secuencia
-- evita el patrón leer-máximo-sumar-uno (prohibido en la sección 9.1)
-- cuando dos clientes confirman compra a la vez.
-- Arranca después del último comprobante WEB-nnnnnn ya cargado por la
-- semilla, para no colisionar con la restricción UNIQUE de venta.
CREATE SEQUENCE IF NOT EXISTS seq_comprobante_web MINVALUE 0 START WITH 0;

SELECT setval(
    'seq_comprobante_web',
    GREATEST(
        (SELECT COALESCE(MAX(substring(codigo_comprobante FROM '^WEB-(\d+)$')::integer), 0)
           FROM venta
          WHERE codigo_comprobante ~ '^WEB-\d+$'),
        (SELECT last_value FROM seq_comprobante_web)
    ),
    true
);

-- ---------- Consulta pública de impacto --------------------------------
-- El portal busca por boleta: venta → detalle_venta → donacion.
CREATE INDEX IF NOT EXISTS idx_donacion_detalle ON donacion (id_detalle_venta);

-- ---------- Verificación (solo lectura) --------------------------------
-- Debe devolver una fila por columna nueva: apellido, direccion, password_hash.
SELECT column_name, data_type, character_maximum_length
  FROM information_schema.columns
 WHERE table_name = 'cliente'
   AND column_name IN ('apellido', 'direccion', 'password_hash')
 ORDER BY column_name;

-- El siguiente comprobante que emitirá la web (sin consumir la secuencia).
SELECT 'WEB-' || lpad((last_value + 1)::text, 6, '0') AS siguiente_comprobante
  FROM seq_comprobante_web;
