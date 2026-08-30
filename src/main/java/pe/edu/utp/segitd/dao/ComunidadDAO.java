package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.Comunidad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de las comunidades altoandinas destino de las donaciones. */
public final class ComunidadDAO {

    public List<Comunidad> listarTodas(Connection conexion) throws SQLException {
        String sql = "SELECT * FROM comunidad ORDER BY nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Comunidad> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        }
    }

    public Optional<Comunidad> buscarPorId(int id, Connection conexion) throws SQLException {
        String sql = "SELECT * FROM comunidad WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Comunidad mapear(ResultSet rs) throws SQLException {
        Comunidad comunidad = new Comunidad();
        comunidad.setId(rs.getInt("id"));
        comunidad.setNombre(rs.getString("nombre"));
        comunidad.setDistrito(rs.getString("distrito"));
        comunidad.setProvincia(rs.getString("provincia"));
        comunidad.setRegion(rs.getString("region"));
        return comunidad;
    }
}
