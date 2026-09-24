package pe.edu.utp.segitd.modelo;

/**
 * Clasificación visual del nivel de stock comercial de un producto, usada
 * en el reporte de stock (RF-03). Se determina comparando el stock comercial con el mínimo
 */
public enum EstadoStock {

    SIN_STOCK("Sin stock", "No quedan unidades disponibles para la venta."),
    CRITICO("Crítico", "El stock está en el mínimo configurado o por debajo: reponer con urgencia."),
    PROXIMO_A_AGOTAR("Próximo a crítico", "El stock se acerca al mínimo: pronto pasará a estado crítico si no se repone."),
    DISPONIBLE("Disponible", "El stock está en un nivel saludable.");

    private final String etiqueta;
    private final String descripcion;

    EstadoStock(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}