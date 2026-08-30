package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.UsuarioDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.util.HashUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Administración de usuarios internos (RF-08). Solo ADMINISTRADOR. */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> listarUsuarios() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return usuarioDAO.listarTodos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar la lista de usuarios.", e);
        }
    }

    public void crearUsuario(String nombre, String username, String password, RolUsuario rol) {
        if (esVacio(nombre) || esVacio(username) || esVacio(password)) {
            throw new ServicioException("Nombre, usuario y contraseña son obligatorios.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            if (usuarioDAO.existeUsername(username, conexion)) {
                throw new ServicioException("Ya existe un usuario con el nombre de usuario \"" + username + "\".");
            }
            String salt = HashUtil.generarSalt();
            String hash = HashUtil.hashear(password, salt);

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setUsername(username);
            usuario.setPasswordHash(hash);
            usuario.setSalt(salt);
            usuario.setRol(rol);
            usuarioDAO.crear(usuario, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo crear el usuario.", e);
        }
    }

    public void actualizarUsuario(int id, String nombre, RolUsuario rol) {
        if (esVacio(nombre)) {
            throw new ServicioException("El nombre es obligatorio.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            Usuario usuario = new Usuario();
            usuario.setId(id);
            usuario.setNombre(nombre);
            usuario.setRol(rol);
            usuarioDAO.actualizarDatos(usuario, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo actualizar el usuario.", e);
        }
    }

    /** "Un administrador no puede desactivarse a sí mismo" (sección 8, pantalla 8). */
    public void desactivarUsuario(int idUsuarioObjetivo, int idUsuarioActual) {
        if (idUsuarioObjetivo == idUsuarioActual) {
            throw new ServicioException("No puedes desactivar tu propio usuario.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            usuarioDAO.desactivar(idUsuarioObjetivo, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo desactivar el usuario.", e);
        }
    }

    public void resetearPassword(int id, String nuevaPassword) {
        if (esVacio(nuevaPassword)) {
            throw new ServicioException("La nueva contraseña no puede estar vacía.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            String salt = HashUtil.generarSalt();
            String hash = HashUtil.hashear(nuevaPassword, salt);
            usuarioDAO.actualizarCredenciales(id, hash, salt, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo restablecer la contraseña.", e);
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
