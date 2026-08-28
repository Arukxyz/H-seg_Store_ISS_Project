package pe.edu.utp.segitd;

import pe.edu.utp.segitd.db.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Punto de entrada de la aplicacion.
 * TODO: reemplazar la prueba de conexion por FlatLaf + LoginJFrame
 * cuando esas clases existan (ver seccion 12 del documento de especificacion).
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        System.out.println("SEGITD-HOSEG -- probando conexion a Supabase...");
        try (Connection conexion = ConexionBD.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {

            sentencia.executeQuery("SELECT 1");
            System.out.println("Conexion exitosa: " + conexion.getMetaData().getURL());
        } catch (IllegalStateException e) {
            System.err.println("Configuracion incompleta: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a la base de datos: " + e.getMessage());
            System.exit(1);
        } finally {
            ConexionBD.cerrar();
        }
    }
}
