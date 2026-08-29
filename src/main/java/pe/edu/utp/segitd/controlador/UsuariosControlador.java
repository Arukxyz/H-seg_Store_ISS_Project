package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.UsuarioService;
import pe.edu.utp.segitd.util.SesionUsuario;

import java.util.List;

/** Traduce los eventos de UsuariosJFrame a llamadas de UsuarioService. */
public class UsuariosControlador {

    private final UsuarioService usuarioService = new UsuarioService();

    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    public void crearUsuario(String nombre, String username, String password, RolUsuario rol) {
        usuarioService.crearUsuario(nombre, username, password, rol);
    }

    public void actualizarUsuario(int id, String nombre, RolUsuario rol) {
        usuarioService.actualizarUsuario(id, nombre, rol);
    }

    public void desactivarUsuario(int idObjetivo) {
        int idActual = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        usuarioService.desactivarUsuario(idObjetivo, idActual);
    }

    public void resetearPassword(int id, String nuevaPassword) {
        usuarioService.resetearPassword(id, nuevaPassword);
    }

    public int idUsuarioActual() {
        return SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
    }
}
