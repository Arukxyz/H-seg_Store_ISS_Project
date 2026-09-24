package pe.edu.utp.segitd.modelo;

import java.math.BigDecimal;

/**
 * Fila de solo lectura del reporte de stock (RF-03): se usa tanto para
 * pintar las tablas de ReporteStockJFrame como para las hojas del
 * Excel exportado por ExcelExporter.
 */
public record FilaReporteStock(
        String codigo,
        String nombre,
        String categoria,
        String talla,
        BigDecimal precio,
        int stockComercial,
        int stockMinimo,
        EstadoStock estado
) {
}
