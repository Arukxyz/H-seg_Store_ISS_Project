package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.Ong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de las ONG aliadas para la entrega de donaciones. */
public final class OngDAO {

    public List<Ong> listarTodas(Connection conexion) throws SQLException {
        String sql = "SELECT * FROM ong ORDER BY nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Ong> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        }
    }

    public Optional<Ong> buscarPorId(int id, Connection conexion) throws SQLException {
        String sql = "SELECT * FROM ong WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Ong mapear(ResultSet rs) throws SQLException {
        Ong ong = new Ong();
        ong.setId(rs.getInt("id"));
        ong.setNombre(rs.getString("nombre"));
        ong.setContacto(rs.getString("contacto"));
        ong.setTelefono(rs.getString("telefono"));
        return ong;
    }
}
