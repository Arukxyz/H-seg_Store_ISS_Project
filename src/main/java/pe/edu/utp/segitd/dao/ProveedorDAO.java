package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.EstadoPedidoProveedor;
import pe.edu.utp.segitd.modelo.PedidoProveedor;
import pe.edu.utp.segitd.modelo.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de proveedor y sus pedidos de reposición (RF-06). */
public final class ProveedorDAO {

    public Proveedor crear(Proveedor proveedor, Connection conexion) throws SQLException {
        String sql = """
                INSERT INTO proveedor (nombre_taller, ruc, contacto, telefono)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proveedor.getNombreTaller());
            ps.setString(2, proveedor.getRuc());
            ps.setString(3, proveedor.getContacto());
            ps.setString(4, proveedor.getTelefono());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    proveedor.setId(claves.getInt(1));
                }
            }
        }
        return proveedor;
    }

    public List<Proveedor> listarActivos(Connection conexion) throws SQLException {
        String sql = "SELECT * FROM proveedor WHERE activo = TRUE ORDER BY nombre_taller";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Proveedor> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapearProveedor(rs));
            }
            return resultado;
        }
    }

    public void actualizar(Proveedor proveedor, Connection conexion) throws SQLException {
        String sql = "UPDATE proveedor SET nombre_taller = ?, ruc = ?, contacto = ?, telefono = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, proveedor.getNombreTaller());
            ps.setString(2, proveedor.getRuc());
            ps.setString(3, proveedor.getContacto());
            ps.setString(4, proveedor.getTelefono());
            ps.setInt(5, proveedor.getId());
            ps.executeUpdate();
        }
    }

    public void desactivar(int id, Connection conexion) throws SQLException {
        String sql = "UPDATE proveedor SET activo = FALSE WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public PedidoProveedor crearPedido(PedidoProveedor pedido, Connection conexion) throws SQLException {
        String sql = """
                INSERT INTO pedido_proveedor (id_proveedor, codigo_producto, descripcion, cantidad, estado, id_usuario)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedido.getIdProveedor());
            ps.setString(2, pedido.getCodigoProducto());
            ps.setString(3, pedido.getDescripcion());
            ps.setInt(4, pedido.getCantidad());
            ps.setString(5, pedido.getEstado().name());
            ps.setObject(6, pedido.getIdUsuario());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    pedido.setId(claves.getInt(1));
                }
            }
        }
        return pedido;
    }

    public Optional<PedidoProveedor> buscarPedidoPorId(int id, Connection conexion) throws SQLException {
        String sql = """
                SELECT pp.*, p.nombre_taller, pr.nombre AS producto_nombre
                  FROM pedido_proveedor pp
                  JOIN proveedor p ON p.id = pp.id_proveedor
                  LEFT JOIN producto pr ON pr.codigo = pp.codigo_producto
                 WHERE pp.id = ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearPedido(rs)) : Optional.empty();
            }
        }
    }

    public List<PedidoProveedor> listarPedidos(Connection conexion) throws SQLException {
        String sql = """
                SELECT pp.*, p.nombre_taller, pr.nombre AS producto_nombre
                  FROM pedido_proveedor pp
                  JOIN proveedor p ON p.id = pp.id_proveedor
                  LEFT JOIN producto pr ON pr.codigo = pp.codigo_producto
                 ORDER BY pp.fecha DESC
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PedidoProveedor> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapearPedido(rs));
            }
            return resultado;
        }
    }

    public void actualizarEstadoPedido(int idPedido, EstadoPedidoProveedor nuevoEstado, Connection conexion) throws SQLException {
        String sql = "UPDATE pedido_proveedor SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(rs.getInt("id"));
        proveedor.setNombreTaller(rs.getString("nombre_taller"));
        proveedor.setRuc(rs.getString("ruc"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setActivo(rs.getBoolean("activo"));
        return proveedor;
    }

    private PedidoProveedor mapearPedido(ResultSet rs) throws SQLException {
        PedidoProveedor pedido = new PedidoProveedor();
        pedido.setId(rs.getInt("id"));
        pedido.setIdProveedor(rs.getInt("id_proveedor"));
        pedido.setNombreTaller(rs.getString("nombre_taller"));
        pedido.setCodigoProducto(rs.getString("codigo_producto"));
        pedido.setNombreProducto(rs.getString("producto_nombre"));
        pedido.setDescripcion(rs.getString("descripcion"));
        pedido.setCantidad(rs.getInt("cantidad"));
        pedido.setFecha(rs.getObject("fecha", OffsetDateTime.class));
        pedido.setEstado(EstadoPedidoProveedor.valueOf(rs.getString("estado")));
        pedido.setIdUsuario((Integer) rs.getObject("id_usuario"));
        return pedido;
    }
}
