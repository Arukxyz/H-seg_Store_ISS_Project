# SEGITD-HÖSÉG — Módulo de Escritorio (Java Swing + Supabase)

Especificación de implementación para Claude Code.
Proyecto académico — Curso Integrador I: Sistemas y Software (UTP, ciclo 2026-2).

---

## 1. Contexto

**Empresa:** 14-DIEZ S.A.C., nombre comercial **Höség Store** (RUC 20566428386). Retail textil peruano de ropa outdoor sostenible, certificado como Empresa B.

**Modelo de negocio:** triple impacto.
- *Buy One, Give One*: por cada prenda de abrigo vendida, se dona una casaca a un niño altoandino.
- *Buy One, Plant One*: por cada accesorio vendido, se planta un árbol nativo junto a la ONG Pachamama Raymi.

**Problema:** no existe vínculo trazable entre la venta que generó un compromiso social y la donación efectivamente entregada. Todo se consolida a mano en Excel, lo que produce inconsistencias y hace lento el reporte de impacto que exige la recertificación como Empresa B.

**Ecosistema completo del proyecto (dos sistemas, una sola base de datos):**

| Sistema | Responsabilidad | Tecnología |
|---|---|---|
| **Portal web e-commerce** | Catálogo, pedidos de clientes finales, consulta pública de impacto | Otro equipo (fuera de este repositorio) |
| **Aplicativo de escritorio** ← **ESTE PROYECTO** | Back-office administrativo: inventario, lotes de donación, despacho, reportes, usuarios, proveedores | Java Swing + JDBC |

**Regla de oro:** este aplicativo **NO vende**. Las ventas nacen en la web. El escritorio recibe esos pedidos, los gestiona, y agrupa las donaciones generadas en lotes logísticos hacia comunidades altoandinas de Cusco.

---

## 2. Alcance de este entregable

Aplicación de escritorio Java Swing, empaquetada como `.jar` ejecutable único, conectada por JDBC a una base PostgreSQL alojada en Supabase Cloud.

### Requisitos funcionales cubiertos

| ID | Requisito | Pantalla |
|---|---|---|
| RF-01 | Autenticación de usuarios con bloqueo tras 3 intentos | LoginJFrame |
| RF-02 | Gestión de catálogo de prendas | GestionProductosJFrame |
| RF-03 | Inventario dual (stock comercial / comprometido) | GestionProductosJFrame |
| RF-04 | Recepción y gestión de pedidos provenientes de la web | PedidosWebJFrame |
| RF-05 | Gestión de lotes y destinos de donación | DespachoLotesJFrame |
| RF-06 | Registro de pedidos a proveedores | ProveedoresJFrame |
| RF-07 | Exportación de reportes de impacto a Excel (Apache POI) | ReportesImpactoJFrame |
| RF-08 | Registro y administración de usuarios internos | UsuariosJFrame |

### Requisitos no funcionales

| ID | Requisito | Cómo se cumple |
|---|---|---|
| RNF-01 | Usabilidad: operaciones en ≤ 5 pasos | Menú de acceso directo, formularios de una sola pantalla |
| RNF-02 | Contraseñas nunca en texto plano | SHA-256 **con salt por usuario** (`java.security.MessageDigest`) + `JPasswordField` |
| RNF-03 | Operaciones críticas en < 2 segundos | Consultas indexadas, `PreparedStatement`, pool de conexiones |
| RNF-04 | Portabilidad: `.jar` multiplataforma | `maven-shade-plugin`, compilado con `release 21` (requiere JRE 21+ en la máquina destino) |
| RNF-05 | Respaldo de tablas críticas | `BackupService` exporta a CSV con timestamp |
| RNF-06 | Mantenibilidad: MVC + DAO | Estructura de paquetes obligatoria (sección 6) |

---

## 3. Creación del proyecto en Supabase

El proyecto **aún no existe**. Estos pasos son manuales (los ejecuta el equipo, no Claude Code), pero el repositorio debe incluir los scripts SQL listos para pegar.

