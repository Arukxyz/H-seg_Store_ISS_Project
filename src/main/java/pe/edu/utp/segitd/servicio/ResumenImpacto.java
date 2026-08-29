package pe.edu.utp.segitd.servicio;

/** DTO con los totales de la hoja "Resumen de impacto" del reporte (RF-07). */
public record ResumenImpacto(
        int prendasDonadas,
        int arbolesPlantados,
        int comunidadesAtendidas,
        int lotesEntregados
) {
}
