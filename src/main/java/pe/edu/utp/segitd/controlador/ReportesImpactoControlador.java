package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.FilaTrazabilidad;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.servicio.ReporteService;
import pe.edu.utp.segitd.servicio.ResumenImpacto;
import pe.edu.utp.segitd.util.ExcelExporter;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/** Traduce los eventos de ReportesImpactoJFrame a llamadas de ReporteService + ExcelExporter. */
public class ReportesImpactoControlador {

    private final ReporteService reporteService = new ReporteService();
    private final ExcelExporter excelExporter = new ExcelExporter();

    public List<Comunidad> listarComunidades() {
        return reporteService.listarComunidades();
    }

    public File exportar(OffsetDateTime desde, OffsetDateTime hasta, Integer idComunidad) throws IOException {
        List<FilaTrazabilidad> trazabilidad = reporteService.generarTrazabilidad(desde, hasta, idComunidad);
        ResumenImpacto resumen = reporteService.calcularResumen(trazabilidad);
        List<Producto> inventario = reporteService.listarInventario();
        return excelExporter.exportarReporteImpacto(resumen, trazabilidad, inventario);
    }
}
