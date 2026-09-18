package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.ComunidadDAO;
import pe.edu.utp.segitd.dao.DonacionDAO;
import pe.edu.utp.segitd.dao.LoteDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.dao.VentaDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.EstadoDonacion;
import pe.edu.utp.segitd.modelo.FilaHistorialDespacho;
import pe.edu.utp.segitd.modelo.FilaTrazabilidad;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.ResumenVentas;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consultas para el reporte de impacto (RF-07). La hoja de trazabilidad
 * que arma este servicio es la respuesta directa al problema planteado
 * en la sección 1: qué venta generó qué donación entregada.
 *
 * Dos ejes temporales distintos, a propósito:
 * <ul>
 *   <li><b>Fecha de venta</b>: trazabilidad y "ventas versus donaciones"
 *       (qué pasó con lo vendido en el periodo).</li>
 *   <li><b>Fecha de despacho</b>: historial de despachos e indicadores de
 *       impacto (qué se entregó a las comunidades en el periodo).</li>
 * </ul>
 */
public class ReporteService {

    private final DonacionDAO donacionDAO = new DonacionDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ComunidadDAO comunidadDAO = new ComunidadDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

    public List<Comunidad> listarComunidades() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return comunidadDAO.listarTodas(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar la lista de comunidades.", e);
        }
    }

    public List<FilaTrazabilidad> generarTrazabilidad(OffsetDateTime desde, OffsetDateTime hasta, Integer idComunidad) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return donacionDAO.listarTrazabilidad(desde, hasta, idComunidad, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo generar la trazabilidad del reporte.", e);
        }
    }

    public List<FilaHistorialDespacho> generarHistorialDespachos(OffsetDateTime desde, OffsetDateTime hasta, Integer idComunidad) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return loteDAO.listarHistorialDespachos(desde, hasta, idComunidad, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo generar el historial de despachos del reporte.", e);
        }
    }

    public List<Producto> listarInventario() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return productoDAO.listarActivos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar el inventario para el reporte.", e);
        }
    }

    /**
     * Indicadores de impacto a partir del historial de despachos del periodo,
     * más el consolidado de ventas versus donaciones. El filtro de comunidad
     * aplica al historial; las ventas no tienen comunidad hasta que se
     * despachan, así que ese bloque siempre es global al periodo.
     */
    public ResumenImpacto calcularResumen(OffsetDateTime desde, OffsetDateTime hasta, List<FilaHistorialDespacho> historial) {
        int prendasDonadas = historial.stream().mapToInt(FilaHistorialDespacho::prendasAbrigo).sum();
        int arbolesPlantados = historial.stream().mapToInt(FilaHistorialDespacho::arboles).sum();
        long comunidadesAtendidas = historial.stream().map(FilaHistorialDespacho::comunidad).distinct().count();
        int lotesEntregados = historial.size();

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            ResumenVentas ventas = ventaDAO.resumirVentasPagadas(desde, hasta, conexion);
            Map<EstadoDonacion, Integer> porEstado = donacionDAO.contarUnidadesPorEstado(desde, hasta, conexion);
            int pendientes = porEstado.get(EstadoDonacion.PENDIENTE);
            int asignadas = porEstado.get(EstadoDonacion.ASIGNADA);
            int entregadas = porEstado.get(EstadoDonacion.ENTREGADA);

            return new ResumenImpacto(prendasDonadas, arbolesPlantados, (int) comunidadesAtendidas, lotesEntregados,
                    ventas, pendientes + asignadas + entregadas, pendientes, asignadas, entregadas);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo calcular el resumen de ventas y donaciones.", e);
        }
    }
}
