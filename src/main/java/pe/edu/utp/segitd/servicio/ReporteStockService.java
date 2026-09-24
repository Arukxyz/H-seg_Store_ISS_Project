package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.EstadoStock;
import pe.edu.utp.segitd.modelo.FilaReporteStock;
import pe.edu.utp.segitd.modelo.Producto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ReporteStockService {

    /** Unidades por encima del mínimo que aún se consideran "casi crítico". */
    private static final int MARGEN_PROXIMO_A_AGOTAR_UNIDADES = 5;

    private final ProductoDAO productoDAO = new ProductoDAO();

    public ReporteStock generarReporte() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            List<Producto> productos = productoDAO.listarActivos(conexion);

            List<FilaReporteStock> sinStock = new ArrayList<>();
            List<FilaReporteStock> criticos = new ArrayList<>();
            List<FilaReporteStock> disponibles = new ArrayList<>();

            for (Producto p : productos) {
                EstadoStock estado = clasificar(p);
                FilaReporteStock fila = new FilaReporteStock(
                        p.getCodigo(), p.getNombre(), p.getCategoria(), p.getTalla(),
                        p.getPrecio(), p.getStockComercial(), p.getStockMinimo(), estado);

                switch (estado) {
                    case SIN_STOCK -> sinStock.add(fila);
                    case CRITICO -> criticos.add(fila);
                    default -> disponibles.add(fila); // PROXIMO_A_AGOTAR o DISPONIBLE
                }
            }

            sinStock.sort(Comparator.comparing(FilaReporteStock::nombre));
            // Críticos: de menor a mayor stock comercial (el más urgente primero).
            criticos.sort(Comparator.comparingInt(FilaReporteStock::stockComercial)
                    .thenComparing(FilaReporteStock::nombre));
            // Disponibles: misma regla que críticos, de menor a mayor stock comercial,
            // así los "próximo a agotar" quedan naturalmente arriba sin tabla aparte.
            disponibles.sort(Comparator.comparingInt(FilaReporteStock::stockComercial)
                    .thenComparing(FilaReporteStock::nombre));

            return new ReporteStock(sinStock, criticos, disponibles);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo generar el reporte de stock.", e);
        }
    }

    /** Determina el estado de stock de un producto según su mínimo configurado. */
    private EstadoStock clasificar(Producto p) {
        int comercial = p.getStockComercial();
        int minimo = p.getStockMinimo();

        if (comercial <= 0) {
            return EstadoStock.SIN_STOCK;
        }
        if (comercial <= minimo) {
            return EstadoStock.CRITICO;
        }
        if (comercial <= minimo + MARGEN_PROXIMO_A_AGOTAR_UNIDADES) {
            return EstadoStock.PROXIMO_A_AGOTAR;
        }
        return EstadoStock.DISPONIBLE;
    }

    /** Resultado ya clasificado, en tres grupos independientes. */
    public record ReporteStock(List<FilaReporteStock> sinStock, List<FilaReporteStock> criticos,
                                List<FilaReporteStock> disponibles) {

        public int totalCriticos() {
            return criticos.size();
        }

        public int totalProximosAAgotar() {
            return (int) disponibles.stream().filter(f -> f.estado() == EstadoStock.PROXIMO_A_AGOTAR).count();
        }
    }
}