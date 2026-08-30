package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoVenta;
import pe.edu.utp.segitd.modelo.Venta;
import pe.edu.utp.segitd.servicio.PedidoWebService;
import pe.edu.utp.segitd.util.SesionUsuario;

import java.time.OffsetDateTime;
import java.util.List;

/** Traduce los eventos de PedidosWebJFrame a llamadas de PedidoWebService. */
public class PedidosWebControlador {

    private final PedidoWebService pedidoWebService = new PedidoWebService();

    public List<Venta> listarPedidos(EstadoVenta estado, OffsetDateTime desde, OffsetDateTime hasta) {
        return pedidoWebService.listarPedidos(estado, desde, hasta);
    }

    public List<DetalleVenta> listarDetalle(int idVenta) {
        return pedidoWebService.listarDetalle(idVenta);
    }

    public List<Donacion> listarDonaciones(int idVenta) {
        return pedidoWebService.listarDonaciones(idVenta);
    }

    public void confirmarPedido(int idVenta) {
        int idUsuario = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        pedidoWebService.confirmarPedido(idVenta, idUsuario);
    }

    public void anularPedido(int idVenta) {
        int idUsuario = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        pedidoWebService.anularPedido(idVenta, idUsuario);
    }
}
