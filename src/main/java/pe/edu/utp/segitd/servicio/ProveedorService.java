package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.MovimientoDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.dao.ProveedorDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.EstadoPedidoProveedor;
import pe.edu.utp.segitd.modelo.MovimientoInventario;
import pe.edu.utp.segitd.modelo.OrigenSistema;
import pe.edu.utp.segitd.modelo.PedidoProveedor;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.Proveedor;
import pe.edu.utp.segitd.modelo.TipoMovimiento;
import pe.edu.utp.segitd.modelo.TipoStock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Proveedores y pedidos de reposición (RF-06). Al marcar un pedido como
 * RECIBIDO, suma al stock_comercial del producto asociado y registra el
 * movimiento de INGRESO correspondiente, todo en una sola transacción.
 */
public class ProveedorService {

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    public List<Proveedor> listarProveedores() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return proveedorDAO.listarActivos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar la lista de proveedores.", e);
        }
    }

    public List<Producto> listarProductos() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return productoDAO.listarActivos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar el catálogo de productos.", e);
        }
    }

    public void crearProveedor(Proveedor proveedor) {
        if (proveedor.getNombreTaller() == null || proveedor.getNombreTaller().isBlank()) {
            throw new ServicioException("El nombre del taller es obligatorio.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            proveedorDAO.crear(proveedor, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo registrar el proveedor.", e);
        }
    }

    public void actualizarProveedor(Proveedor proveedor) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            proveedorDAO.actualizar(proveedor, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo actualizar el proveedor.", e);
        }
    }

    public void desactivarProveedor(int id) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            proveedorDAO.desactivar(id, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo dar de baja el proveedor.", e);
        }
    }

    public List<PedidoProveedor> listarPedidos() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return proveedorDAO.listarPedidos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar los pedidos a proveedores.", e);
        }
    }

    /** "Valida que proveedor y cantidad no estén vacíos" (sección 8, pantalla 7). */
    public void crearPedido(int idProveedor, String codigoProducto, String descripcion, int cantidad, int idUsuario) {
        if (idProveedor <= 0) {
            throw new ServicioException("Selecciona un proveedor.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ServicioException("La descripción es obligatoria.");
        }
        if (cantidad <= 0) {
            throw new ServicioException("La cantidad debe ser mayor a cero.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            PedidoProveedor pedido = new PedidoProveedor();
            pedido.setIdProveedor(idProveedor);
            pedido.setCodigoProducto(codigoProducto);
            pedido.setDescripcion(descripcion);
            pedido.setCantidad(cantidad);
            pedido.setEstado(EstadoPedidoProveedor.SOLICITADO);
            pedido.setIdUsuario(idUsuario);
            proveedorDAO.crearPedido(pedido, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo registrar el pedido.", e);
        }
    }

    public void marcarRecibido(int idPedido, int idUsuario) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                PedidoProveedor pedido = proveedorDAO.buscarPedidoPorId(idPedido, conexion)
                        .orElseThrow(() -> new ServicioException("El pedido no existe."));
                if (pedido.getEstado() != EstadoPedidoProveedor.SOLICITADO) {
                    throw new ServicioException("Solo se pueden recibir pedidos en estado SOLICITADO.");
                }
                if (pedido.getCodigoProducto() == null) {
                    throw new ServicioException(
                            "Este pedido no tiene un producto de catálogo asociado; no se puede actualizar el stock automáticamente.");
                }

                Producto producto = productoDAO.buscarPorCodigo(pedido.getCodigoProducto(), conexion)
                        .orElseThrow(() -> new ServicioException("Producto no encontrado: " + pedido.getCodigoProducto()));

                productoDAO.incrementarStockComercial(producto.getCodigo(), pedido.getCantidad(), conexion);

                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setCodigoProducto(producto.getCodigo());
                movimiento.setTipoStock(TipoStock.COMERCIAL);
                movimiento.setTipoMovimiento(TipoMovimiento.INGRESO);
                movimiento.setCantidad(pedido.getCantidad());
                movimiento.setReferencia("PEDIDO_PROV:" + idPedido);
                movimiento.setOrigenSistema(OrigenSistema.ESCRITORIO);
                movimiento.setIdUsuario(idUsuario);
                movimientoDAO.registrar(movimiento, conexion);

                proveedorDAO.actualizarEstadoPedido(idPedido, EstadoPedidoProveedor.RECIBIDO, conexion);
                conexion.commit();
            } catch (ServicioException | SQLException e) {
                conexion.rollback();
                if (e instanceof ServicioException se) {
                    throw se;
                }
                throw new ServicioException("No se pudo marcar el pedido como recibido.", e);
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServicioException("No se pudo conectar a la base de datos.", e);
        }
    }

    public void anularPedido(int idPedido) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            PedidoProveedor pedido = proveedorDAO.buscarPedidoPorId(idPedido, conexion)
                    .orElseThrow(() -> new ServicioException("El pedido no existe."));
            if (pedido.getEstado() != EstadoPedidoProveedor.SOLICITADO) {
                throw new ServicioException("Solo se pueden anular pedidos en estado SOLICITADO.");
            }
            proveedorDAO.actualizarEstadoPedido(idPedido, EstadoPedidoProveedor.ANULADO, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo anular el pedido.", e);
        }
    }
}
