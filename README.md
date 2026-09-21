# SEGITD-HÖSÉG — Módulo de Escritorio

Back-office administrativo de Höség Store (14-DIEZ S.A.C.): inventario dual, pedidos web, lotes de donación, despacho a comunidades de Cusco, reportes de impacto, boleta digital con QR, proveedores y usuarios.

Java 25 + Swing (FlatLaf) + JDBC, conectado a PostgreSQL en Supabase Cloud. Ver [SEGITD-HOSEG.md](SEGITD-HOSEG.md) para la especificación completa.

## Requisitos

- JDK 25
- Maven 3.9+
- Un proyecto de Supabase con los scripts SQL ejecutados (ver abajo)

## Base de datos

Ejecutar en el SQL Editor de Supabase, en este orden:

| Script | Qué hace |
|---|---|
| `src/main/resources/sql/01_schema.sql` | Esquema (tablas, restricciones, índices) |
| `src/main/resources/sql/02_datos_prueba.sql` | Usuarios `admin` / `encargado`, catálogo, ONG, comunidades, proveedores y pedidos web iniciales |
| `src/main/resources/sql/03_datos_adicionales.sql` | Más productos y pedidos en distintos estados, lotes y donaciones para la demo |
| `src/main/resources/sql/04_comunidad_marcapata.sql` | Solo para bases cargadas antes de que Marcapata estuviera en `02` (idempotente) |
| `src/main/resources/sql/05_correccion_fechas_lotes.sql` | Solo para bases cargadas con una versión anterior de `03` (idempotente, incluye consultas de verificación) |
| `src/main/resources/sql/06_web_clientes.sql` | Requisito del portal web (Spring Boot): columnas de autenticación en `cliente`, secuencia de comprobantes `WEB-nnnnnn` e índice para la consulta de impacto (idempotente) |

Los hashes de contraseña de `02` se generan con `GeneradorHash` (`mvn compile exec:java -Dexec.mainClass=pe.edu.utp.segitd.util.GeneradorHash`), nunca a mano.

## Configuración

1. Copia `config.properties.example` a `config.properties` en la raíz del proyecto.
2. Completa `db.url`, `db.user` y `db.password` con los datos del **Session pooler** (puerto 5432) de tu proyecto Supabase.
3. Opcional: `portal.consulta.url` es la URL pública de `consulta-impacto.html` (equipo web); el QR de cada boleta apunta a `<esa URL>?donacion=<id>`.
4. `config.properties` está en `.gitignore`: nunca se sube al repositorio. Alternativamente, define `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_POOL_SIZE` y `PORTAL_CONSULTA_URL` como variables de entorno (tienen prioridad sobre el archivo).

## Compilar y ejecutar

```bash
mvn compile exec:java          # ejecución rápida en desarrollo
mvn package                    # genera target/segitd-hoseg-desktop.jar (único, con dependencias)
java -jar target/segitd-hoseg-desktop.jar
```

## Archivos que genera la aplicación

Todos se crean junto al `.jar` y están en `.gitignore`:

| Carpeta / archivo | Origen |
|---|---|
| `reporte_impacto_<timestamp>.xlsx` | Reportes de impacto (Apache POI): Resumen, Trazabilidad, Historial de despachos, Inventario |
| `boletas/boleta_<comprobante>.pdf` | Boleta digital con QR de trazabilidad (PDFBox + ZXing), desde Pedidos web |
| `backups/backup_<timestamp>/` | Respaldo CSV de tablas críticas (manual o automático al cerrar tras 24 h) |

## Estructura

Arquitectura en capas estricta: `vista` → `controlador` → `servicio` → `dao` → `db`. Ninguna sentencia SQL fuera de `dao`; ninguna regla de negocio dentro de un `actionPerformed`; validaciones de entrada en `util.Validador`. Ver sección 6 del documento de especificación.
