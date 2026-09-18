package pe.edu.utp.segitd.servicio;

import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.Venta;

import java.util.List;

/**
 * Todo lo que necesita el generador de PDF para emitir una boleta con
 * trazabilidad RSU (módulo 5): la venta, sus líneas y las donaciones que
 * generó. {@code urlPortal} es la base del portal público de consulta al
 * que apunta cada QR.
 */
public record DatosBoleta(Venta venta, List<DetalleVenta> detalle, List<Donacion> donaciones, String urlPortal) {
}
