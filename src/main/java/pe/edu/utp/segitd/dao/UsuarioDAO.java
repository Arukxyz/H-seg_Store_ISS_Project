package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Acceso a datos de usuario (personal interno de Höség).
 * Todos los métodos reciben la Connection para poder participar de una
 * transacción externa iniciada en la capa de servicio (sección 9.2).
 */
public final class UsuarioDAO {

    public Usuario crear(Usuario usuario, Connection conexion) throws SQLException {
        String sql = """
                INSERT INTO usuario (nombre, username, password_hash, salt, rol)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, usuario.getPasswordHash());
            ps.setString(4, usuario.getSalt());
            ps.setString(5, usuario.getRol().name());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setId(claves.getInt(1));
                }
            }
        }
        return usuario;
    }

    public Optional<Usuario> buscarPorUsername(String username, Connection conexion) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> buscarPorId(int id, Connection conexion) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Usuario> listarTodos(Connection conexion) throws SQLException {
        String sql = "SELECT * FROM usuario ORDER BY nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Usuario> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        }
    }

    public boolean existeUsername(String username, Connection conexion) throws SQLException {
        String sql = "SELECT 1 FROM usuario WHERE username = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void actualizarDatos(Usuario usuario, Connection conexion) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, rol = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getRol().name());
            ps.setInt(3, usuario.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarCredenciales(int id, String passwordHash, String salt, Connection conexion) throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ?, salt = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /**
     * Guarda el contador y, si {@code minutosBloqueo} > 0, fija el bloqueo
     * con el reloj del servidor (regla 9.4: nunca la hora de la máquina
     * cliente, porque hay dos sistemas escribiendo desde equipos distintos).
     */
    public void registrarIntentoFallido(int id, int intentosFallidos, int minutosBloqueo, Connection conexion) throws SQLException {
        String sql = """
                UPDATE usuario
                   SET intentos_fallidos = ?,
                       bloqueado_hasta   = CASE WHEN ? > 0 THEN now() + make_interval(mins => ?) ELSE NULL END
                 WHERE id = ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, intentosFallidos);
            ps.setInt(2, minutosBloqueo);
            ps.setInt(3, minutosBloqueo);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    /** Minutos que faltan para que expire el bloqueo, calculados por el servidor; vacío si no está bloqueado. */
    public OptionalInt minutosRestantesBloqueo(int id, Connection conexion) throws SQLException {
        String sql = """
                SELECT CEIL(EXTRACT(EPOCH FROM (bloqueado_hasta - now())) / 60)::int AS minutos
                  FROM usuario
                 WHERE id = ? AND bloqueado_hasta > now()
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? OptionalInt.of(Math.max(1, rs.getInt("minutos"))) : OptionalInt.empty();
            }
        }
    }

    public void resetearIntentosFallidos(int id, Connection conexion) throws SQLException {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void desactivar(int id, Connection conexion) throws SQLException {
        String sql = "UPDATE usuario SET activo = FALSE WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setSalt(rs.getString("salt"));
        usuario.setRol(RolUsuario.valueOf(rs.getString("rol")));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        usuario.setBloqueadoHasta(rs.getObject("bloqueado_hasta", OffsetDateTime.class));
        usuario.setCreadoEn(rs.getObject("creado_en", OffsetDateTime.class));
        return usuario;
    }
}
