package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoDonacion;
import pe.edu.utp.segitd.modelo.FilaTrazabilidad;
import pe.edu.utp.segitd.modelo.TipoCompromiso;
import pe.edu.utp.segitd.modelo.FilaDonacionRiesgo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Agrupa las donaciones seleccionadas en un lote (RF-05). Solo afecta
     * las que sigan PENDIENTE (evita una condición de carrera si dos
     * usuarios arman un lote con la misma donación a la vez) y devuelve
     * cuántas se pudieron asignar realmente.
     */
    public int asignarALote(List<Integer> idsDonacion, int idLote, Connection conexion) throws SQLException {
        String sql = "UPDATE donacion SET estado = 'ASIGNADA', id_lote = ? WHERE id = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (Integer idDonacion : idsDonacion) {
                ps.setInt(1, idLote);
                ps.setInt(2, idDonacion);
                ps.addBatch();
            }
            int[] resultados = ps.executeBatch();
            int total = 0;
            for (int resultado : resultados) {
                total += Math.max(resultado, 0);
            }
            return total;
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

    /**
     * Trazabilidad para el reporte de impacto (RF-07): una fila por
     * donación con la venta que la originó y el lote/comunidad que la
     * entregó. Los filtros son opcionales; sin ellos trae todo el
     * historial, incluidas las donaciones aún no despachadas.
     */
    public List<FilaTrazabilidad> listarTrazabilidad(OffsetDateTime desde, OffsetDateTime hasta, Integer idComunidad,
                                                       Connection conexion) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT v.codigo_comprobante, v.fecha AS fecha_venta, p.nombre AS producto_nombre,
                       d.cantidad, d.tipo, d.estado, l.codigo_lote, c.nombre AS comunidad_nombre, l.fecha_despacho
                  FROM donacion d
                  JOIN detalle_venta dv ON dv.id = d.id_detalle_venta
                  JOIN venta v ON v.id = dv.id_venta
                  JOIN producto p ON p.codigo = d.codigo_producto
                  LEFT JOIN lote_donacion l ON l.id = d.id_lote
                  LEFT JOIN comunidad c ON c.id = l.id_comunidad
                 WHERE 1 = 1
                """);
        // Se filtra por fecha de VENTA: la hoja responde "qué pasó con las
        // ventas de este periodo", por eso una donación puede aparecer aún
        // PENDIENTE o ASIGNADA. Lo entregado en el periodo va en el historial.
        if (desde != null) {
            sql.append(" AND v.fecha >= ?");
        }
        if (hasta != null) {
            sql.append(" AND v.fecha <= ?");
        }
        if (idComunidad != null) {
            sql.append(" AND l.id_comunidad = ?");
        }
        sql.append(" ORDER BY v.fecha DESC");

        try (PreparedStatement ps = conexion.prepareStatement(sql.toString())) {
            int indice = 1;
            if (desde != null) {
                ps.setObject(indice++, desde);
            }
            if (hasta != null) {
                ps.setObject(indice++, hasta);
            }
            if (idComunidad != null) {
                ps.setInt(indice++, idComunidad);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<FilaTrazabilidad> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(new FilaTrazabilidad(
                            rs.getString("codigo_comprobante"),
                            rs.getObject("fecha_venta", OffsetDateTime.class),
                            rs.getString("producto_nombre"),
                            rs.getInt("cantidad"),
                            TipoCompromiso.valueOf(rs.getString("tipo")),
                            EstadoDonacion.valueOf(rs.getString("estado")),
                            rs.getString("codigo_lote"),
                            rs.getString("comunidad_nombre"),
                            rs.getObject("fecha_despacho", OffsetDateTime.class)));
                }
                return resultado;
            }
        }
    }

    /**
     * Cuántas donaciones (unidades) generaron las ventas del periodo, por
     * estado. Compara contra ResumenVentas.unidadesConCompromiso: si la
     * regla "una prenda vendida = una donación" se cumple, coinciden.
     */
    public Map<EstadoDonacion, Integer> contarUnidadesPorEstado(OffsetDateTime desde, OffsetDateTime hasta,
                                                                Connection conexion) throws SQLException {
        String sql = """
                SELECT d.estado, COALESCE(SUM(d.cantidad), 0) AS unidades
                  FROM donacion d
                  JOIN detalle_venta dv ON dv.id = d.id_detalle_venta
                  JOIN venta v          ON v.id = dv.id_venta
                 WHERE v.fecha >= ? AND v.fecha <= ?
                 GROUP BY d.estado
                """;
        Map<EstadoDonacion, Integer> resultado = new EnumMap<>(EstadoDonacion.class);
        for (EstadoDonacion estado : EstadoDonacion.values()) {
            resultado.put(estado, 0);
        }
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.put(EstadoDonacion.valueOf(rs.getString("estado")), rs.getInt("unidades"));
                }
            }
        }
        return resultado;
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


    /**
 * Donaciones PENDIENTE o ASIGNADA que llevan {@code diasUmbral} días o más
 * sin avanzar de estado (sección "donaciones en riesgo", valor agregado).
 * El reloj de una PENDIENTE corre desde que se creó; el de una ASIGNADA
 * corre desde que se armó su lote, no desde que nació la donación.
 */
public List<FilaDonacionRiesgo> listarEnRiesgo(int diasUmbral, Connection conexion) throws SQLException {
    String sql = """
            SELECT d.id, p.nombre AS producto_nombre, d.cantidad, d.tipo, d.estado,
                   EXTRACT(DAY FROM (now() - COALESCE(l.fecha_creacion, d.creado_en)))::int AS dias_en_riesgo,
                   l.codigo_lote, c.nombre AS comunidad_nombre
              FROM donacion d
              JOIN producto p ON p.codigo = d.codigo_producto
              LEFT JOIN lote_donacion l ON l.id = d.id_lote
              LEFT JOIN comunidad c ON c.id = l.id_comunidad
             WHERE d.estado IN ('PENDIENTE', 'ASIGNADA')
               AND (now() - COALESCE(l.fecha_creacion, d.creado_en)) >= make_interval(days => ?)
             ORDER BY dias_en_riesgo DESC
            """;
    try (PreparedStatement ps = conexion.prepareStatement(sql)) {
        ps.setInt(1, diasUmbral);
        try (ResultSet rs = ps.executeQuery()) {
            List<FilaDonacionRiesgo> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(new FilaDonacionRiesgo(
                        rs.getInt("id"),
                        rs.getString("producto_nombre"),
                        rs.getInt("cantidad"),
                        TipoCompromiso.valueOf(rs.getString("tipo")),
                        EstadoDonacion.valueOf(rs.getString("estado")),
                        rs.getInt("dias_en_riesgo"),
                        rs.getString("codigo_lote"),
                        rs.getString("comunidad_nombre")));
            }
            return resultado;
        }
    }
}
}
