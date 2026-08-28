package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.DonacionDAO;
import pe.edu.utp.segitd.dao.MovimientoDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.dao.VentaDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoDonacion;
import pe.edu.utp.segitd.modelo.EstadoVenta;
import pe.edu.utp.segitd.modelo.MovimientoInventario;
import pe.edu.utp.segitd.modelo.OrigenSistema;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.TipoMovimiento;
import pe.edu.utp.segitd.modelo.TipoStock;
import pe.edu.utp.segitd.modelo.Venta;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestión de pedidos provenientes de la web (RF-04). El escritorio no crea
 * ventas ("regla de oro" de la sección 1): solo las consulta, confirma o
 * anula. Confirmar mueve el stock y genera las donaciones pendientes;
 * anular revierte lo que corresponda (secciones 9.1, 9.2 y 9.3).
 *
 * Los datos de prueba ya traen donaciones PENDIENTE para simular pedidos
 * recién llegados sin depender de la web; por eso confirmarPedido crea la
 * donación solo si todavía no existe una para esa línea de detalle.
 */
public class PedidoWebService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final DonacionDAO donacionDAO = new DonacionDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    public List<Venta> listarPedidos(EstadoVenta estado, OffsetDateTime desde, OffsetDateTime hasta) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return ventaDAO.listarWeb(estado, desde, hasta, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar los pedidos web.", e);
        }
    }

    public List<DetalleVenta> listarDetalle(int idVenta) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return ventaDAO.listarDetalle(idVenta, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar el detalle del pedido.", e);
        }
    }

    public List<Donacion> listarDonaciones(int idVenta) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return donacionDAO.listarPorVenta(idVenta, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar las donaciones del pedido.", e);
        }
    }

    public void confirmarPedido(int idVenta, int idUsuario) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Venta venta = ventaDAO.buscarPorId(idVenta, conexion)
                        .orElseThrow(() -> new ServicioException("El pedido no existe."));
                if (venta.getEstado() != EstadoVenta.PENDIENTE) {
                    throw new ServicioException("Solo se pueden confirmar pedidos pendientes.");
                }

                List<DetalleVenta> detalle = ventaDAO.listarDetalle(idVenta, conexion);
                Set<Integer> detallesConDonacion = donacionDAO.listarPorVenta(idVenta, conexion).stream()
                        .map(Donacion::getIdDetalleVenta)
                        .collect(Collectors.toCollection(HashSet::new));

                for (DetalleVenta linea : detalle) {
                    Producto producto = productoDAO.buscarPorCodigo(linea.getCodigoProducto(), conexion)
                            .orElseThrow(() -> new ServicioException("Producto no encontrado: " + linea.getCodigoProducto()));

                    boolean descontado = productoDAO.descontarStockComercial(producto.getCodigo(), linea.getCantidad(), conexion);
                    if (!descontado) {
                        throw new ServicioException("Stock comercial insuficiente para " + producto.getNombre() + ".");
                    }
                    registrarMovimiento(producto.getCodigo(), TipoStock.COMERCIAL, TipoMovimiento.SALIDA,
                            -linea.getCantidad(), "VENTA:" + idVenta, idUsuario, conexion);

                    if (producto.isAplicaTripleImpacto() && producto.getTipoCompromiso() != null) {
                        productoDAO.incrementarStockComprometido(producto.getCodigo(), linea.getCantidad(), conexion);
                        registrarMovimiento(producto.getCodigo(), TipoStock.COMPROMETIDO, TipoMovimiento.INGRESO,
                                linea.getCantidad(), "VENTA:" + idVenta, idUsuario, conexion);

                        if (!detallesConDonacion.contains(linea.getId())) {
                            Donacion donacion = new Donacion();
                            donacion.setIdDetalleVenta(linea.getId());
                            donacion.setCodigoProducto(producto.getCodigo());
                            donacion.setCantidad(linea.getCantidad());
                            donacion.setTipo(producto.getTipoCompromiso());
                            donacion.setEstado(EstadoDonacion.PENDIENTE);
                            donacionDAO.crear(donacion, conexion);
                        }
                    }
                }

                ventaDAO.actualizarEstado(idVenta, EstadoVenta.PAGADO, idUsuario, conexion);
                conexion.commit();
            } catch (ServicioException | SQLException e) {
                conexion.rollback();
                if (e instanceof ServicioException se) {
                    throw se;
                }
                throw new ServicioException("No se pudo confirmar el pedido.", e);
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServicioException("No se pudo conectar a la base de datos.", e);
        }
    }

    public void anularPedido(int idVenta, int idUsuario) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Venta venta = ventaDAO.buscarPorId(idVenta, conexion)
                        .orElseThrow(() -> new ServicioException("El pedido no existe."));
                if (venta.getEstado() == EstadoVenta.ANULADO) {
                    throw new ServicioException("El pedido ya está anulado.");
                }

                List<Donacion> donaciones = donacionDAO.listarPorVenta(idVenta, conexion);
                boolean comprometidas = donaciones.stream().anyMatch(d -> d.getEstado() != EstadoDonacion.PENDIENTE);
                if (comprometidas) {
                    throw new ServicioException("No se puede anular: ya tiene donaciones asignadas a un lote de despacho.");
                }

                if (venta.getEstado() == EstadoVenta.PAGADO) {
                    for (DetalleVenta linea : ventaDAO.listarDetalle(idVenta, conexion)) {
                        Producto producto = productoDAO.buscarPorCodigo(linea.getCodigoProducto(), conexion)
                                .orElseThrow(() -> new ServicioException("Producto no encontrado: " + linea.getCodigoProducto()));

                        productoDAO.incrementarStockComercial(producto.getCodigo(), linea.getCantidad(), conexion);
                        registrarMovimiento(producto.getCodigo(), TipoStock.COMERCIAL, TipoMovimiento.INGRESO,
                                linea.getCantidad(), "ANULACION:" + idVenta, idUsuario, conexion);

                        if (producto.isAplicaTripleImpacto() && producto.getTipoCompromiso() != null) {
                            boolean descontado = productoDAO.descontarStockComprometido(producto.getCodigo(), linea.getCantidad(), conexion);
                            if (descontado) {
                                registrarMovimiento(producto.getCodigo(), TipoStock.COMPROMETIDO, TipoMovimiento.SALIDA,
                                        -linea.getCantidad(), "ANULACION:" + idVenta, idUsuario, conexion);
                            }
                        }
                    }
                }

                donacionDAO.eliminarPendientesPorVenta(idVenta, conexion);
                ventaDAO.actualizarEstado(idVenta, EstadoVenta.ANULADO, idUsuario, conexion);
                conexion.commit();
            } catch (ServicioException | SQLException e) {
                conexion.rollback();
                if (e instanceof ServicioException se) {
                    throw se;
                }
                throw new ServicioException("No se pudo anular el pedido.", e);
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServicioException("No se pudo conectar a la base de datos.", e);
        }
    }

    private void registrarMovimiento(String codigoProducto, TipoStock tipoStock, TipoMovimiento tipoMovimiento,
                                      int cantidad, String referencia, int idUsuario, Connection conexion) throws SQLException {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setCodigoProducto(codigoProducto);
        movimiento.setTipoStock(tipoStock);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setCantidad(cantidad);
        movimiento.setReferencia(referencia);
        movimiento.setOrigenSistema(OrigenSistema.ESCRITORIO);
        movimiento.setIdUsuario(idUsuario);
        movimientoDAO.registrar(movimiento, conexion);
    }
}
