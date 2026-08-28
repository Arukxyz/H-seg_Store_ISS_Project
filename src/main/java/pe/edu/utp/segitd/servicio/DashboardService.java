package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.DonacionDAO;
import pe.edu.utp.segitd.dao.LoteDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.dao.VentaDAO;
import pe.edu.utp.segitd.db.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;

/** Indicadores en vivo y estado de conexión para MenuPrincipalJFrame. */
public class DashboardService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final VentaDAO ventaDAO = new VentaDAO();
    private final DonacionDAO donacionDAO = new DonacionDAO();
    private final LoteDAO loteDAO = new LoteDAO();

    public IndicadoresDashboard obtenerIndicadores() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return new IndicadoresDashboard(
                    productoDAO.contarActivos(conexion),
                    productoDAO.contarBajoStockMinimo(conexion),
                    ventaDAO.contarPendientes(conexion),
                    donacionDAO.contarPendientes(conexion),
                    loteDAO.contarEnRuta(conexion));
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar los indicadores del panel.", e);
        }
    }

    public boolean hayConexion() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return conexion.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }
}
