package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.EstadoVenta;
import pe.edu.utp.segitd.modelo.OrigenVenta;
import pe.edu.utp.segitd.modelo.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de venta y su detalle. El escritorio no crea ventas
 * (nacen en la web, "regla de oro" de la sección 1): solo las consulta,
 * confirma o anula (RF-04).
 */
public final class VentaDAO {

    /** Lista pedidos web, filtrables por estado y rango de fechas (los tres filtros son opcionales). */
    public List<Venta> listarWeb(EstadoVenta estado, OffsetDateTime desde, OffsetDateTime hasta, Connection conexion) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT v.*, c.nombre AS cliente_nombre
                  FROM venta v
                  LEFT JOIN cliente c ON c.id = v.id_cliente
                 WHERE v.origen = 'WEB'
                """);
        if (estado != null) {
            sql.append(" AND v.estado = ?");
        }
        if (desde != null) {
            sql.append(" AND v.fecha >= ?");
        }
        if (hasta != null) {
            sql.append(" AND v.fecha <= ?");
        }
        sql.append(" ORDER BY v.fecha DESC");

        try (PreparedStatement ps = conexion.prepareStatement(sql.toString())) {
            int indice = 1;
            if (estado != null) {
                ps.setString(indice++, estado.name());
            }
            if (desde != null) {
                ps.setObject(indice++, desde);
            }
            if (hasta != null) {
                ps.setObject(indice++, hasta);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Venta> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
                return resultado;
            }
        }
    }

    public Optional<Venta> buscarPorId(int id, Connection conexion) throws SQLException {
        String sql = """
                SELECT v.*, c.nombre AS cliente_nombre
                  FROM venta v
                  LEFT JOIN cliente c ON c.id = v.id_cliente
                 WHERE v.id = ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<DetalleVenta> listarDetalle(int idVenta, Connection conexion) throws SQLException {
        String sql = """
                SELECT dv.*, p.nombre AS producto_nombre
                  FROM detalle_venta dv
                  JOIN producto p ON p.codigo = dv.codigo_producto
                 WHERE dv.id_venta = ?
                 ORDER BY dv.id
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                List<DetalleVenta> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapearDetalle(rs));
                }
                return resultado;
            }
        }
    }

    /**
     * Cambia el estado de la venta. La aritmética de stock y la creación o
     * eliminación de donaciones NO ocurren aquí: las orquesta PedidoWebService
     * dentro de la misma transacción (sección 9.2).
     */
    public void actualizarEstado(int idVenta, EstadoVenta nuevoEstado, int idUsuario, Connection conexion) throws SQLException {
        String sql = "UPDATE venta SET estado = ?, id_usuario = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idUsuario);
            ps.setInt(3, idVenta);
            ps.executeUpdate();
        }
    }

    public int contarPendientes(Connection conexion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM venta WHERE origen = 'WEB' AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Venta mapear(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setId(rs.getInt("id"));
        venta.setCodigoComprobante(rs.getString("codigo_comprobante"));
        venta.setFecha(rs.getObject("fecha", OffsetDateTime.class));
        venta.setIdCliente((Integer) rs.getObject("id_cliente"));
        venta.setClienteNombre(rs.getString("cliente_nombre"));
        venta.setIdUsuario((Integer) rs.getObject("id_usuario"));
        venta.setOrigen(OrigenVenta.valueOf(rs.getString("origen")));
        venta.setEstado(EstadoVenta.valueOf(rs.getString("estado")));
        venta.setTotal(rs.getBigDecimal("total"));
        return venta;
    }

    private DetalleVenta mapearDetalle(ResultSet rs) throws SQLException {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(rs.getInt("id"));
        detalle.setIdVenta(rs.getInt("id_venta"));
        detalle.setCodigoProducto(rs.getString("codigo_producto"));
        detalle.setNombreProducto(rs.getString("producto_nombre"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        return detalle;
    }
}
