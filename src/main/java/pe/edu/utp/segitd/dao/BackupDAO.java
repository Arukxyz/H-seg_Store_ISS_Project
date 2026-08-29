package pe.edu.utp.segitd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Exportación genérica de tablas completas para el respaldo (RNF-05).
 * El nombre de tabla se concatena en el SQL porque JDBC no permite
 * parametrizar identificadores, pero solo se acepta si está en la lista
 * blanca fija: nunca proviene de entrada de usuario.
 */
public final class BackupDAO {

    private static final Set<String> TABLAS_PERMITIDAS = Set.of(
            "producto", "venta", "detalle_venta", "donacion", "lote_donacion", "movimiento_inventario");

    public record VolcadoTabla(List<String> columnas, List<Object[]> filas) {
    }

    public VolcadoTabla volcarTabla(String nombreTabla, Connection conexion) throws SQLException {
        if (!TABLAS_PERMITIDAS.contains(nombreTabla)) {
            throw new IllegalArgumentException("Tabla no permitida para respaldo: " + nombreTabla);
        }
        String sql = "SELECT * FROM " + nombreTabla;
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metadata = rs.getMetaData();
            int cantidadColumnas = metadata.getColumnCount();

            List<String> columnas = new ArrayList<>();
            for (int i = 1; i <= cantidadColumnas; i++) {
                columnas.add(metadata.getColumnName(i));
            }

            List<Object[]> filas = new ArrayList<>();
            while (rs.next()) {
                Object[] fila = new Object[cantidadColumnas];
                for (int i = 0; i < cantidadColumnas; i++) {
                    fila[i] = rs.getObject(i + 1);
                }
                filas.add(fila);
            }
            return new VolcadoTabla(columnas, filas);
        }
    }
}
