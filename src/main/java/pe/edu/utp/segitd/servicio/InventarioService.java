package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.MovimientoDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.MovimientoInventario;
import pe.edu.utp.segitd.modelo.OrigenSistema;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.TipoMovimiento;
import pe.edu.utp.segitd.modelo.TipoStock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Reglas de negocio del catálogo y del inventario dual (RF-02 / RF-03,
 * sección 9.3). stock_comprometido no se toca desde aquí salvo por un
 * ajuste manual explícito: en el flujo normal lo mueven PedidoWebService
 * (al confirmar un pedido) y DespachoService (al entregar un lote).
 */
public class InventarioService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    public List<Producto> listarProductos() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return productoDAO.listarActivos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar el catálogo de productos.", e);
        }
    }

    public void crearProducto(Producto producto) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            if (productoDAO.existeCodigo(producto.getCodigo(), conexion)) {
                throw new ServicioException("Ya existe un producto con el código " + producto.getCodigo() + ".");
            }
            productoDAO.crear(producto, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo registrar el producto.", e);
        }
    }

    public void actualizarProducto(Producto producto) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            productoDAO.actualizarDatos(producto, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo actualizar el producto.", e);
        }
    }

    public void desactivarProducto(String codigo) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            productoDAO.desactivar(codigo, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo dar de baja el producto.", e);
        }
    }

    /**
     * Ajuste manual de stock: registra el movimiento de tipo AJUSTE en la
     * misma transacción que el cambio de stock (patrón de la sección 9.2).
     * delta puede ser positivo (ingreso) o negativo (salida).
     */
    public void ajustarStock(String codigoProducto, TipoStock tipoStock, int delta, String motivo, int idUsuario) {
        if (delta == 0) {
            throw new ServicioException("La cantidad de ajuste no puede ser cero.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                boolean exito = aplicarAjuste(codigoProducto, tipoStock, delta, conexion);
                if (!exito) {
                    throw new ServicioException("No hay stock suficiente para aplicar ese ajuste.");
                }

                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setCodigoProducto(codigoProducto);
                movimiento.setTipoStock(tipoStock);
                movimiento.setTipoMovimiento(TipoMovimiento.AJUSTE);
                movimiento.setCantidad(delta);
                movimiento.setMotivo(motivo);
                movimiento.setOrigenSistema(OrigenSistema.ESCRITORIO);
                movimiento.setIdUsuario(idUsuario);
                movimientoDAO.registrar(movimiento, conexion);

                conexion.commit();
            } catch (ServicioException | SQLException e) {
                conexion.rollback();
                if (e instanceof ServicioException se) {
                    throw se;
                }
                throw new ServicioException("No se pudo registrar el ajuste de stock.", e);
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServicioException("No se pudo conectar a la base de datos.", e);
        }
    }

    private boolean aplicarAjuste(String codigo, TipoStock tipoStock, int delta, Connection conexion) throws SQLException {
        return switch (tipoStock) {
            case COMERCIAL -> {
                if (delta > 0) {
                    productoDAO.incrementarStockComercial(codigo, delta, conexion);
                    yield true;
                }
                yield productoDAO.descontarStockComercial(codigo, -delta, conexion);
            }
            case COMPROMETIDO -> {
                if (delta > 0) {
                    productoDAO.incrementarStockComprometido(codigo, delta, conexion);
                    yield true;
                }
                yield productoDAO.descontarStockComprometido(codigo, -delta, conexion);
            }
        };
    }
}
