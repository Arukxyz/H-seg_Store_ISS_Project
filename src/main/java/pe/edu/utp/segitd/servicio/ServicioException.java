package pe.edu.utp.segitd.servicio;

/**
 * Excepción de negocio con mensaje en español listo para mostrar al usuario
 * final. Las excepciones SQL nunca llegan crudas a la vista: se traducen
 * aquí (sección 9.4 de la especificación).
 */
public class ServicioException extends RuntimeException {

    public ServicioException(String mensaje) {
        super(mensaje);
    }

    public ServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
