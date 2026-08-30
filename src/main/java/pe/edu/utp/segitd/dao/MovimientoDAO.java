package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.MovimientoInventario;
import pe.edu.utp.segitd.modelo.OrigenSistema;
import pe.edu.utp.segitd.modelo.TipoMovimiento;
import pe.edu.utp.segitd.modelo.TipoStock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bitácora de inventario. Toda variación de stock_comercial o
 * stock_comprometido inserta una fila aquí, sin excepciones (sección 9.3).
 */
public final class MovimientoDAO {

    public MovimientoInventario registrar(MovimientoInventario movimiento, Connection conexion) throws SQLException {
        String sql = """
                INSERT INTO movimiento_inventario
                    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo,
                     referencia, origen_sistema, id_usuario)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, movimiento.getCodigoProducto());
            ps.setString(2, movimiento.getTipoStock().name());
            ps.setString(3, movimiento.getTipoMovimiento().name());
            ps.setInt(4, movimiento.getCantidad());
            ps.setString(5, movimiento.getMotivo());
            ps.setString(6, movimiento.getReferencia());
            ps.setString(7, movimiento.getOrigenSistema().name());
            ps.setObject(8, movimiento.getIdUsuario());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    movimiento.setId(claves.getInt(1));
                }
            }
        }
        return movimiento;
    }

    public List<MovimientoInventario> listarPorProducto(String codigoProducto, Connection conexion) throws SQLException {
        String sql = "SELECT * FROM movimiento_inventario WHERE codigo_producto = ? ORDER BY fecha DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigoProducto);
            try (ResultSet rs = ps.executeQuery()) {
                List<MovimientoInventario> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
                return resultado;
            }
        }
    }

    private MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setId(rs.getInt("id"));
        movimiento.setCodigoProducto(rs.getString("codigo_producto"));
        movimiento.setTipoStock(TipoStock.valueOf(rs.getString("tipo_stock")));
        movimiento.setTipoMovimiento(TipoMovimiento.valueOf(rs.getString("tipo_movimiento")));
        movimiento.setCantidad(rs.getInt("cantidad"));
        movimiento.setMotivo(rs.getString("motivo"));
        movimiento.setReferencia(rs.getString("referencia"));
        movimiento.setOrigenSistema(OrigenSistema.valueOf(rs.getString("origen_sistema")));
        movimiento.setIdUsuario((Integer) rs.getObject("id_usuario"));
        movimiento.setFecha(rs.getObject("fecha", OffsetDateTime.class));
        return movimiento;
    }
}
