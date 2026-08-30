package pe.edu.utp.segitd.util;

import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;

/** Usuario interno autenticado en la sesión actual del escritorio (singleton). */
public final class SesionUsuario {

    private static final SesionUsuario INSTANCIA = new SesionUsuario();

    private Usuario usuarioActual;

    private SesionUsuario() {
    }

    public static SesionUsuario obtenerInstancia() {
        return INSTANCIA;
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public boolean esAdministrador() {
        return haySesionActiva() && usuarioActual.getRol() == RolUsuario.ADMINISTRADOR;
    }
}