1. Crear cuenta en supabase.com → **New Project**.
   - Nombre: `hoseg-segitd`
   - Región: **South America (São Paulo)** — la más cercana a Lima, menor latencia.
   - Guardar la contraseña de la base de datos en un gestor; **no se puede recuperar después**, solo resetear.
2. Ir al **SQL Editor** y ejecutar, en orden:
   - `src/main/resources/sql/01_schema.sql`
   - `src/main/resources/sql/02_datos_prueba.sql`
3. Botón **Connect** → pestaña **JDBC** → copiar la cadena del **Session pooler (puerto 5432)**.

### Cadena de conexión — usar SIEMPRE el Session pooler

Las conexiones directas de Supabase son **IPv6**. La red de la universidad y la mayoría de ISPs peruanos son IPv4-only, y la conexión directa falla sin mensaje claro. El pooler en modo sesión es IPv4 y se comporta igual que una conexión directa.

```
jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require
usuario:  postgres.<PROJECT_REF>
password: <la contraseña guardada>
```

- **Puerto 5432** (modo sesión). **NO usar 6543** (modo transacción): rompe los prepared statements de JDBC.
- El usuario lleva el project ref después de un punto.
- `sslmode=require` explícito: por defecto los drivers usan `prefer`, que puede caer a texto plano.

### Advertencias operativas

- **El plan gratuito pausa el proyecto tras ~1 semana de inactividad.** Entrar al dashboard el día antes de cualquier demostración.
- Probar la conexión desde la red del campus con antelación: algunas redes bloquean puertos de salida distintos de 80/443.
- Tener un dump `.sql` de respaldo y una configuración alterna a PostgreSQL local como plan B.

---

## 4. Seguridad de credenciales — CRÍTICO

El repositorio de GitHub es **público** (requisito del curso). La contraseña de la base de datos **no puede estar en el código**.

- La configuración se lee de `config.properties`, que va en `.gitignore`.
- El repositorio versiona únicamente `config.properties.example` con placeholders.
- `AppConfig` prioriza variables de entorno sobre el archivo, y muestra un mensaje claro si falta la configuración.
- Si alguna vez se comitea una credencial real: resetear el password en Supabase de inmediato (el historial de Git la conserva para siempre).

`config.properties.example`:
```properties
db.url=jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require
db.user=postgres.<PROJECT_REF>
db.password=<TU_PASSWORD>
db.pool.size=5
```

---

## 5. Esquema de base de datos

Archivo: `src/main/resources/sql/01_schema.sql`

Este esquema es **compartido con el equipo web**. Es la única fuente de verdad: ninguna tabla ni columna se crea a mano desde el dashboard. Todo cambio pasa por este archivo versionado.

```sql
-- =====================================================================
-- SEGITD-HÖSÉG · Esquema de base de datos
-- PostgreSQL / Supabase
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
    id           SERIAL PRIMARY KEY,
    id_proveedor INTEGER NOT NULL REFERENCES proveedor(id),
    descripcion  VARCHAR(255) NOT NULL,
    cantidad     INTEGER NOT NULL CHECK (cantidad > 0),
    fecha        TIMESTAMPTZ NOT NULL DEFAULT now(),
    estado       VARCHAR(12) NOT NULL DEFAULT 'SOLICITADO'
                 CHECK (estado IN ('SOLICITADO','RECIBIDO','ANULADO')),
    id_usuario   INTEGER REFERENCES usuario(id)
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
```

### Datos de prueba

Archivo: `src/main/resources/sql/02_datos_prueba.sql`

- 2 usuarios: `admin` (ADMINISTRADOR) y `encargado` (ENCARGADO).
  **Los hashes se generan con la utilidad Java del proyecto**, no a mano — ver sección 7.
