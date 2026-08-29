package pe.edu.utp.segitd.modelo;

import java.time.OffsetDateTime;

/**
 * Una fila de la hoja "Trazabilidad" del reporte de impacto (RF-07):
 * qué venta generó qué donación y en qué lote terminó entregándose.
 * Es la respuesta directa al problema planteado en la sección 1.
 */
public record FilaTrazabilidad(
        String comprobanteVenta,
        OffsetDateTime fechaVenta,
        String producto,
        int cantidad,
        TipoCompromiso tipo,
        EstadoDonacion estado,
        String codigoLote,
        String comunidad,
        OffsetDateTime fechaEntrega
) {
}
