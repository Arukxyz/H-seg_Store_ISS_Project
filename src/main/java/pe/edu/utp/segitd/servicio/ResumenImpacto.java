package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.modelo.ResumenVentas;

/**
 * DTO con los totales de la hoja "Resumen de impacto" del reporte (RF-07).
 *
 * Bloque 1 (impacto en comunidades) se calcula sobre lo ENTREGADO en el
 * periodo (fecha de despacho). Bloque 2 (ventas versus donaciones) se
 * calcula sobre las ventas del periodo (fecha de venta) y muestra en qué
 * estado están las donaciones que esas ventas generaron.
 */
public record ResumenImpacto(
        int prendasDonadas,
        int arbolesPlantados,
        int comunidadesAtendidas,
        int lotesEntregados,
        ResumenVentas ventas,
        int donacionesGeneradas,
        int donacionesPendientes,
        int donacionesAsignadas,
        int donacionesEntregadas
) {
    /** Porcentaje de unidades ya entregadas sobre las generadas en el periodo (0 si no hubo). */
    public double porcentajeCumplimiento() {
        return donacionesGeneradas == 0 ? 0 : 100.0 * donacionesEntregadas / donacionesGeneradas;
    }
}
