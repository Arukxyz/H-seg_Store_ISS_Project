package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoDonacion;
import pe.edu.utp.segitd.modelo.TipoCompromiso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de donación: la tabla bisagra entre una línea de venta
 * y el lote logístico que finalmente la entrega (sección 5).
 */
public final class DonacionDAO {

    public Donacion crear(Donacion donacion, Connection conexion) throws SQLException {
        String sql = """
                INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, donacion.getIdDetalleVenta());
            ps.setString(2, donacion.getCodigoProducto());
            ps.setInt(3, donacion.getCantidad());
            ps.setString(4, donacion.getTipo().name());
            ps.setString(5, donacion.getEstado().name());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    donacion.setId(claves.getInt(1));
                }
            }
        }
        return donacion;
    }

    public List<Donacion> listarPorVenta(int idVenta, Connection conexion) throws SQLException {
        String sql = """
                SELECT d.*, p.nombre AS producto_nombre
                  FROM donacion d
                  JOIN detalle_venta dv ON dv.id = d.id_detalle_venta
                  JOIN producto p ON p.codigo = d.codigo_producto
                 WHERE dv.id_venta = ?
                 ORDER BY d.id
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                List<Donacion> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
                return resultado;
            }
        }
    }

    public List<Donacion> listarPendientes(Connection conexion) throws SQLException {
        String sql = """
                SELECT d.*, p.nombre AS producto_nombre
                  FROM donacion d
                  JOIN producto p ON p.codigo = d.codigo_producto
                 WHERE d.estado = 'PENDIENTE'
                 ORDER BY d.creado_en
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Donacion> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        }
    }

    public List<Donacion> listarPorLote(int idLote, Connection conexion) throws SQLException {
        String sql = """
                SELECT d.*, p.nombre AS producto_nombre
                  FROM donacion d
                  JOIN producto p ON p.codigo = d.codigo_producto
                 WHERE d.id_lote = ?
                 ORDER BY d.id
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                List<Donacion> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
                return resultado;
            }
        }
    }

    public int contarPendientes(Connection conexion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM donacion WHERE estado = 'PENDIENTE'";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Agrupa las donaciones seleccionadas en un lote (RF-05). */
    public void asignarALote(List<Integer> idsDonacion, int idLote, Connection conexion) throws SQLException {
        String sql = "UPDATE donacion SET estado = 'ASIGNADA', id_lote = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (Integer idDonacion : idsDonacion) {
                ps.setInt(1, idLote);
                ps.setInt(2, idDonacion);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Marca como entregadas todas las donaciones de un lote que pasó a ENTREGADO. */
    public void marcarEntregadasPorLote(int idLote, Connection conexion) throws SQLException {
        String sql = "UPDATE donacion SET estado = 'ENTREGADA' WHERE id_lote = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idLote);
            ps.executeUpdate();
        }
    }

    /** Al anular un pedido web, elimina las donaciones que aún no fueron asignadas a un lote. */
    public void eliminarPendientesPorVenta(int idVenta, Connection conexion) throws SQLException {
        String sql = """
                DELETE FROM donacion
                 WHERE estado = 'PENDIENTE'
                   AND id_detalle_venta IN (SELECT id FROM detalle_venta WHERE id_venta = ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        }
    }

    private Donacion mapear(ResultSet rs) throws SQLException {
        Donacion donacion = new Donacion();
        donacion.setId(rs.getInt("id"));
        donacion.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
        donacion.setCodigoProducto(rs.getString("codigo_producto"));
        donacion.setNombreProducto(rs.getString("producto_nombre"));
        donacion.setCantidad(rs.getInt("cantidad"));
        donacion.setTipo(TipoCompromiso.valueOf(rs.getString("tipo")));
        donacion.setEstado(EstadoDonacion.valueOf(rs.getString("estado")));
        donacion.setIdLote((Integer) rs.getObject("id_lote"));
        donacion.setCreadoEn(rs.getObject("creado_en", OffsetDateTime.class));
        return donacion;
    }
}
