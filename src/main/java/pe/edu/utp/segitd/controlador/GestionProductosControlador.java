package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.TipoStock;
import pe.edu.utp.segitd.servicio.InventarioService;
import pe.edu.utp.segitd.util.SesionUsuario;

import java.util.List;

/** Traduce los eventos de GestionProductosJFrame a llamadas de InventarioService. */
public class GestionProductosControlador {

    private final InventarioService inventarioService = new InventarioService();

    public List<Producto> listarProductos() {
        return inventarioService.listarProductos();
    }

    public void crearProducto(Producto producto) {
        inventarioService.crearProducto(producto);
    }

    public void actualizarProducto(Producto producto) {
        inventarioService.actualizarProducto(producto);
    }

    public void desactivarProducto(String codigo) {
        inventarioService.desactivarProducto(codigo);
    }

    public void ajustarStock(String codigoProducto, TipoStock tipoStock, int delta, String motivo) {
        int idUsuario = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        inventarioService.ajustarStock(codigoProducto, tipoStock, delta, motivo, idUsuario);
    }
}
