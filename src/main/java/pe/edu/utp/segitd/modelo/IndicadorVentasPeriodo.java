package pe.edu.utp.segitd.modelo;

import java.math.BigDecimal;

/** Indicadores de ventas (no financieros/contables) para el dashboard de Ventas y Demanda. */
public record IndicadorVentasPeriodo(int pedidosConfirmados, int unidadesVendidas, BigDecimal montoVendido, BigDecimal ticketPromedio) {
}