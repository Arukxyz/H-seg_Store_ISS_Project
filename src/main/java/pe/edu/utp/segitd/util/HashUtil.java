package pe.edu.utp.segitd.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Hash de contraseñas con SHA-256 y salt por usuario (RNF-02).
 * Nunca se guarda ni compara una contraseña en texto plano.
 */
public final class HashUtil {

    private static final String ALGORITMO = "SHA-256";
    private static final int LONGITUD_SALT_BYTES = 16;
    private static final SecureRandom GENERADOR_ALEATORIO = new SecureRandom();

    private HashUtil() {
    }

    public static String generarSalt() {
        byte[] bytes = new byte[LONGITUD_SALT_BYTES];
        GENERADOR_ALEATORIO.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public static String hashear(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            byte[] resultado = digest.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resultado);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo " + ALGORITMO + " no disponible en esta JVM", e);
        }
    }
}
