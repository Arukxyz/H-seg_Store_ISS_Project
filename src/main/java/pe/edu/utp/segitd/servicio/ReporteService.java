package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.ComunidadDAO;
import pe.edu.utp.segitd.dao.DonacionDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.EstadoDonacion;
import pe.edu.utp.segitd.modelo.FilaTrazabilidad;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.TipoCompromiso;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Consultas para el reporte de impacto (RF-07). La hoja de trazabilidad
 * que arma este servicio es la respuesta directa al problema planteado
 * en la sección 1: qué venta generó qué donación entregada.
 */
public class ReporteService {

    private final DonacionDAO donacionDAO = new DonacionDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ComunidadDAO comunidadDAO = new ComunidadDAO();

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

    public List<Producto> listarInventario() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return productoDAO.listarActivos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar el inventario para el reporte.", e);
        }
    }

    public ResumenImpacto calcularResumen(List<FilaTrazabilidad> filas) {
        int prendasDonadas = filas.stream()
                .filter(f -> f.estado() == EstadoDonacion.ENTREGADA && f.tipo() == TipoCompromiso.ABRIGO)
                .mapToInt(FilaTrazabilidad::cantidad)
                .sum();
        int arbolesPlantados = filas.stream()
                .filter(f -> f.estado() == EstadoDonacion.ENTREGADA && f.tipo() == TipoCompromiso.ARBOL)
                .mapToInt(FilaTrazabilidad::cantidad)
                .sum();
        long comunidadesAtendidas = filas.stream()
                .filter(f -> f.estado() == EstadoDonacion.ENTREGADA && f.comunidad() != null)
                .map(FilaTrazabilidad::comunidad)
                .distinct()
                .count();
        long lotesEntregados = filas.stream()
                .filter(f -> f.estado() == EstadoDonacion.ENTREGADA && f.codigoLote() != null)
                .map(FilaTrazabilidad::codigoLote)
                .distinct()
                .count();
        return new ResumenImpacto(prendasDonadas, arbolesPlantados, (int) comunidadesAtendidas, (int) lotesEntregados);
    }
}
