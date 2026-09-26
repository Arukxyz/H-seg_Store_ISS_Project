package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.MovimientoDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.dao.VentaDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.CategoriaDemanda;
import pe.edu.utp.segitd.modelo.FilaDemandaProducto;
import pe.edu.utp.segitd.modelo.FilaRankingProducto;
import pe.edu.utp.segitd.modelo.IndicadorVentasPeriodo;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.ResumenVentas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Dashboard de Ventas y Demanda (valor agregado): indicadores de ventas del
 * periodo, ranking de productos más/menos vendidos y clasificación de
 * demanda real por producto (sección 8, pantalla nueva). Reutiliza el mismo
 * consumo histórico de movimiento_inventario que InventarioService usa para
 * el stock mínimo sugerido, aquí a nivel de todo el catálogo.
 */
public class AnalisisVentasService {

    private static final int DIAS_VENTANA_CONSUMO = 30;
    private static final double UMBRAL_DEMANDA_ALTA = 0.5; // unidades/día

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    public IndicadorVentasPeriodo calcularIndicadores(OffsetDateTime desde, OffsetDateTime hasta) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            ResumenVentas r = ventaDAO.resumirVentasPagadas(desde, hasta, conexion);
            BigDecimal ticketPromedio = r.pedidosConfirmados() == 0
                    ? BigDecimal.ZERO
                    : r.montoVendido().divide(BigDecimal.valueOf(r.pedidosConfirmados()), 2, RoundingMode.HALF_UP);
            return new IndicadorVentasPeriodo(r.pedidosConfirmados(), r.unidadesVendidas(), r.montoVendido(), ticketPromedio);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron calcular los indicadores de ventas.", e);
        }
    }

    public List<FilaRankingProducto> rankingProductos(OffsetDateTime desde, OffsetDateTime hasta) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return ventaDAO.rankingProductosVendidos(desde, hasta, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo calcular el ranking de productos.", e);
        }
    }

    /** Ordenado de mayor a menor consumo: arriba lo de más demanda, al fondo lo SIN_MOVIMIENTO. */
    public List<FilaDemandaProducto> analizarDemanda() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            List<Producto> productos = productoDAO.listarActivos(conexion);
            Map<String, Double> consumo = movimientoDAO.mapaConsumoPromedioDiario(DIAS_VENTANA_CONSUMO, conexion);

            List<FilaDemandaProducto> resultado = new ArrayList<>();
            for (Producto p : productos) {
                double c = consumo.getOrDefault(p.getCodigo(), 0.0);
                CategoriaDemanda categoria = c <= 0 ? CategoriaDemanda.SIN_MOVIMIENTO
                        : c >= UMBRAL_DEMANDA_ALTA ? CategoriaDemanda.ALTA : CategoriaDemanda.NORMAL;
                resultado.add(new FilaDemandaProducto(
                        p.getCodigo(), p.getNombre(), p.getStockComercial(), p.getStockMinimo(), c, categoria));
            }
            resultado.sort(Comparator.comparingDouble(FilaDemandaProducto::consumoPromedioDiario).reversed());
            return resultado;
        } catch (SQLException e) {
            throw new ServicioException("No se pudo analizar la demanda de productos.", e);
        }
    }
}