package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.servicio.DashboardService;
import pe.edu.utp.segitd.servicio.IndicadoresDashboard;
import pe.edu.utp.segitd.util.SesionUsuario;

/** Traduce los eventos de MenuPrincipalJFrame a llamadas de servicio. */
public class MenuPrincipalControlador {

    private final DashboardService dashboardService = new DashboardService();

    public IndicadoresDashboard cargarIndicadores() {
        return dashboardService.obtenerIndicadores();
    }

    public boolean verificarConexion() {
        return dashboardService.hayConexion();
    }

    public void cerrarSesion() {
        SesionUsuario.obtenerInstancia().cerrarSesion();
    }
}
