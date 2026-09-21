package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.UsuarioDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.util.HashUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.OptionalInt;

/**
 * Autenticación de usuarios internos (RF-01). El contador de intentos
 * fallidos y el bloqueo temporal viven en la base de datos, no en memoria
 * (sección 7), para que sobrevivan a un reinicio de la aplicación.
 */
public class AuthService {

    private static final int MAX_INTENTOS_FALLIDOS = 3;
    private static final int MINUTOS_BLOQUEO = 5;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario autenticar(String username, char[] password) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            
            Usuario usuario = usuarioDAO.buscarPorUsername(username, conexion)
                    .orElseThrow(() -> new ServicioException("Usuario o contraseña incorrectos."));

            if (!usuario.isActivo()) {
                throw new ServicioException("El usuario está inactivo. Contacta a un administrador.");
            }

            verificarBloqueo(usuario, conexion);

            String hashIngresado = HashUtil.hashear(new String(password), usuario.getSalt());
            if (!hashIngresado.equals(usuario.getPasswordHash())) {
                registrarIntentoFallido(usuario, conexion);
                throw new ServicioException("Usuario o contraseña incorrectos.");
            }

            usuarioDAO.resetearIntentosFallidos(usuario.getId(), conexion);
            return usuario;
        } catch (SQLException e) {
            throw new ServicioException("No se pudo validar las credenciales. Verifica tu conexión.", e);
        }
    }

    /** El tiempo restante lo calcula Postgres con su propio now() (regla 9.4). */
    private void verificarBloqueo(Usuario usuario, Connection conexion) throws SQLException {
        OptionalInt minutosRestantes = usuarioDAO.minutosRestantesBloqueo(usuario.getId(), conexion);
        if (minutosRestantes.isPresent()) {
            throw new ServicioException(
                    "Usuario bloqueado temporalmente. Intenta de nuevo en " + minutosRestantes.getAsInt() + " minuto(s).");
        }
    }

    private void registrarIntentoFallido(Usuario usuario, Connection conexion) throws SQLException {
    int intentos = usuario.getIntentosFallidos() + 1;
    int minutosBloqueo = intentos >= MAX_INTENTOS_FALLIDOS ? MINUTOS_BLOQUEO : 0;
    
    usuarioDAO.registrarIntentoFallido(usuario.getId(), intentos, minutosBloqueo, conexion);

    if (intentos >= MAX_INTENTOS_FALLIDOS) {
        throw new ServicioException(
            "Has superado el límite de intentos. Usuario bloqueado temporalmente por " + MINUTOS_BLOQUEO + " minutos."
        );
    }
}

}
