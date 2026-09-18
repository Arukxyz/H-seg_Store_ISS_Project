package pe.edu.utp.segitd.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Genera códigos QR con ZXing (módulo 5). Solo se usa zxing-core: el
 * BitMatrix se vuelca a un BufferedImage a mano para no arrastrar el
 * módulo javase (y sus dependencias) al .jar empaquetado.
 */
public final class QrUtil {

    private static final int NEGRO = 0x000000;
    private static final int BLANCO = 0xFFFFFF;

    private QrUtil() {
    }

    /** Devuelve una imagen cuadrada de {@code tamanoPx} píxeles con el QR del texto indicado. */
    public static BufferedImage generar(String contenido, int tamanoPx) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matriz;
        try {
            matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamanoPx, tamanoPx, hints);
        } catch (WriterException e) {
            throw new IllegalArgumentException("No se pudo codificar el contenido del QR.", e);
        }

        BufferedImage imagen = new BufferedImage(matriz.getWidth(), matriz.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < matriz.getWidth(); x++) {
            for (int y = 0; y < matriz.getHeight(); y++) {
                imagen.setRGB(x, y, matriz.get(x, y) ? NEGRO : BLANCO);
            }
        }
        return imagen;
    }
}
