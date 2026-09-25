package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoLote;
import pe.edu.utp.segitd.modelo.LoteDonacion;
import pe.edu.utp.segitd.modelo.Ong;
import pe.edu.utp.segitd.servicio.DespachoService;
import pe.edu.utp.segitd.util.SesionUsuario;
import pe.edu.utp.segitd.modelo.FilaDonacionRiesgo;
import pe.edu.utp.segitd.util.ExcelExporter;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** Traduce los eventos de DespachoLotesJFrame a llamadas de DespachoService. */
public class DespachoControlador {

    private final DespachoService despachoService = new DespachoService();
    private final ExcelExporter excelExporter = new ExcelExporter();

    public List<Comunidad> listarComunidades() {
        return despachoService.listarComunidades();
    }

    public List<Ong> listarOngs() {
        return despachoService.listarOngs();
    }

    public List<Donacion> listarDonacionesPendientes() {
        return despachoService.listarDonacionesPendientes();
    }

    public List<LoteDonacion> listarLotes() {
        return despachoService.listarLotes();
    }

    public List<Donacion> listarDonacionesPorLote(int idLote) {
        return despachoService.listarDonacionesPorLote(idLote);
    }

    public LoteDonacion crearLote(int idComunidad, int idOng, List<Integer> idsDonacion) {
        int idUsuario = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        return despachoService.crearLote(idComunidad, idOng, idsDonacion, idUsuario);
    }

    public void cambiarEstadoLote(int idLote, EstadoLote nuevoEstado) {
        int idUsuario = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        despachoService.cambiarEstadoLote(idLote, nuevoEstado, idUsuario);
    }

    public List<FilaDonacionRiesgo> listarDonacionesEnRiesgo() {
    return despachoService.listarDonacionesEnRiesgo();
}
public File exportarDonacionesEnRiesgo() throws IOException {
    return excelExporter.exportarDonacionesRiesgo(despachoService.listarDonacionesEnRiesgo());
}
}
