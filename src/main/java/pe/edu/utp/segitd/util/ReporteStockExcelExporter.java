package pe.edu.utp.segitd.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.edu.utp.segitd.modelo.FilaReporteStock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera el reporte de stock (RF-03) en su propio archivo .xlsx,
 * independiente del Excel de trazabilidad/inventario ({@link ExcelExporter},
 * RF-07). Tres hojas: "Sin stock", "Críticos" y "Disponibles" (incluye
 * próximos a crítico, resaltados en ámbar).
 */
public final class ReporteStockExcelExporter {

    /** dd-MM-yyyy_HH-mm en vez de dígitos pegados, para que el nombre se lea bien. */
    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");

    public File exportar(List<FilaReporteStock> sinStock, List<FilaReporteStock> criticos,
                          List<FilaReporteStock> disponibles) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
            CellStyle estiloCritico = crearEstiloResaltado(libro, IndexedColors.ROSE.getIndex());
            CellStyle estiloProximo = crearEstiloResaltado(libro, IndexedColors.LIGHT_YELLOW.getIndex());

            escribirHojaSinStock(libro, estiloEncabezado, sinStock);
            escribirHojaCriticos(libro, estiloEncabezado, estiloCritico, criticos);
            escribirHojaDisponibles(libro, estiloEncabezado, estiloProximo, disponibles);

            File archivo = new File("reporte_stock_" + FORMATO_ARCHIVO.format(LocalDateTime.now()) + ".xlsx");
            try (FileOutputStream salida = new FileOutputStream(archivo)) {
                libro.write(salida);
            }
            return archivo;
        }
    }

    private void escribirHojaSinStock(Workbook libro, CellStyle estiloEncabezado, List<FilaReporteStock> filas) {
        Sheet hoja = libro.createSheet("Sin stock");
        escribirEncabezado(hoja, estiloEncabezado, "Código", "Nombre", "Categoría", "Talla", "Precio (S/)", "Stock mínimo");

        int indiceFila = 1;
        for (FilaReporteStock f : filas) {
            Row fila = hoja.createRow(indiceFila++);
            fila.createCell(0).setCellValue(f.codigo());
            fila.createCell(1).setCellValue(f.nombre());
            fila.createCell(2).setCellValue(nvl(f.categoria()));
            fila.createCell(3).setCellValue(nvl(f.talla()));
            fila.createCell(4).setCellValue(f.precio() == null ? 0 : f.precio().doubleValue());
            fila.createCell(5).setCellValue(f.stockMinimo());
        }
        autoajustarColumnas(hoja, 6);
    }

    /** Ya llega ordenada de menor a mayor margen (stock comercial - stock mínimo). */
    private void escribirHojaCriticos(Workbook libro, CellStyle estiloEncabezado, CellStyle estiloCritico,
                                       List<FilaReporteStock> filas) {
        Sheet hoja = libro.createSheet("Críticos");
        escribirEncabezado(hoja, estiloEncabezado,
                "Código", "Nombre", "Categoría", "Talla", "Precio (S/)", "Stock comercial", "Stock mínimo");

        int indiceFila = 1;
        for (FilaReporteStock f : filas) {
            Row fila = hoja.createRow(indiceFila++);
            fila.createCell(0).setCellValue(f.codigo());
            fila.createCell(1).setCellValue(f.nombre());
            fila.createCell(2).setCellValue(nvl(f.categoria()));
            fila.createCell(3).setCellValue(nvl(f.talla()));
            fila.createCell(4).setCellValue(f.precio() == null ? 0 : f.precio().doubleValue());
            fila.createCell(5).setCellValue(f.stockComercial());
            fila.createCell(6).setCellValue(f.stockMinimo());
            for (int c = 0; c <= 6; c++) {
                fila.getCell(c).setCellStyle(estiloCritico);
            }
        }
        autoajustarColumnas(hoja, 7);
    }

    /** Ya llega ordenada de menor a mayor margen (stock comercial - stock mínimo). */
    private void escribirHojaDisponibles(Workbook libro, CellStyle estiloEncabezado, CellStyle estiloProximo,
                                          List<FilaReporteStock> filas) {
        Sheet hoja = libro.createSheet("Disponibles");
        escribirEncabezado(hoja, estiloEncabezado,
                "Código", "Nombre", "Categoría", "Talla", "Precio (S/)", "Stock comercial", "Stock mínimo", "Estado");

        int indiceFila = 1;
        for (FilaReporteStock f : filas) {
            Row fila = hoja.createRow(indiceFila++);
            fila.createCell(0).setCellValue(f.codigo());
            fila.createCell(1).setCellValue(f.nombre());
            fila.createCell(2).setCellValue(nvl(f.categoria()));
            fila.createCell(3).setCellValue(nvl(f.talla()));
            fila.createCell(4).setCellValue(f.precio() == null ? 0 : f.precio().doubleValue());
            fila.createCell(5).setCellValue(f.stockComercial());
            fila.createCell(6).setCellValue(f.stockMinimo());
            fila.createCell(7).setCellValue(f.estado().getEtiqueta());

            if (f.estado() == pe.edu.utp.segitd.modelo.EstadoStock.PROXIMO_A_AGOTAR) {
                for (int c = 0; c <= 7; c++) {
                    fila.getCell(c).setCellStyle(estiloProximo);
                }
            }
        }
        autoajustarColumnas(hoja, 8);
    }

    private CellStyle crearEstiloResaltado(Workbook libro, short colorIndex) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setFillForegroundColor(colorIndex);
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
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

    private void escribirEncabezado(Sheet hoja, CellStyle estilo, String... columnas) {
        Row fila = hoja.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            var celda = fila.createCell(i);
            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estilo);
        }
    }

    private void autoajustarColumnas(Sheet hoja, int cantidadColumnas) {
        for (int i = 0; i < cantidadColumnas; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }
}