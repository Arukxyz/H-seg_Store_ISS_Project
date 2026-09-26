package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.FilaDemandaProducto;
import pe.edu.utp.segitd.modelo.FilaRankingProducto;
import pe.edu.utp.segitd.modelo.IndicadorVentasPeriodo;
import pe.edu.utp.segitd.servicio.AnalisisVentasService;
import pe.edu.utp.segitd.util.ExcelExporter;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

public class DashboardVentasControlador {

    private final AnalisisVentasService analisisService = new AnalisisVentasService();
    private final ExcelExporter excelExporter = new ExcelExporter();

    public IndicadorVentasPeriodo calcularIndicadores(OffsetDateTime desde, OffsetDateTime hasta) {
        return analisisService.calcularIndicadores(desde, hasta);
    }

    public List<FilaRankingProducto> rankingProductos(OffsetDateTime desde, OffsetDateTime hasta) {
        return analisisService.rankingProductos(desde, hasta);
    }

    public List<FilaDemandaProducto> analizarDemanda() {
        return analisisService.analizarDemanda();
    }

    public File exportar(OffsetDateTime desde, OffsetDateTime hasta) throws IOException {
        IndicadorVentasPeriodo indicadores = calcularIndicadores(desde, hasta);
        List<FilaRankingProducto> ranking = rankingProductos(desde, hasta);
        List<FilaDemandaProducto> demanda = analizarDemanda();
        return excelExporter.exportarDashboardVentas(desde, hasta, indicadores, ranking, demanda);
    }
}