- Al menos 5 productos de la colección Höség (casacas y accesorios Älpafill) con stock comercial inicial.
- ONG: Pachamama Raymi.
- Comunidades de Cusco: Omacha, Ccatca, Paucartambo, Layo.
- 2 proveedores (talleres textiles).
- 3 ventas de origen WEB con sus detalles y donaciones en estado PENDIENTE, para poder demostrar el flujo sin depender de que la web esté levantada.

---

## 6. Estructura del proyecto

```
segitd-hoseg-desktop/
├── pom.xml
├── README.md
├── .gitignore                        # config.properties, target/, *.xlsx, backups/
├── config.properties.example
└── src/main/
    ├── java/pe/edu/utp/segitd/
    │   ├── App.java                  # main: FlatLaf + LoginJFrame
    │   ├── config/
    │   │   └── AppConfig.java        # env vars > config.properties
    │   ├── db/
    │   │   └── ConexionBD.java       # pool, getConnection(), cierre limpio
    │   ├── modelo/
    │   │   ├── Usuario.java  Producto.java  Cliente.java
    │   │   ├── Venta.java    DetalleVenta.java
    │   │   ├── Donacion.java LoteDonacion.java
    │   │   ├── Comunidad.java Ong.java
    │   │   ├── Proveedor.java PedidoProveedor.java
    │   │   └── MovimientoInventario.java
    │   ├── dao/
    │   │   ├── UsuarioDAO.java   ProductoDAO.java  VentaDAO.java
    │   │   ├── DonacionDAO.java  LoteDAO.java      ProveedorDAO.java
    │   │   ├── ComunidadDAO.java OngDAO.java       MovimientoDAO.java
    │   ├── servicio/
    │   │   ├── AuthService.java        # login, hash, bloqueo
    │   │   ├── InventarioService.java  # stock, alertas, movimientos
    │   │   ├── PedidoWebService.java   # confirmar/anular pedidos web
    │   │   ├── DespachoService.java    # lotes, asignación, entrega
    │   │   ├── ReporteService.java     # consultas para POI
    │   │   ├── BackupService.java      # RNF-05
    │   │   └── UsuarioService.java     # RF-08
    │   ├── controlador/                # un controlador por vista
    │   ├── vista/                      # los JFrames (sección 8)
    │   └── util/
    │       ├── HashUtil.java           # SHA-256 + salt
    │       ├── ExcelExporter.java      # Apache POI
    │       ├── SesionUsuario.java      # usuario autenticado (singleton)
    │       ├── Validador.java
    │       └── GeneradorHash.java      # main auxiliar (ver sección 7)
    └── resources/
        ├── sql/01_schema.sql
        ├── sql/02_datos_prueba.sql
        └── iconos/
```

**Regla de capas, no negociable (RNF-06):**
`vista` (solo captura eventos y pinta) → `controlador` (traduce evento a llamada) → `servicio` (reglas de negocio y transacciones) → `dao` (solo SQL) → `db`.

Ninguna sentencia SQL fuera de `dao`. Ninguna regla de negocio dentro de un `actionPerformed`.

### Dependencias Maven

```xml
<dependencies>
  <dependency>org.postgresql:postgresql</dependency>       <!-- driver JDBC -->
  <dependency>org.apache.poi:poi</dependency>
  <dependency>org.apache.poi:poi-ooxml</dependency>        <!-- .xlsx -->
  <dependency>com.formdev:flatlaf</dependency>             <!-- look & feel -->
  <dependency>com.zaxxer:HikariCP</dependency>             <!-- pool, opcional -->
</dependencies>
```

Todas las versiones deben ser las últimas estables compatibles con JDK 21. Apache POI requiere 5.2.0 o superior para compilar sin advertencias en 21; FlatLaf 3.x y HikariCP 5.x son compatibles sin ajustes.

Plugins: `maven-compiler-plugin` (Java 21, usando `<maven.compiler.release>21</maven.compiler.release>` en `<properties>`, no `source`/`target`) y **`maven-shade-plugin`** con `mainClass = pe.edu.utp.segitd.App` — sin shade no hay `.jar` único y no se cumple RNF-04.

