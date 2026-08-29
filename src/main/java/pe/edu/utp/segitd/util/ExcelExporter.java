package pe.edu.utp.segitd.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.edu.utp.segitd.modelo.FilaTrazabilidad;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.servicio.ResumenImpacto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Genera el reporte de impacto en .xlsx con Apache POI (RF-07). */
public final class ExcelExporter {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public File exportarReporteImpacto(ResumenImpacto resumen, List<FilaTrazabilidad> trazabilidad,
                                        List<Producto> inventario) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);

            escribirResumen(libro, estiloEncabezado, resumen);
            escribirTrazabilidad(libro, estiloEncabezado, trazabilidad);
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

    private void escribirResumen(Workbook libro, CellStyle estiloEncabezado, ResumenImpacto resumen) {
        Sheet hoja = libro.createSheet("Resumen de impacto");
        escribirEncabezado(hoja, estiloEncabezado, "Indicador", "Valor");

        int fila = escribirFilaResumen(hoja, 1, "Prendas donadas", resumen.prendasDonadas());
        fila = escribirFilaResumen(hoja, fila, "Árboles plantados", resumen.arbolesPlantados());
        fila = escribirFilaResumen(hoja, fila, "Comunidades atendidas", resumen.comunidadesAtendidas());
        escribirFilaResumen(hoja, fila, "Lotes entregados", resumen.lotesEntregados());

        autoajustarColumnas(hoja, 2);
    }

    private int escribirFilaResumen(Sheet hoja, int indiceFila, String etiqueta, int valor) {
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
        Row fila = hoja.createRow(0);
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

    private String formatear(java.time.OffsetDateTime fecha) {
        return fecha == null ? "" : FORMATO_FECHA.format(fecha);
    }
}
