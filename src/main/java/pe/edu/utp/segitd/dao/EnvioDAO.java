package pe.edu.utp.segitd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos del envío de un pedido web (script 08). La web crea la
 * fila junto con la venta; el escritorio solo la acompaña en la
 * confirmación y la anulación. Los pedidos anteriores al script 08 no
 * tienen envío: estos métodos no hacen nada sobre ellos.
 */
public final class EnvioDAO {

    /** Al confirmar el pago, el envío pendiente pasa a preparación. */
    public void marcarPreparando(int idVenta, Connection conexion) throws SQLException {
        String sql = """
                UPDATE envio SET estado = 'PREPARANDO'
                 WHERE id_venta = ? AND estado = 'PENDIENTE'
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        }
    }

    /** Indica si el pedido ya salió con el courier o fue entregado. */
    public boolean fueDespachado(int idVenta, Connection conexion) throws SQLException {
        String sql = """
                SELECT EXISTS (SELECT 1 FROM envio
                                WHERE id_venta = ? AND estado IN ('EN_CAMINO', 'ENTREGADO'))
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }

    /** Al anular el pedido, cancela el envío que aún no salió. */
    public void cancelarPorVenta(int idVenta, Connection conexion) throws SQLException {
        String sql = """
                UPDATE envio SET estado = 'CANCELADO'
                 WHERE id_venta = ? AND estado IN ('PENDIENTE', 'PREPARANDO')
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        }
    }
}
