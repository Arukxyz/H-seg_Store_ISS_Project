package pe.edu.utp.segitd.modelo;

/**
 * Ciclo de vida de una donación (07_normalizacion.sql):
 * EN_ESPERA (la web la registró y la venta aún no está pagada) →
 * PENDIENTE (pago confirmado, lista para un lote) → ASIGNADA → ENTREGADA.
 */
public enum EstadoDonacion {
    EN_ESPERA,
    PENDIENTE,
    ASIGNADA,
    ENTREGADA
}
