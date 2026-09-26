package pe.edu.utp.segitd.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.edu.utp.segitd.modelo.FilaHistorialDespacho;
import pe.edu.utp.segitd.modelo.FilaTrazabilidad;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.servicio.ResumenImpacto;
import pe.edu.utp.segitd.modelo.FilaDonacionRiesgo;
import pe.edu.utp.segitd.modelo.FilaDemandaProducto;
import pe.edu.utp.segitd.modelo.FilaRankingProducto;
import pe.edu.utp.segitd.modelo.IndicadorVentasPeriodo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera el reporte de impacto en .xlsx con Apache POI (RF-07). Hojas:
 * Resumen de impacto (indicadores + ventas versus donaciones),
 * Trazabilidad (una fila por donación), Historial de despachos (lotes
 * entregados en orden cronológico) e Inventario al momento del corte.
 */
public final class ExcelExporter {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public File exportarReporteImpacto(OffsetDateTime desde, OffsetDateTime hasta, ResumenImpacto resumen,
                                        List<FilaTrazabilidad> trazabilidad, List<FilaHistorialDespacho> historial,
                                        List<Producto> inventario) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
            CellStyle estiloSeccion = crearEstiloSeccion(libro);

            escribirResumen(libro, estiloEncabezado, estiloSeccion, resumen, desde, hasta);
            escribirTrazabilidad(libro, estiloEncabezado, trazabilidad);
            escribirHistorialDespachos(libro, estiloEncabezado, historial);
            escribirInventario(libro, estiloEncabezado, inventario);

