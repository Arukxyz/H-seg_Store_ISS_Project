package pe.edu.utp.segitd.modelo;

/**
 * Clasificación de demanda de un producto según su consumo real (salidas
 * comerciales) de los últimos 30 días. SIN_MOVIMIENTO es la señal de "no
 * tiene caso reponer esto ahora", aunque esté bajo su stock mínimo.
 */
public record FilaDemandaProducto(
        String codigoProducto,
        String nombreProducto,
        int stockComercial,
        int stockMinimoActual,
        double consumoPromedioDiario,
        CategoriaDemanda categoria
) {
}