Al empaquetar con shade sobre JDK 21 pueden aparecer advertencias de firmas duplicadas por los módulos de POI; se resuelven excluyendo `META-INF/*.SF`, `*.DSA` y `*.RSA` en la configuración del plugin.

---

## 7. Autenticación y hash

- `HashUtil.hashear(String password, String salt)` → SHA-256 hexadecimal de `salt + password`, usando `java.security.MessageDigest`.
- `HashUtil.generarSalt()` → 16 bytes de `SecureRandom` en hexadecimal.
- Al crear un usuario: generar salt, hashear, guardar ambos.
- Al validar: recuperar salt del usuario, hashear lo ingresado, comparar.

**`GeneradorHash.java`** es un `main` auxiliar que imprime el `INSERT` completo para los usuarios semilla. Se ejecuta una vez y su salida se pega en `02_datos_prueba.sql`. Así ningún hash se escribe a mano.

**Bloqueo (RF-01):** el contador vive en la base, no en memoria.
- Login fallido → `intentos_fallidos = intentos_fallidos + 1`.
- Al llegar a 3 → `bloqueado_hasta = now() + interval '5 minutes'`.
- Login exitoso → resetear contador y `bloqueado_hasta` a NULL.
- Antes de validar credenciales, comprobar si `bloqueado_hasta > now()`; si sí, rechazar con el tiempo restante.

---

## 8. Pantallas

### 1. LoginJFrame — RF-01 / RNF-02
`JTextField` de usuario y `JPasswordField` enmascarado. Valida contra Supabase con SHA-256 + salt. Aplica el bloqueo de la sección 7. Mensajes claros: credenciales inválidas, usuario bloqueado, usuario inactivo, error de conexión. Al autenticar, carga `SesionUsuario` y abre el menú.

### 2. MenuPrincipalJFrame — Dashboard
Panel con indicadores en vivo: total de productos activos, productos bajo stock mínimo, pedidos web pendientes, donaciones pendientes de asignar, lotes en ruta. Indicador visual del estado de conexión a Supabase.

**Control de accesos por rol (en código Java, `setEnabled(false)`):**

| Módulo | ADMINISTRADOR | ENCARGADO |
|---|---|---|
| Gestión de productos | Total | Solo consulta |
| Pedidos web | Total | Total |
| Despacho de lotes | Total | Total |
| Reportes de impacto | Sí | **No** |
| Pedidos a proveedores | Sí | **No** |
| Gestión de usuarios | Sí | **No** |

### 3. GestionProductosJFrame — RF-02 / RF-03
Formulario de alta y edición. `JTable` con columnas **"Stock Comercial"** y **"Stock Comprometido (Ayuda Social)"** visibles y separadas — es la evidencia del inventario dual. Fila resaltada en color cuando `stock_comercial <= stock_minimo`. Botón de ajuste manual de stock que registra un movimiento de tipo AJUSTE. Rechaza códigos duplicados. Baja lógica (`activo = false`), nunca DELETE.

### 4. PedidosWebJFrame — RF-04
Lista los registros de `venta` con `origen = 'WEB'`, filtrables por estado y rango de fechas. Al seleccionar uno, muestra su detalle y las donaciones que generó.

Acciones: **Confirmar pedido** (PENDIENTE → PAGADO) y **Anular pedido** (→ ANULADO, revierte stock y elimina las donaciones asociadas si aún están PENDIENTE).

Botón **Actualizar** siempre visible, más un `javax.swing.Timer` que recarga cada 30 segundos. Esto es lo que hace visible en vivo que la web y el escritorio comparten base.

### 5. DespachoLotesJFrame — RF-05
Selector de comunidad de Cusco y de ONG (ambos obligatorios). Lista de donaciones en estado PENDIENTE con casillas de selección. Al crear el lote: genera `codigo_lote` correlativo (`HSG-L001`), marca las donaciones seleccionadas como ASIGNADA con su `id_lote`.

