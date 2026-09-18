package pe.edu.utp.segitd.util;

import pe.edu.utp.segitd.servicio.ServicioException;

import java.math.BigDecimal;

/**
 * Validaciones de entrada reutilizables (sección 6). Lanzan
 * ServicioException con un mensaje en español listo para mostrarse en un
 * JOptionPane, de modo que ni las vistas ni los servicios repitan los
 * mismos if/throw. Aquí solo hay validaciones de formato y presencia; las
 * reglas de negocio (duplicados, stock, transiciones) viven en servicio.
 */
public final class Validador {

    private Validador() {
    }

    public static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    /** Devuelve el valor sin espacios en los extremos, o lanza si está vacío. */
    public static String obligatorio(String valor, String nombreCampo) {
        if (esVacio(valor)) {
            throw new ServicioException("El campo \"" + nombreCampo + "\" es obligatorio.");
        }
        return valor.trim();
    }

    /** Para columnas opcionales: un texto en blanco se guarda como NULL, no como "". */
    public static String vacioComoNulo(String valor) {
        return esVacio(valor) ? null : valor.trim();
    }

    /** Convierte texto a BigDecimal exigiendo un número válido y no negativo (precios, montos). */
    public static BigDecimal decimalNoNegativo(String valor, String nombreCampo) {
        String texto = obligatorio(valor, nombreCampo).replace(',', '.');
        BigDecimal numero;
        try {
            numero = new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new ServicioException("El campo \"" + nombreCampo + "\" debe ser un número válido.");
        }
        if (numero.signum() < 0) {
            throw new ServicioException("El campo \"" + nombreCampo + "\" no puede ser negativo.");
        }
        return numero;
    }

    public static int mayorQueCero(int valor, String nombreCampo) {
        if (valor <= 0) {
            throw new ServicioException("El campo \"" + nombreCampo + "\" debe ser mayor a cero.");
        }
        return valor;
    }
}
