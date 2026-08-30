package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.dao.BackupDAO;
import pe.edu.utp.segitd.db.ConexionBD;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Respaldo de tablas críticas a CSV (RNF-05). Se dispara desde el menú
 * (solo ADMINISTRADOR) o automáticamente al cerrar la aplicación si
 * pasaron más de 24 horas desde el último respaldo exitoso.
 */
public class BackupService {

    private static final String[] TABLAS = {
            "producto", "venta", "detalle_venta", "donacion", "lote_donacion", "movimiento_inventario"
    };
    private static final Path CARPETA_BACKUPS = Path.of("backups");
    private static final Path MARCADOR_ULTIMO = CARPETA_BACKUPS.resolve("ultimo_backup.txt");
    private static final DateTimeFormatter FORMATO_CARPETA = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final BackupDAO backupDAO = new BackupDAO();

    public Path ejecutarBackup() {
        try {
            Files.createDirectories(CARPETA_BACKUPS);
            Path carpetaDestino = CARPETA_BACKUPS.resolve("backup_" + FORMATO_CARPETA.format(LocalDateTime.now()));
            Files.createDirectories(carpetaDestino);

            try (Connection conexion = ConexionBD.obtenerConexion()) {
                for (String tabla : TABLAS) {
                    exportarTablaACsv(tabla, carpetaDestino, conexion);
                }
            }

            Files.writeString(MARCADOR_ULTIMO, LocalDateTime.now().toString(), StandardCharsets.UTF_8);
            return carpetaDestino;
        } catch (IOException | SQLException e) {
            throw new ServicioException("No se pudo generar el respaldo.", e);
        }
    }

    /** true si nunca se respaldó o si pasaron más de 24 horas desde el último respaldo exitoso. */
    public boolean debeRespaldarAutomaticamente() {
        if (!Files.exists(MARCADOR_ULTIMO)) {
            return true;
        }
        try {
            LocalDateTime ultimo = LocalDateTime.parse(Files.readString(MARCADOR_ULTIMO, StandardCharsets.UTF_8).trim());
            return ultimo.plusHours(24).isBefore(LocalDateTime.now());
        } catch (IOException | DateTimeParseException e) {
            return true;
        }
    }

    private void exportarTablaACsv(String tabla, Path carpetaDestino, Connection conexion) throws IOException, SQLException {
        BackupDAO.VolcadoTabla volcado = backupDAO.volcarTabla(tabla, conexion);
        Path archivo = carpetaDestino.resolve(tabla + ".csv");
        try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            escritor.write(String.join(",", volcado.columnas()));
            escritor.write("\n");
            for (Object[] fila : volcado.filas()) {
                escritor.write(formatearFilaCsv(fila));
                escritor.write("\n");
            }
        }
    }

    private String formatearFilaCsv(Object[] fila) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fila.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escaparCsv(fila[i]));
        }
        return sb.toString();
    }

    private String escaparCsv(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor.toString();
        boolean necesitaComillas = texto.contains(",") || texto.contains("\"") || texto.contains("\n");
        if (!necesitaComillas) {
            return texto;
        }
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
