package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.EstadoLote;
import pe.edu.utp.segitd.modelo.LoteDonacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de lote_donacion (RF-05). El código de lote (HSG-L001,
 * HSG-L002...) se genera a partir de la misma secuencia del id, para que
 * sea correlativo sin una segunda consulta de "último código + 1" que
 * sería propensa a condiciones de carrera.
 */
public final class LoteDAO {

    public LoteDonacion crear(LoteDonacion lote, Connection conexion) throws SQLException {
        int id = siguienteId(conexion);
        String codigoLote = "HSG-L%03d".formatted(id);

        String sql = """
                INSERT INTO lote_donacion
                    (id, codigo_lote, id_comunidad, id_ong, id_usuario_responsable, estado, observaciones)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, codigoLote);
            ps.setInt(3, lote.getIdComunidad());
            ps.setInt(4, lote.getIdOng());
            ps.setObject(5, lote.getIdUsuarioResponsable());
            ps.setString(6, lote.getEstado().name());
            ps.setString(7, lote.getObservaciones());
            ps.executeUpdate();
        }
        lote.setId(id);
        lote.setCodigoLote(codigoLote);
        return lote;
    }

    private int siguienteId(Connection conexion) throws SQLException {
        String sql = "SELECT nextval(pg_get_serial_sequence('lote_donacion', 'id'))";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return (int) rs.getLong(1);
        }
    }

    public Optional<LoteDonacion> buscarPorId(int id, Connection conexion) throws SQLException {
        String sql = """
                SELECT l.*, c.nombre AS comunidad_nombre, o.nombre AS ong_nombre
                  FROM lote_donacion l
                  JOIN comunidad c ON c.id = l.id_comunidad
                  JOIN ong o ON o.id = l.id_ong
                 WHERE l.id = ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<LoteDonacion> listarTodos(Connection conexion) throws SQLException {
        String sql = """
                SELECT l.*, c.nombre AS comunidad_nombre, o.nombre AS ong_nombre
                  FROM lote_donacion l
                  JOIN comunidad c ON c.id = l.id_comunidad
                  JOIN ong o ON o.id = l.id_ong
                 ORDER BY l.fecha_creacion DESC
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<LoteDonacion> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        }
    }

    public void actualizarEstado(int idLote, EstadoLote nuevoEstado, Connection conexion) throws SQLException {
        String sql = nuevoEstado == EstadoLote.ENTREGADO
                ? "UPDATE lote_donacion SET estado = ?, fecha_despacho = now() WHERE id = ?"
                : "UPDATE lote_donacion SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idLote);
            ps.executeUpdate();
        }
    }

    public int contarEnRuta(Connection conexion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM lote_donacion WHERE estado = 'EN_RUTA'";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private LoteDonacion mapear(ResultSet rs) throws SQLException {
        LoteDonacion lote = new LoteDonacion();
        lote.setId(rs.getInt("id"));
        lote.setCodigoLote(rs.getString("codigo_lote"));
        lote.setIdComunidad(rs.getInt("id_comunidad"));
        lote.setComunidadNombre(rs.getString("comunidad_nombre"));
        lote.setIdOng(rs.getInt("id_ong"));
        lote.setOngNombre(rs.getString("ong_nombre"));
        lote.setIdUsuarioResponsable((Integer) rs.getObject("id_usuario_responsable"));
        lote.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        lote.setFechaDespacho(rs.getObject("fecha_despacho", OffsetDateTime.class));
        lote.setEstado(EstadoLote.valueOf(rs.getString("estado")));
        lote.setObservaciones(rs.getString("observaciones"));
        return lote;
    }
}
