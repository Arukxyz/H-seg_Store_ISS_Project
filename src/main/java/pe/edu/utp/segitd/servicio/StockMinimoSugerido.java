package pe.edu.utp.segitd.servicio;

/**
 * Resultado del cálculo de stock mínimo sugerido (ver InventarioService).
 * {@code conHistorial} indica si el cálculo usó ventas reales, o si el
 * producto es nuevo / no tiene ventas en la ventana analizada, en cuyo
 * caso se devuelve un valor de referencia fijo en lugar de un cálculo.
 */
public record StockMinimoSugerido(int valorSugerido, double consumoPromedioDiario, boolean conHistorial) {
}