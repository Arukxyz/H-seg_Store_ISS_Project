package pe.edu.utp.segitd.modelo;

import java.math.BigDecimal;

/**
 * Consolidado de ventas web confirmadas (PAGADO) en un periodo, para la
 * comparación "ventas versus donaciones" del reporte de impacto (RF-07).
 * {@code unidadesConCompromiso} cuenta solo prendas y accesorios con
 * aplica_triple_impacto: cada una debió generar una donación.
 */
public record ResumenVentas(int pedidosConfirmados, int unidadesVendidas, int unidadesConCompromiso, BigDecimal montoVendido) {
}
