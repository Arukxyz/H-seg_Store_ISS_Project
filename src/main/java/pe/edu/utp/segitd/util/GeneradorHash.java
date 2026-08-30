package pe.edu.utp.segitd.util;

/**
 * Utilidad de un solo uso: calcula salt + hash para los usuarios semilla
 * e imprime la fila lista para pegar en 02_datos_prueba.sql. Ningún hash
 * se escribe a mano (ver sección 7 del documento de especificación).
 */
public final class GeneradorHash {

    private GeneradorHash() {
    }

    public static void main(String[] args) {
        imprimirFila("Administrador General", "admin", "Admin#2026", "ADMINISTRADOR");
        imprimirFila("Encargado de Almacén", "encargado", "Encargado#2026", "ENCARGADO");
    }

    private static void imprimirFila(String nombre, String username, String password, String rol) {
        String salt = HashUtil.generarSalt();
        String hash = HashUtil.hashear(password, salt);
        System.out.printf("-- login de prueba: %s / %s%n", username, password);
        System.out.printf("('%s', '%s', '%s', '%s', '%s'),%n", nombre, username, hash, salt, rol);
    }
}