Transiciones de estado del lote: PENDIENTE → EN_RUTA → ENTREGADO. Al pasar a ENTREGADO, las donaciones del lote pasan a ENTREGADA y se descuenta el `stock_comprometido` de cada producto, registrando el movimiento correspondiente.

Validación: si se intenta despachar más unidades de las disponibles en `stock_comprometido`, bloquear con `JOptionPane.showMessageDialog`.

### 6. ReportesImpactoJFrame — RF-07
Solo ADMINISTRADOR. Filtros por rango de fechas y comunidad. Exporta a `.xlsx` con Apache POI, con estas hojas:

- **Resumen de impacto** — totales de prendas donadas, árboles plantados, comunidades atendidas, lotes entregados.
- **Trazabilidad** — una fila por donación: comprobante de venta, fecha, producto, tipo de compromiso, estado, lote, comunidad, fecha de entrega. *Esta hoja es la respuesta directa al problema planteado.*
- **Inventario** — stock comercial y comprometido por producto al momento del corte.

Encabezados con estilo, columnas autoajustadas, nombre de archivo con timestamp. Abrir el archivo automáticamente con `Desktop.getDesktop().open()` al terminar.

### 7. ProveedoresJFrame — RF-06
Solo ADMINISTRADOR. CRUD de proveedores y registro de pedidos de reposición. Valida que proveedor y cantidad no estén vacíos. Al marcar un pedido como RECIBIDO, suma al `stock_comercial` y registra el movimiento de INGRESO.

### 8. UsuariosJFrame — RF-08
Solo ADMINISTRADOR. Crear, editar y desactivar usuarios internos con su rol. Rechaza usernames duplicados. Un administrador no puede desactivarse a sí mismo. Botón de reseteo de contraseña que regenera salt y hash.

---

## 9. Reglas de negocio críticas

### 9.1 Concurrencia — la web escribe sobre las mismas tablas

**Prohibido el patrón leer-modificar-escribir sobre el stock.** Si Java lee el stock, resta en memoria y hace UPDATE, una venta web ocurrida en medio se pierde. Siempre delegar la aritmética a Postgres y verificar filas afectadas:

```sql
UPDATE producto
   SET stock_comercial = stock_comercial - ?
 WHERE codigo = ? AND stock_comercial >= ?
```

Si `executeUpdate()` devuelve 0, no había stock suficiente: lanzar excepción de negocio y hacer rollback. Para operaciones de varios pasos sobre la misma fila, `SELECT ... FOR UPDATE` dentro de la transacción.

### 9.2 Transacciones

Toda operación que toca más de una tabla va en una transacción explícita en la capa `servicio`:

```java
conn.setAutoCommit(false);
try {
    // ... varios DAO con la MISMA conexión
    conn.commit();
} catch (Exception e) {
    conn.rollback();
    throw new ServicioException("...", e);
} finally {
    conn.setAutoCommit(true);
}
```

Los métodos DAO deben aceptar una `Connection` como parámetro para poder participar de una transacción externa.

### 9.3 Inventario dual

- El `stock_comprometido` **nunca** puede usarse para ventas. Es un saldo bloqueado.
- Solo aumenta cuando se confirma un pedido de producto con `aplica_triple_impacto = true`.
- Solo disminuye cuando un lote pasa a ENTREGADO.
- Toda variación de cualquiera de los dos stocks inserta una fila en `movimiento_inventario`. Sin excepciones.

### 9.4 Reglas transversales

- Fechas: siempre `TIMESTAMPTZ` y `now()` del servidor. Nunca `new Date()` de Java, porque hay dos sistemas escribiendo desde máquinas distintas.
- Toda consulta con `PreparedStatement`. Nunca concatenación de strings en SQL.
- Todos los recursos JDBC con try-with-resources.
- Las excepciones SQL no llegan crudas al usuario: se traducen a mensajes en español en la capa de servicio.
- Nada de lógica de negocio en triggers ni stored procedures: debe estar en Java (requisito del curso).
- El escritorio se conecta con el usuario `postgres`, por lo que **ignora las políticas RLS**. Si el equipo web activa RLS, este aplicativo no se ve afectado.

