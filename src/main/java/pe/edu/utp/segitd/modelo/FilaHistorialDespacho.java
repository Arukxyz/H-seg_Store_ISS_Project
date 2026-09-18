package pe.edu.utp.segitd.modelo;

import java.time.OffsetDateTime;

/**
 * Una fila de la hoja "Historial de despachos" del reporte de impacto
 * (RF-07): un lote entregado a una comunidad de Cusco, con el consolidado
 * de lo que llevaba.
 */
public record FilaHistorialDespacho(
        String codigoLote,
        String comunidad,
        String distrito,
        String provincia,
        String ong,
        String responsable,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaDespacho,
        EstadoLote estado,
        int donaciones,
        int prendasAbrigo,
        int arboles
) {
}
