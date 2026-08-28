package pe.edu.utp.segitd.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuracion de la aplicacion (credenciales de base de datos).
 * Prioridad de lectura: variables de entorno &gt; config.properties.
 * config.properties nunca se versiona (ver .gitignore); en su lugar
 * se distribuye config.properties.example con placeholders.
 */
public final class AppConfig {

    private static final String ARCHIVO_CONFIG = "config.properties";
    private static final int POOL_SIZE_POR_DEFECTO = 5;

    private static volatile AppConfig instancia;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final int dbPoolSize;

    private AppConfig() {
        Properties propiedades = cargarPropiedades();

        this.dbUrl = valorObligatorio("DB_URL", "db.url", propiedades);
        this.dbUser = valorObligatorio("DB_USER", "db.user", propiedades);
        this.dbPassword = valorObligatorio("DB_PASSWORD", "db.password", propiedades);
        this.dbPoolSize = valorPoolSize(propiedades);
    }

    public static AppConfig obtenerInstancia() {
        AppConfig resultado = instancia;
        if (resultado == null) {
            synchronized (AppConfig.class) {
                resultado = instancia;
                if (resultado == null) {
                    resultado = new AppConfig();
                    instancia = resultado;
                }
            }
        }
        return resultado;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public int getDbPoolSize() {
        return dbPoolSize;
    }

    private Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        Path ruta = Path.of(ARCHIVO_CONFIG);
        if (Files.exists(ruta)) {
            try (InputStream entrada = Files.newInputStream(ruta)) {
                propiedades.load(entrada);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer " + ARCHIVO_CONFIG, e);
            }
        }
        return propiedades;
    }

    private String valorObligatorio(String variableEntorno, String clavePropiedad, Properties propiedades) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.isBlank()) {
            valor = propiedades.getProperty(clavePropiedad);
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Falta la configuracion '" + clavePropiedad + "'. Definila como variable de entorno "
                            + variableEntorno + ", o copia config.properties.example a config.properties "
                            + "y completa sus valores.");
        }
        return valor;
    }

    private int valorPoolSize(Properties propiedades) {
        String valor = System.getenv("DB_POOL_SIZE");
        if (valor == null || valor.isBlank()) {
            valor = propiedades.getProperty("db.pool.size");
        }
        if (valor == null || valor.isBlank()) {
            return POOL_SIZE_POR_DEFECTO;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("db.pool.size debe ser un numero entero, se recibio: " + valor, e);
        }
    }
}
