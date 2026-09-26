package pe.edu.utp.segitd.modelo;

import java.math.BigDecimal;

/** Una fila del ranking de más/menos vendidos del dashboard de Ventas y Demanda. */
public record FilaRankingProducto(String codigoProducto, String nombreProducto, int unidadesVendidas, BigDecimal ingresoGenerado) {
}