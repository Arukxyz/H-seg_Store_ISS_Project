package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.ComunidadDAO;
import pe.edu.utp.segitd.dao.DonacionDAO;
import pe.edu.utp.segitd.dao.LoteDAO;
import pe.edu.utp.segitd.dao.MovimientoDAO;
import pe.edu.utp.segitd.dao.OngDAO;
import pe.edu.utp.segitd.dao.ProductoDAO;
import pe.edu.utp.segitd.db.ConexionBD;
import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoLote;
import pe.edu.utp.segitd.modelo.LoteDonacion;
import pe.edu.utp.segitd.modelo.MovimientoInventario;
import pe.edu.utp.segitd.modelo.Ong;
import pe.edu.utp.segitd.modelo.OrigenSistema;
import pe.edu.utp.segitd.modelo.TipoMovimiento;
import pe.edu.utp.segitd.modelo.TipoStock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Gestión de lotes y destinos de donación (RF-05). El stock_comprometido
 * solo se descuenta cuando un lote pasa a ENTREGADO (sección 9.3); las
 * transiciones de estado siguen el flujo PENDIENTE → EN_RUTA → ENTREGADO.
 */
public class DespachoService {

    private final ComunidadDAO comunidadDAO = new ComunidadDAO();
    private final OngDAO ongDAO = new OngDAO();
    private final DonacionDAO donacionDAO = new DonacionDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    public List<Comunidad> listarComunidades() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return comunidadDAO.listarTodas(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar la lista de comunidades.", e);
        }
    }

    public List<Ong> listarOngs() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return ongDAO.listarTodas(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudo cargar la lista de ONG.", e);
        }
    }

    public List<Donacion> listarDonacionesPendientes() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return donacionDAO.listarPendientes(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar las donaciones pendientes.", e);
        }
    }

    public List<LoteDonacion> listarLotes() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return loteDAO.listarTodos(conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar los lotes.", e);
        }
    }

    public List<Donacion> listarDonacionesPorLote(int idLote) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return donacionDAO.listarPorLote(idLote, conexion);
        } catch (SQLException e) {
            throw new ServicioException("No se pudieron cargar las donaciones del lote.", e);
        }
    }

    public LoteDonacion crearLote(int idComunidad, int idOng, List<Integer> idsDonacion, int idUsuario) {
        if (idsDonacion == null || idsDonacion.isEmpty()) {
            throw new ServicioException("Selecciona al menos una donación para el lote.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                LoteDonacion lote = new LoteDonacion();
                lote.setIdComunidad(idComunidad);
                lote.setIdOng(idOng);
                lote.setIdUsuarioResponsable(idUsuario);
                lote.setEstado(EstadoLote.PENDIENTE);
                loteDAO.crear(lote, conexion);

                int asignadas = donacionDAO.asignarALote(idsDonacion, lote.getId(), conexion);
                if (asignadas != idsDonacion.size()) {
                    throw new ServicioException(
                            "Alguna de las donaciones seleccionadas ya no estaba disponible. Vuelve a cargar la lista e inténtalo de nuevo.");
                }

                conexion.commit();
                return lote;
            } catch (ServicioException | SQLException e) {
                conexion.rollback();
                if (e instanceof ServicioException se) {
                    throw se;
                }
                throw new ServicioException("No se pudo crear el lote.", e);
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServicioException("No se pudo conectar a la base de datos.", e);
        }
    }

    public void cambiarEstadoLote(int idLote, EstadoLote nuevoEstado, int idUsuario) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                LoteDonacion lote = loteDAO.buscarPorId(idLote, conexion)
                        .orElseThrow(() -> new ServicioException("El lote no existe."));

                validarTransicion(lote.getEstado(), nuevoEstado);

                if (nuevoEstado == EstadoLote.ENTREGADO) {
                    for (Donacion donacion : donacionDAO.listarPorLote(idLote, conexion)) {
                        boolean descontado = productoDAO.descontarStockComprometido(
                                donacion.getCodigoProducto(), donacion.getCantidad(), conexion);
                        if (!descontado) {
                            throw new ServicioException(
                                    "Stock comprometido insuficiente para " + donacion.getNombreProducto() + ".");
                        }
                        registrarMovimiento(donacion.getCodigoProducto(), donacion.getCantidad(),
                                lote.getCodigoLote(), idUsuario, conexion);
                    }
                    donacionDAO.marcarEntregadasPorLote(idLote, conexion);
                }

                loteDAO.actualizarEstado(idLote, nuevoEstado, conexion);
                conexion.commit();
            } catch (ServicioException | SQLException e) {
                conexion.rollback();
                if (e instanceof ServicioException se) {
                    throw se;
                }
                throw new ServicioException("No se pudo actualizar el estado del lote.", e);
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServicioException("No se pudo conectar a la base de datos.", e);
        }
    }

    private void validarTransicion(EstadoLote actual, EstadoLote nuevo) {
        boolean valido = switch (actual) {
            case PENDIENTE -> nuevo == EstadoLote.EN_RUTA;
            case EN_RUTA -> nuevo == EstadoLote.ENTREGADO;
            case ENTREGADO -> false;
        };
        if (!valido) {
            throw new ServicioException("No se puede pasar el lote de " + actual + " a " + nuevo + ".");
        }
    }

    private void registrarMovimiento(String codigoProducto, int cantidad, String codigoLote, int idUsuario,
                                      Connection conexion) throws SQLException {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setCodigoProducto(codigoProducto);
        movimiento.setTipoStock(TipoStock.COMPROMETIDO);
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
        movimiento.setCantidad(-cantidad);
        movimiento.setReferencia("LOTE:" + codigoLote);
        movimiento.setOrigenSistema(OrigenSistema.ESCRITORIO);
        movimiento.setIdUsuario(idUsuario);
        movimientoDAO.registrar(movimiento, conexion);
    }
}
