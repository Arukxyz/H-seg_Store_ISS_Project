package pe.edu.utp.segitd.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pe.edu.utp.segitd.config.AppConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Punto unico de acceso al pool de conexiones JDBC hacia Supabase.
 * Usar SIEMPRE el Session pooler (puerto 5432): la conexion directa
 * de Supabase es IPv6 y falla en redes IPv4-only sin mensaje claro.
 */
public final class ConexionBD {

    private static volatile HikariDataSource dataSource;

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return obtenerDataSource().getConnection();
    }

    public static void cerrar() {
        HikariDataSource actual = dataSource;
        if (actual != null && !actual.isClosed()) {
            actual.close();
        }
    }

    private static HikariDataSource obtenerDataSource() {
        HikariDataSource resultado = dataSource;
        if (resultado == null) {
            synchronized (ConexionBD.class) {
                resultado = dataSource;
                if (resultado == null) {
                    resultado = crearDataSource();
                    dataSource = resultado;
                }
            }
        }
        return resultado;
    }

    private static HikariDataSource crearDataSource() {
        AppConfig config = AppConfig.obtenerInstancia();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getDbUrl());
        hikariConfig.setUsername(config.getDbUser());
        hikariConfig.setPassword(config.getDbPassword());
        hikariConfig.setMaximumPoolSize(config.getDbPoolSize());
        hikariConfig.setPoolName("segitd-hoseg-pool");

        return new HikariDataSource(hikariConfig);
    }
}
