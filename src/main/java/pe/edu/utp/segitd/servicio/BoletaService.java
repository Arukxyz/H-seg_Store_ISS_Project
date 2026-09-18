package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.config.AppConfig;
import pe.edu.utp.segitd.dao.DonacionDAO;
import pe.edu.utp.segitd.dao.VentaDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoVenta;
import pe.edu.utp.segitd.modelo.Venta;
import pe.edu.utp.segitd.util.BoletaPdfGenerator;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Emisión de la boleta digital con trazabilidad QR (módulo 5).
 *
 * Regla de negocio: solo se emite boleta de un pedido PAGADO. Un pedido
 * PENDIENTE aún no movió stock ni tiene donaciones definitivas, y uno
 * ANULADO ya no representa una venta. El QR de cada donación enlaza al
 * portal público de consulta con el ID único de la donación, que es lo que
 * permite al cliente verificar el estado de la prenda que financió.
 */
public class BoletaService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final DonacionDAO donacionDAO = new DonacionDAO();
    private final BoletaPdfGenerator generador = new BoletaPdfGenerator();

    public File emitirBoleta(int idVenta) {
        DatosBoleta datos = cargarDatos(idVenta);
        try {
            return generador.generar(datos);
        } catch (IOException e) {
            throw new ServicioException("No se pudo generar el archivo PDF de la boleta. "
                    + "Verifica que la carpeta 'boletas' no esté abierta ni protegida.", e);
        }
    }

    private DatosBoleta cargarDatos(int idVenta) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            Venta venta = ventaDAO.buscarPorId(idVenta, conexion)
                    .orElseThrow(() -> new ServicioException("El pedido no existe."));
            if (venta.getEstado() != EstadoVenta.PAGADO) {
                throw new ServicioException("Solo se puede emitir boleta de un pedido confirmado (PAGADO).");
            }
            List<DetalleVenta> detalle = ventaDAO.listarDetalle(idVenta, conexion);
            List<Donacion> donaciones = donacionDAO.listarPorVenta(idVenta, conexion);
            String urlPortal = AppConfig.obtenerInstancia().getPortalConsultaUrl();
            return new DatosBoleta(venta, detalle, donaciones, urlPortal);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar los datos del pedido para la boleta.", e);
        }
    }
}
