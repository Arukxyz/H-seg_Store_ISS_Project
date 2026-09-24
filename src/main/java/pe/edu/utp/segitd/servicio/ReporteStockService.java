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

/**
 * Clasifica el catálogo activo según su nivel de stock comercial (RF-03) en
 * tres grupos independientes:
 *
 * <ul>
 *   <li>{@code sinStock}: no quedan unidades comerciales (stock = 0).</li>
 *   <li>{@code criticos}: el stock está en el mínimo configurado o por
 *       debajo.</li>
 *   <li>{@code disponibles}: el resto (incluye {@code PROXIMO_A_AGOTAR} y
 *       {@code DISPONIBLE}).</li>
 * </ul>
 *
 * Tanto "críticos" como "disponibles" se ordenan por el <b>margen</b> sobre
 * el mínimo ({@code stockComercial - stockMinimo}), de menor a mayor — no
 * por el stock comercial en bruto. Así, un producto con margen 4 se prioriza
 * antes que uno con margen 5, aunque este último tenga menos unidades en
 * términos absolutos: lo que importa es qué tan cerca está de su propio
 * mínimo, no la cantidad pura.
 *
 * El margen de "próximo a crítico" ({@link #MARGEN_PROXIMO_A_AGOTAR_UNIDADES})
 * es puramente interno: no se muestra ningún número en la interfaz, solo el
 * resaltado ámbar y la etiqueta de estado.
 */
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
            // Críticos: de menor a mayor MARGEN (stock comercial - stock mínimo), no stock crudo.
            criticos.sort(Comparator.comparingInt(this::margen).thenComparing(FilaReporteStock::nombre));
            // Disponibles: misma regla que críticos, así los "próximo a crítico" quedan
            // naturalmente arriba (menor margen) sin necesitar tabla aparte.
            disponibles.sort(Comparator.comparingInt(this::margen).thenComparing(FilaReporteStock::nombre));

            return new ReporteStock(sinStock, criticos, disponibles);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo generar el reporte de stock.", e);
        }
    }

    /** Margen sobre el mínimo: negativo o cero en críticos, positivo en disponibles. */
    private int margen(FilaReporteStock f) {
        return f.stockComercial() - f.stockMinimo();
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