---

## 10. Backup — RNF-05

`BackupService` exporta a `./backups/` los datos de `producto`, `venta`, `detalle_venta`, `donacion`, `lote_donacion` y `movimiento_inventario` en archivos CSV con timestamp (`backup_20260828_143000/`). Se dispara desde el menú (solo ADMINISTRADOR) y automáticamente al cerrar la aplicación si han pasado más de 24 horas desde el último respaldo.

---

## 11. Flujo de demostración

El código debe permitir ejecutar esta secuencia de principio a fin:

1. **Seguridad** — intentar entrar con clave errónea tres veces; el sistema rechaza y bloquea temporalmente.
2. **Roles** — entrar como `encargado`; los botones de reportes, proveedores y usuarios aparecen deshabilitados.
3. **Catálogo** — entrar como `admin`, registrar un producto nuevo con stock comercial; aparece en la tabla.
4. **Integración web** — generar un pedido desde el portal web (o insertarlo por SQL), pulsar Actualizar en `PedidosWebJFrame`: el pedido aparece, se confirma, baja el stock comercial y sube el comprometido, y se crea la donación en estado PENDIENTE.
5. **Despacho** — agrupar las donaciones pendientes en un lote hacia Omacha con Pachamama Raymi; marcarlo EN_RUTA y luego ENTREGADO.
6. **Auditoría** — exportar el reporte de impacto y abrir el `.xlsx` en vivo, mostrando la hoja de trazabilidad con la venta vinculada a su donación entregada.

---

## 12. Orden de implementación sugerido

1. `pom.xml`, `.gitignore`, `config.properties.example`, `AppConfig`, `ConexionBD` + prueba de conexión.
2. Scripts SQL (`01_schema.sql`, `02_datos_prueba.sql`) y `GeneradorHash`.
3. Modelos y DAOs con las operaciones básicas.
4. `AuthService` + `LoginJFrame` + `SesionUsuario`.
5. `MenuPrincipalJFrame` con control de roles.
6. `InventarioService` + `GestionProductosJFrame`.
7. `PedidoWebService` + `PedidosWebJFrame`.
8. `DespachoService` + `DespachoLotesJFrame`.
9. `ReporteService` + `ExcelExporter` + `ReportesImpactoJFrame`.
10. `ProveedoresJFrame`, `UsuariosJFrame`, `BackupService`.
11. Empaquetado con shade y prueba del `.jar` en Windows y Linux.

---

## 13. Convenciones

- **JDK 21 (LTS)**, codificación UTF-8. Verificar con `java -version` que el JDK instalado sea 21 antes de compilar; NetBeans debe tener la plataforma Java 21 seleccionada en las propiedades del proyecto.
- Características de Java 21 que conviene aprovechar (sin forzarlas):
  - `record` para los DTO de reportes y para los resultados de consultas agregadas (los modelos de entidad siguen siendo clases normales, porque necesitan setters para los DAO).
  - `switch` con patrones y expresiones de switch para las transiciones de estado de lotes, donaciones y ventas.
  - Bloques de texto (`"""`) para las sentencias SQL largas en los DAO: mucho más legibles que la concatenación con `+`.
  - `Objects.requireNonNull` y `Optional` en los DAO que devuelven un único registro.
- **No usar** hilos virtuales ni `StructuredTaskScope`: no aportan nada en una app Swing de escritorio y complican la explicación en la sustentación.
- Nombres de clases, métodos y variables en español (coherente con la documentación del curso); palabras clave de Java en inglés, obviamente.
- Comentarios Javadoc en las clases de servicio explicando la regla de negocio que implementan.
- Commits en español y descriptivos. El repositorio debe mostrar contribuciones de los tres integrantes: Aarón, Najhely y Pilar.
