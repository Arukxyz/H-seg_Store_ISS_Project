package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.UsuarioDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.util.HashUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Autenticación de usuarios internos (RF-01). El contador de intentos
 * fallidos y el bloqueo temporal viven en la base de datos, no en memoria
 * (sección 7), para que sobrevivan a un reinicio de la aplicación.
 */
public class AuthService {

    private static final int MAX_INTENTOS_FALLIDOS = 3;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(5);

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario autenticar(String username, char[] password) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            Usuario usuario = usuarioDAO.buscarPorUsername(username, conexion)
                    .orElseThrow(() -> new ServicioException("Usuario o contraseña incorrectos."));

            if (!usuario.isActivo()) {
                throw new ServicioException("El usuario está inactivo. Contacta a un administrador.");
            }

            verificarBloqueo(usuario);

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

    private void verificarBloqueo(Usuario usuario) {
        OffsetDateTime bloqueadoHasta = usuario.getBloqueadoHasta();
        if (bloqueadoHasta != null && bloqueadoHasta.isAfter(OffsetDateTime.now())) {
            long minutosRestantes = Duration.between(OffsetDateTime.now(), bloqueadoHasta).toMinutes() + 1;
            throw new ServicioException(
                    "Usuario bloqueado temporalmente. Intenta de nuevo en " + minutosRestantes + " minuto(s).");
        }
    }

    private void registrarIntentoFallido(Usuario usuario, Connection conexion) throws SQLException {
        int intentos = usuario.getIntentosFallidos() + 1;
        OffsetDateTime bloqueadoHasta = intentos >= MAX_INTENTOS_FALLIDOS
                ? OffsetDateTime.now().plus(DURACION_BLOQUEO)
                : null;
        usuarioDAO.registrarIntentoFallido(usuario.getId(), intentos, bloqueadoHasta, conexion);
    }
}