            File archivo = new File("reporte_impacto_" + FORMATO_ARCHIVO.format(LocalDateTime.now()) + ".xlsx");
            try (FileOutputStream salida = new FileOutputStream(archivo)) {
                libro.write(salida);
            }
            return archivo;
        }
    }

    private CellStyle crearEstiloEncabezado(Workbook libro) {
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle crearEstiloSeccion(Workbook libro) {
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 12);
        fuente.setColor(IndexedColors.DARK_GREEN.getIndex());

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.LEFT);
        return estilo;
    }

    private void escribirResumen(Workbook libro, CellStyle estiloEncabezado, CellStyle estiloSeccion,
                                 ResumenImpacto resumen, OffsetDateTime desde, OffsetDateTime hasta) {
        Sheet hoja = libro.createSheet("Resumen de impacto");

        int fila = 0;
        fila = escribirTituloSeccion(hoja, estiloSeccion, fila, "Impacto en comunidades (entregado en el periodo)");
        escribirEncabezadoEnFila(hoja, estiloEncabezado, fila++, "Indicador", "Valor");
        fila = escribirFilaResumen(hoja, fila, "Prendas de abrigo donadas", resumen.prendasDonadas());
        fila = escribirFilaResumen(hoja, fila, "Árboles nativos plantados", resumen.arbolesPlantados());
        fila = escribirFilaResumen(hoja, fila, "Comunidades atendidas", resumen.comunidadesAtendidas());
        fila = escribirFilaResumen(hoja, fila, "Lotes entregados", resumen.lotesEntregados());

        fila++;
        fila = escribirTituloSeccion(hoja, estiloSeccion, fila, "Ventas versus donaciones (ventas del periodo)");
        escribirEncabezadoEnFila(hoja, estiloEncabezado, fila++, "Indicador", "Valor");
        fila = escribirFilaResumen(hoja, fila, "Pedidos web confirmados", resumen.ventas().pedidosConfirmados());
        fila = escribirFilaResumen(hoja, fila, "Unidades vendidas", resumen.ventas().unidadesVendidas());
        fila = escribirFilaResumen(hoja, fila, "Unidades con compromiso social (Buy One, Give One / Plant One)",
                resumen.ventas().unidadesConCompromiso());
        fila = escribirFilaResumen(hoja, fila, "Monto vendido (S/)", resumen.ventas().montoVendido().doubleValue());
        fila = escribirFilaResumen(hoja, fila, "Donaciones generadas (unidades)", resumen.donacionesGeneradas());
        fila = escribirFilaResumen(hoja, fila, "   · Pendientes de asignar", resumen.donacionesPendientes());
        fila = escribirFilaResumen(hoja, fila, "   · Asignadas a un lote", resumen.donacionesAsignadas());
        fila = escribirFilaResumen(hoja, fila, "   · Entregadas", resumen.donacionesEntregadas());
        fila = escribirFilaResumen(hoja, fila, "Cumplimiento de entrega (%)",
                Math.round(resumen.porcentajeCumplimiento() * 10) / 10.0);

        fila++;
        hoja.createRow(fila).createCell(0).setCellValue(
                "Periodo: " + FORMATO_DIA.format(desde) + " al " + FORMATO_DIA.format(hasta));
        hoja.createRow(fila + 1).createCell(0).setCellValue(
                "Generado el " + FORMATO_FECHA.format(LocalDateTime.now()) + " por SEGITD-HÖSÉG");

        autoajustarColumnas(hoja, 2);
    }

    private int escribirTituloSeccion(Sheet hoja, CellStyle estilo, int indiceFila, String titulo) {
        Cell celda = hoja.createRow(indiceFila).createCell(0);
        celda.setCellValue(titulo);
        celda.setCellStyle(estilo);
        return indiceFila + 1;
    }

    private int escribirFilaResumen(Sheet hoja, int indiceFila, String etiqueta, double valor) {
        Row fila = hoja.createRow(indiceFila);
        fila.createCell(0).setCellValue(etiqueta);
        fila.createCell(1).setCellValue(valor);
        return indiceFila + 1;
    }

    private void escribirTrazabilidad(Workbook libro, CellStyle estiloEncabezado, List<FilaTrazabilidad> filas) {
        Sheet hoja = libro.createSheet("Trazabilidad");
        escribirEncabezado(hoja, estiloEncabezado,
                "Comprobante", "Fecha venta", "Producto", "Cantidad", "Tipo", "Estado", "Lote", "Comunidad", "Fecha entrega");

        int indiceFila = 1;
        for (FilaTrazabilidad f : filas) {
            Row fila = hoja.createRow(indiceFila++);
            fila.createCell(0).setCellValue(f.comprobanteVenta());
            fila.createCell(1).setCellValue(formatear(f.fechaVenta()));
            fila.createCell(2).setCellValue(f.producto());
            fila.createCell(3).setCellValue(f.cantidad());
            fila.createCell(4).setCellValue(f.tipo().name());
            fila.createCell(5).setCellValue(f.estado().name());
            fila.createCell(6).setCellValue(f.codigoLote() == null ? "" : f.codigoLote());
            fila.createCell(7).setCellValue(f.comunidad() == null ? "" : f.comunidad());
            fila.createCell(8).setCellValue(formatear(f.fechaEntrega()));
        }
        autoajustarColumnas(hoja, 9);
    }

    private void escribirHistorialDespachos(Workbook libro, CellStyle estiloEncabezado, List<FilaHistorialDespacho> filas) {
        Sheet hoja = libro.createSheet("Historial de despachos");
        escribirEncabezado(hoja, estiloEncabezado,
                "Lote", "Comunidad", "Distrito", "Provincia", "ONG", "Responsable",
                "Fecha creación", "Fecha entrega", "Estado", "Donaciones", "Prendas de abrigo", "Árboles");

        int indiceFila = 1;
        for (FilaHistorialDespacho f : filas) {
            Row fila = hoja.createRow(indiceFila++);
            fila.createCell(0).setCellValue(f.codigoLote());
            fila.createCell(1).setCellValue(f.comunidad());
            fila.createCell(2).setCellValue(nvl(f.distrito()));
            fila.createCell(3).setCellValue(nvl(f.provincia()));
            fila.createCell(4).setCellValue(f.ong());
            fila.createCell(5).setCellValue(nvl(f.responsable()));
            fila.createCell(6).setCellValue(formatear(f.fechaCreacion()));
            fila.createCell(7).setCellValue(formatear(f.fechaDespacho()));
            fila.createCell(8).setCellValue(f.estado().name());
            fila.createCell(9).setCellValue(f.donaciones());
            fila.createCell(10).setCellValue(f.prendasAbrigo());
            fila.createCell(11).setCellValue(f.arboles());
        }
        autoajustarColumnas(hoja, 12);
    }

    private void escribirInventario(Workbook libro, CellStyle estiloEncabezado, List<Producto> productos) {
        Sheet hoja = libro.createSheet("Inventario");
        escribirEncabezado(hoja, estiloEncabezado,
                "Código", "Nombre", "Categoría", "Stock Comercial", "Stock Comprometido", "Stock Mínimo");

        int indiceFila = 1;
        for (Producto p : productos) {
            Row fila = hoja.createRow(indiceFila++);
            fila.createCell(0).setCellValue(p.getCodigo());
            fila.createCell(1).setCellValue(p.getNombre());
            fila.createCell(2).setCellValue(p.getCategoria());
            fila.createCell(3).setCellValue(p.getStockComercial());
            fila.createCell(4).setCellValue(p.getStockComprometido());
            fila.createCell(5).setCellValue(p.getStockMinimo());
        }
        autoajustarColumnas(hoja, 6);
    }

    private void escribirEncabezado(Sheet hoja, CellStyle estilo, String... columnas) {
        escribirEncabezadoEnFila(hoja, estilo, 0, columnas);
    }

    private void escribirEncabezadoEnFila(Sheet hoja, CellStyle estilo, int indiceFila, String... columnas) {
        Row fila = hoja.createRow(indiceFila);
        for (int i = 0; i < columnas.length; i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estilo);
        }
    }

    private void autoajustarColumnas(Sheet hoja, int cantidadColumnas) {
        for (int i = 0; i < cantidadColumnas; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    private String formatear(OffsetDateTime fecha) {
        return fecha == null ? "" : FORMATO_FECHA.format(fecha);
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }

    /** Exporta la tabla de donaciones en riesgo (valor agregado, sección 8 pantalla 5). */
public File exportarDonacionesRiesgo(List<FilaDonacionRiesgo> filas) throws IOException {
    try (XSSFWorkbook libro = new XSSFWorkbook()) {
        CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
        Sheet hoja = libro.createSheet("Donaciones en riesgo");
        escribirEncabezado(hoja, estiloEncabezado,
                "Producto", "Cantidad", "Tipo", "Estado", "Días en riesgo", "Lote", "Comunidad");

        int indiceFila = 1;
        for (FilaDonacionRiesgo f : filas) {
            Row fila = hoja.createRow(indiceFila++);
            fila.createCell(0).setCellValue(f.nombreProducto());
            fila.createCell(1).setCellValue(f.cantidad());
            fila.createCell(2).setCellValue(f.tipo().name());
            fila.createCell(3).setCellValue(f.estado().name());
            fila.createCell(4).setCellValue(f.diasEnRiesgo());
            fila.createCell(5).setCellValue(f.codigoLote() == null ? "" : f.codigoLote());
            fila.createCell(6).setCellValue(f.comunidadNombre() == null ? "" : f.comunidadNombre());
        }
        autoajustarColumnas(hoja, 7);

        File archivo = new File("reporte_donaciones_riesgo_" + FORMATO_ARCHIVO.format(LocalDateTime.now()) + ".xlsx");
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            libro.write(salida);
        }
        return archivo;
    }
}

public File exportarDashboardVentas(OffsetDateTime desde, OffsetDateTime hasta, IndicadorVentasPeriodo indicadores,
                                     List<FilaRankingProducto> ranking, List<FilaDemandaProducto> demanda) throws IOException {
    try (XSSFWorkbook libro = new XSSFWorkbook()) {
        CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
        CellStyle estiloSeccion = crearEstiloSeccion(libro);

        Sheet hojaIndicadores = libro.createSheet("Indicadores de ventas");
        int fila = escribirTituloSeccion(hojaIndicadores, estiloSeccion, 0,
                "Ventas del periodo " + FORMATO_DIA.format(desde) + " al " + FORMATO_DIA.format(hasta));
        escribirEncabezadoEnFila(hojaIndicadores, estiloEncabezado, fila++, "Indicador", "Valor");
        fila = escribirFilaResumen(hojaIndicadores, fila, "Pedidos confirmados", indicadores.pedidosConfirmados());
        fila = escribirFilaResumen(hojaIndicadores, fila, "Unidades vendidas", indicadores.unidadesVendidas());
        fila = escribirFilaResumen(hojaIndicadores, fila, "Monto vendido (S/)", indicadores.montoVendido().doubleValue());
        escribirFilaResumen(hojaIndicadores, fila, "Ticket promedio (S/)", indicadores.ticketPromedio().doubleValue());
        autoajustarColumnas(hojaIndicadores, 2);

        Sheet hojaRanking = libro.createSheet("Ranking de productos");
        escribirEncabezado(hojaRanking, estiloEncabezado, "Código", "Producto", "Unidades vendidas", "Ingreso generado (S/)");
        int indiceFila = 1;
        for (FilaRankingProducto r : ranking) {
            Row f = hojaRanking.createRow(indiceFila++);
            f.createCell(0).setCellValue(r.codigoProducto());
            f.createCell(1).setCellValue(r.nombreProducto());
            f.createCell(2).setCellValue(r.unidadesVendidas());
            f.createCell(3).setCellValue(r.ingresoGenerado().doubleValue());
        }
        autoajustarColumnas(hojaRanking, 4);

        Sheet hojaDemanda = libro.createSheet("Análisis de demanda");
        escribirEncabezado(hojaDemanda, estiloEncabezado,
                "Código", "Producto", "Stock comercial", "Stock mínimo", "Consumo prom. diario", "Categoría de demanda");
        indiceFila = 1;
        for (FilaDemandaProducto d : demanda) {
            Row f = hojaDemanda.createRow(indiceFila++);
            f.createCell(0).setCellValue(d.codigoProducto());
            f.createCell(1).setCellValue(d.nombreProducto());
            f.createCell(2).setCellValue(d.stockComercial());
            f.createCell(3).setCellValue(d.stockMinimoActual());
            f.createCell(4).setCellValue(Math.round(d.consumoPromedioDiario() * 100) / 100.0);
            f.createCell(5).setCellValue(d.categoria().name());
        }
        autoajustarColumnas(hojaDemanda, 6);

        File archivo = new File("dashboard_ventas_demanda_" + FORMATO_ARCHIVO.format(LocalDateTime.now()) + ".xlsx");
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            libro.write(salida);
        }
        return archivo;
    }
}

}
