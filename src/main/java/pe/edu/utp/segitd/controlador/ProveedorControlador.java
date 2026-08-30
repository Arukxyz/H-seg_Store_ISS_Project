package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.PedidoProveedor;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.Proveedor;
import pe.edu.utp.segitd.servicio.ProveedorService;
import pe.edu.utp.segitd.util.SesionUsuario;

import java.util.List;

/** Traduce los eventos de ProveedoresJFrame a llamadas de ProveedorService. */
public class ProveedorControlador {

    private final ProveedorService proveedorService = new ProveedorService();

    public List<Proveedor> listarProveedores() {
        return proveedorService.listarProveedores();
    }

    public List<Producto> listarProductos() {
        return proveedorService.listarProductos();
    }

    public void crearProveedor(Proveedor proveedor) {
        proveedorService.crearProveedor(proveedor);
    }

    public void actualizarProveedor(Proveedor proveedor) {
        proveedorService.actualizarProveedor(proveedor);
    }

    public void desactivarProveedor(int id) {
        proveedorService.desactivarProveedor(id);
    }

    public List<PedidoProveedor> listarPedidos() {
        return proveedorService.listarPedidos();
    }

    public void crearPedido(int idProveedor, String codigoProducto, String descripcion, int cantidad) {
        int idUsuario = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        proveedorService.crearPedido(idProveedor, codigoProducto, descripcion, cantidad, idUsuario);
    }

    public void marcarRecibido(int idPedido) {
        int idUsuario = SesionUsuario.obtenerInstancia().getUsuarioActual().getId();
        proveedorService.marcarRecibido(idPedido, idUsuario);
    }

    public void anularPedido(int idPedido) {
        proveedorService.anularPedido(idPedido);
    }
}
