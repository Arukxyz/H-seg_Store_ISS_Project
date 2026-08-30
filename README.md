# SEGITD-HÖSÉG — Módulo de Escritorio

Back-office administrativo de Höség Store (14-DIEZ S.A.C.): inventario, lotes de donación, despacho, reportes de impacto, proveedores y usuarios.

Java Swing + JDBC, conectado a PostgreSQL en Supabase Cloud. Ver [SEGITD-HOSEG.md](SEGITD-HOSEG.md) para la especificación completa.

## Requisitos

- JDK 21
- Maven 3.9+
- Un proyecto de Supabase creado (ver sección 3 del documento de especificación) con `01_schema.sql` y `02_datos_prueba.sql` ejecutados

## Configuración

1. Copia `config.properties.example` a `config.properties` en la raíz del proyecto.
2. Completa `db.url`, `db.user` y `db.password` con los datos del **Session pooler** (puerto 5432) de tu proyecto Supabase.
3. `config.properties` está en `.gitignore`: nunca se sube al repositorio. Alternativamente, define `DB_URL`, `DB_USER`, `DB_PASSWORD` y `DB_POOL_SIZE` como variables de entorno (tienen prioridad sobre el archivo).

## Compilar y ejecutar

```bash
mvn compile exec:java          # ejecución rápida en desarrollo
mvn package                    # genera target/segitd-hoseg-desktop.jar
java -jar target/segitd-hoseg-desktop.jar
```

## Estructura

Arquitectura en capas estricta: `vista` → `controlador` → `servicio` → `dao` → `db`. Ninguna sentencia SQL fuera de `dao`; ninguna regla de negocio dentro de un `actionPerformed`. Ver sección 6 del documento de especificación.
