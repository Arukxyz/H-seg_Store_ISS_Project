package pe.edu.utp.segitd.servicio;

/** DTO de solo lectura con los indicadores en vivo del panel principal (sección 8, pantalla 2). */
public record IndicadoresDashboard(
        int productosActivos,
        int productosBajoStockMinimo,
        int pedidosWebPendientes,
        int donacionesPendientes,
        int lotesEnRuta
) {
}
