# GUÍA DE PRUEBAS, EJECUCIÓN Y GUION DE DEMOSTRACIÓN (SEGITD)

**Sistema de Información Integral para Höség Store S.A.C. (14-DIEZ S.A.C.)** 
**Documentación y Protocolo de Pruebas elaborado por:** Najhely Banda 
**Curso:** Integrador I: Sistemas - Software (Mgtr. Ing. Danila Raquel Neira Sacaski)

### Configuración del Archivo `config.properties`
Para la ejecución en entorno local, debes crear un archivo llamado `config.properties` en la raíz del proyecto. Este archivo está protegido por `.gitignore` y **no debe subirse al repositorio**. 

Puedes guiarte del archivo plantilla `config.properties.example` adjunto en el proyecto y estructurarlo de la siguiente manera con tus propias credenciales:

```properties
# CONFIGURACIÓN LOCAL (Reemplazar con las credenciales reales)
db.url=jdbc:postgresql://<HOST_SUPABASE>:5432/postgres?sslmode=require
db.user=<USUARIO_POSTGRES>
db.password=<CONTRASEÑA_SUPABASE>
db.pool.size=5
portal.consulta.url=https://hosegstore.com
```

---

## 2. Comandos de Compilación y Ejecución

1. **Compilación y ejecución rápida en desarrollo:**
   ```bash
   mvn compile exec:java
   ```
2. **Generación del archivo ejecutable `.jar` único con dependencias:**
   ```bash
   mvn package
   ```
3. **Ejecución autónoma desde terminal o doble clic:**
   ```bash
   java -jar target/segitd-hoseg-desktop.jar
   ```

---

## 3. Protocolo de Pruebas y Demostración

### MÓDULO 1: Seguridad, Autenticación y Control de Accesos por Rol
* **Caso de Prueba 1.1 - Inicio de Sesión y Cifrado:** Ingresar con usuario `admin` o `encargado`. Las contraseñas se validan mediante cifrado **SHA-256 + salt de 16 bytes** (`HashUtil`).
* **Caso de Prueba 1.2 - Bloqueo de Seguridad Activo:** Al simular 3 intentos fallidos, el sistema activa una restricción temporal en la base de datos usando `bloqueado_hasta` con el tiempo del servidor PostgreSQL (`now()`).
* **Caso de Prueba 1.3 - Control de Accesos por Rol:** Iniciar sesión con el rol `ENCARGADO` y verificar en el `MenuPrincipalJFrame` que las opciones de Gestión de Usuarios y Reportes avanzados se deshabilitan visualmente (`setEnabled(false)`).

### MÓDULO 2: Inventario Dual y Sincronización Atómica de Pedidos Web
* **Caso de Prueba 2.1 - Consulta de Inventario Dual:** En `GestionProductosJFrame` se valida la separación estricta de saldos: **Stock Comercial** (disponible para ventas web) y **Stock Comprometido** (unidades reservadas para donación).
* **Caso de Prueba 2.2 - Protección del Stock de Donaciones:** Verificar que `InventarioService` rechaza cualquier intento de ajuste o decremento manual negativo sobre el stock comprometido, evitando desvíos o manipulaciones no autorizadas en el inventario social.
* **Caso de Prueba 2.3 - Procesamiento Atómico de Pedidos:** Al confirmar un pedido de la tabla `pedido`, `PedidoWebService` ejecuta un `UPDATE` atómico en bloque disminuyendo el stock comercial y registrando en paralelo el nuevo ítem en la tabla `donacion` con estado `PENDIENTE`.

### MÓDULO 3: Emisión de Boleta Digital PDF con QR 
* **Caso de Prueba 3.1 - Generación de Boleta PDF (PDFBox + ZXing):** En la pantalla de *Pedidos Web*, al seleccionar un pedido `PAGADO` y presionar **"Emitir Boleta PDF"**, el sistema genera un documento en la carpeta `boletas/` con los datos de 14-DIEZ S.A.C., desglose de IGV y un **Código QR dinámico**.
* **Caso de Prueba 3.2 - Escaneo y Redirección RSU:** Al escanear el código QR de la boleta con un dispositivo móvil, este redirige a la URL pública (`portal.consulta.url?donacion=<id>`), permitiendo al cliente realizar el seguimiento del abrigo asignado.
* **Caso de Prueba 3.3 - Logística de Despacho (Cusco):** Selección de donaciones `PENDIENTES` para agruparlas en lotes de envío hacia comunidades altoandinas (Omacha, Ccatca, Paucartambo, Layo o Marcapata) a través de la **ONG Pachamama Raymi**, actualizando el flujo a `EN_RUTA` y `ENTREGADO`.
* **Caso de Prueba 3.4 - Auditoría de Impacto B (Apache POI):** Exportación del reporte consolidado `.xlsx` estructurado en 4 pestañas clave: *Resumen de Impacto* (con gráfico integrado), *Trazabilidad*, *Historial de Despachos* e *Inventario*.
