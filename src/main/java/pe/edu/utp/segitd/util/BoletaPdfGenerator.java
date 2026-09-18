package pe.edu.utp.segitd.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.TipoCompromiso;
import pe.edu.utp.segitd.modelo.Venta;
import pe.edu.utp.segitd.servicio.DatosBoleta;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Emite la boleta de venta digital en PDF con Apache PDFBox (módulo 5).
 * Además del detalle comercial (RUC de 14-DIEZ S.A.C., líneas, IGV y
 * total) incluye la sección de trazabilidad RSU: un código QR por
 * donación generada, que enlaza al portal público de consulta de impacto
 * con el ID único de esa donación.
 *
 * No toca la base de datos: recibe todo en {@link DatosBoleta}.
 */
public final class BoletaPdfGenerator {

    private static final String RAZON_SOCIAL = "14-DIEZ S.A.C.";
    private static final String NOMBRE_COMERCIAL = "HÖSÉG STORE";
    private static final String RUC = "20566428386";
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    private static final Path CARPETA_SALIDA = Path.of("boletas");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat FORMATO_MONTO =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private static final float MARGEN = 50f;
    private static final float ANCHO_PAGINA = PDRectangle.A4.getWidth();
    private static final float ALTO_PAGINA = PDRectangle.A4.getHeight();
    private static final float BORDE_DERECHO = ANCHO_PAGINA - MARGEN;
    private static final float ANCHO_UTIL = BORDE_DERECHO - MARGEN;
    private static final float ALTO_FILA = 16f;
    private static final float TAMANO_QR = 72f;

    private static final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);
    private static final Color COLOR_TEXTO = new Color(0x1A, 0x1A, 0x1A);
    private static final Color COLOR_GRIS = new Color(0x66, 0x66, 0x66);
    private static final Color COLOR_LINEA = new Color(0xD3, 0xD3, 0xD3);

    private final PDFont fuente = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont fuenteNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    /** Genera el PDF en {@code ./boletas/boleta_<comprobante>.pdf} y devuelve el archivo. */
    public File generar(DatosBoleta datos) throws IOException {
        Files.createDirectories(CARPETA_SALIDA);
        File archivo = CARPETA_SALIDA
                .resolve("boleta_" + nombreSeguro(datos.venta().getCodigoComprobante()) + ".pdf")
                .toFile();

        try (PDDocument documento = new PDDocument()) {
            Lienzo lienzo = new Lienzo(documento);
            try {
                dibujarCabecera(lienzo, datos.venta());
                dibujarCliente(lienzo, datos.venta());
                dibujarDetalle(lienzo, datos.detalle());
                dibujarTotales(lienzo, datos.venta().getTotal());
                dibujarTrazabilidad(lienzo, datos);
                dibujarPie(lienzo);
            } finally {
                lienzo.cerrar();
            }
            documento.save(archivo);
        }
        return archivo;
    }

    // ------------------------------------------------------------------ secciones

    private void dibujarCabecera(Lienzo l, Venta venta) throws IOException {
        l.y = ALTO_PAGINA - MARGEN;

        l.texto(fuenteNegrita, 20, MARGEN, l.y - 16, NOMBRE_COMERCIAL, COLOR_PRIMARIO);
        l.texto(fuente, 10, MARGEN, l.y - 32, RAZON_SOCIAL, COLOR_TEXTO);
        l.texto(fuente, 10, MARGEN, l.y - 45, "RUC " + RUC, COLOR_TEXTO);
        l.texto(fuente, 9, MARGEN, l.y - 58, "Retail textil outdoor sostenible · Empresa B certificada", COLOR_GRIS);

        // Recuadro del comprobante a la derecha
        float anchoCaja = 200f;
        float altoCaja = 58f;
        float xCaja = BORDE_DERECHO - anchoCaja;
        float yCaja = l.y - altoCaja;
        l.rectangulo(xCaja, yCaja, anchoCaja, altoCaja, COLOR_LINEA, null);
        l.textoCentrado(fuenteNegrita, 11, xCaja + anchoCaja / 2, yCaja + 40, "BOLETA DE VENTA ELECTRÓNICA", COLOR_TEXTO);
        l.textoCentrado(fuenteNegrita, 13, xCaja + anchoCaja / 2, yCaja + 20, "N° " + venta.getCodigoComprobante(), COLOR_PRIMARIO);
        l.textoCentrado(fuente, 8, xCaja + anchoCaja / 2, yCaja + 7, "Origen: pedido web", COLOR_GRIS);

        l.y -= 72;
        l.lineaHorizontal(l.y, COLOR_PRIMARIO, 1.2f);
        l.y -= 18;
    }

    private void dibujarCliente(Lienzo l, Venta venta) throws IOException {
        String documento = venta.getClienteNumDoc() == null
                ? "-"
                : (venta.getClienteTipoDoc() == null ? "" : venta.getClienteTipoDoc() + " ") + venta.getClienteNumDoc();

        l.etiquetaValor("Cliente:", nvl(venta.getClienteNombre(), "Cliente web"));
        l.etiquetaValor("Documento:", documento);
        l.etiquetaValor("Fecha del pedido:", formatear(venta.getFecha()));
        l.etiquetaValor("Fecha de emisión:", formatear(OffsetDateTime.now()));
        l.y -= 6;
        l.lineaHorizontal(l.y, COLOR_LINEA, 0.6f);
        l.y -= 20;
    }

    private void dibujarDetalle(Lienzo l, List<DetalleVenta> detalle) throws IOException {
        float xCantidad = MARGEN + 6;
        float xDescripcion = MARGEN + 50;
        float xPrecio = BORDE_DERECHO - 100;
        float xSubtotal = BORDE_DERECHO - 6;
        float anchoDescripcion = xPrecio - 80 - xDescripcion;

        l.asegurarEspacio(ALTO_FILA * 2);
        l.rectangulo(MARGEN, l.y - 4, ANCHO_UTIL, ALTO_FILA, null, COLOR_PRIMARIO);
        l.texto(fuenteNegrita, 9, xCantidad, l.y, "CANT.", Color.WHITE);
        l.texto(fuenteNegrita, 9, xDescripcion, l.y, "DESCRIPCIÓN", Color.WHITE);
        l.textoDerecha(fuenteNegrita, 9, xPrecio, l.y, "P. UNIT. S/", Color.WHITE);
        l.textoDerecha(fuenteNegrita, 9, xSubtotal, l.y, "SUBTOTAL S/", Color.WHITE);
        l.y -= ALTO_FILA + 2;

        for (DetalleVenta linea : detalle) {
            List<String> descripcion = ajustarLineas(
                    linea.getNombreProducto() + "  (" + linea.getCodigoProducto() + ")", fuente, 9, anchoDescripcion);
            l.asegurarEspacio(ALTO_FILA * descripcion.size());

            l.texto(fuente, 9, xCantidad, l.y, String.valueOf(linea.getCantidad()), COLOR_TEXTO);
            l.textoDerecha(fuente, 9, xPrecio, l.y, FORMATO_MONTO.format(linea.getPrecioUnitario()), COLOR_TEXTO);
            l.textoDerecha(fuente, 9, xSubtotal, l.y, FORMATO_MONTO.format(linea.getSubtotal()), COLOR_TEXTO);
            for (String parte : descripcion) {
                l.texto(fuente, 9, xDescripcion, l.y, parte, COLOR_TEXTO);
                l.y -= ALTO_FILA;
            }
            l.lineaHorizontal(l.y + ALTO_FILA - 5, COLOR_LINEA, 0.4f);
        }
        l.y -= 6;
    }

    private void dibujarTotales(Lienzo l, BigDecimal total) throws IOException {
        // Los precios de venta al público incluyen IGV: se desglosa hacia atrás.
        BigDecimal opGravada = total.divide(BigDecimal.ONE.add(TASA_IGV), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(opGravada);

        float xEtiqueta = BORDE_DERECHO - 150;
        float xValor = BORDE_DERECHO - 6;

        l.asegurarEspacio(ALTO_FILA * 4);
        l.texto(fuente, 9, xEtiqueta, l.y, "Op. gravada S/", COLOR_TEXTO);
        l.textoDerecha(fuente, 9, xValor, l.y, FORMATO_MONTO.format(opGravada), COLOR_TEXTO);
        l.y -= ALTO_FILA;
        l.texto(fuente, 9, xEtiqueta, l.y, "IGV (18%) S/", COLOR_TEXTO);
        l.textoDerecha(fuente, 9, xValor, l.y, FORMATO_MONTO.format(igv), COLOR_TEXTO);
        l.y -= ALTO_FILA + 2;
        l.rectangulo(xEtiqueta - 6, l.y - 5, BORDE_DERECHO - xEtiqueta + 6, ALTO_FILA + 2, null, new Color(0xEE, 0xEE, 0xEC));
        l.texto(fuenteNegrita, 11, xEtiqueta, l.y, "TOTAL S/", COLOR_PRIMARIO);
        l.textoDerecha(fuenteNegrita, 11, xValor, l.y, FORMATO_MONTO.format(total), COLOR_PRIMARIO);
        l.y -= ALTO_FILA * 2;
    }

    private void dibujarTrazabilidad(Lienzo l, DatosBoleta datos) throws IOException {
        l.asegurarEspacio(60);
        l.lineaHorizontal(l.y + 8, COLOR_PRIMARIO, 1.2f);
        l.y -= 10;
        l.texto(fuenteNegrita, 12, MARGEN, l.y, "Trazabilidad de impacto social (RSU)", COLOR_PRIMARIO);
        l.y -= 15;

        String introduccion = "Höség es una Empresa B de triple impacto: por cada prenda de abrigo vendida se dona una "
                + "casaca a un niño altoandino de Cusco, y por cada accesorio se planta un árbol nativo junto a la "
                + "ONG Pachamama Raymi. Escanea el código QR de cada donación para verificar públicamente su estado.";
        for (String parte : ajustarLineas(introduccion, fuente, 9, ANCHO_UTIL)) {
            l.texto(fuente, 9, MARGEN, l.y, parte, COLOR_GRIS);
            l.y -= 12;
        }
        l.y -= 8;

        if (datos.donaciones().isEmpty()) {
            l.texto(fuente, 9, MARGEN, l.y, "Este pedido no incluye productos con compromiso social.", COLOR_TEXTO);
            l.y -= ALTO_FILA;
            return;
        }

        for (Donacion donacion : datos.donaciones()) {
            dibujarDonacion(l, donacion, datos.urlPortal());
        }
    }

    private void dibujarDonacion(Lienzo l, Donacion donacion, String urlPortal) throws IOException {
        float altoBloque = TAMANO_QR + 14;
        l.asegurarEspacio(altoBloque);

        String url = urlPortal + (urlPortal.contains("?") ? "&" : "?") + "donacion=" + donacion.getId();
        PDImageXObject qr = LosslessFactory.createFromImage(l.documento, QrUtil.generar(url, 300));

        float yBloque = l.y - TAMANO_QR;
        l.rectangulo(MARGEN, yBloque - 6, ANCHO_UTIL, altoBloque, COLOR_LINEA, null);
        l.contenido.drawImage(qr, MARGEN + 6, yBloque, TAMANO_QR, TAMANO_QR);

        float xTexto = MARGEN + TAMANO_QR + 18;
        float yTexto = l.y - 12;
        l.texto(fuenteNegrita, 10, xTexto, yTexto,
                "Donación #" + donacion.getId() + "  ·  " + describirCompromiso(donacion), COLOR_TEXTO);
        l.texto(fuente, 9, xTexto, yTexto - 15, "Producto: " + donacion.getNombreProducto()
                + " (" + donacion.getCodigoProducto() + ")", COLOR_TEXTO);
        l.texto(fuente, 9, xTexto, yTexto - 29, "Estado actual: " + donacion.getEstado()
                + (donacion.getIdLote() == null ? "" : "  ·  Lote asignado"), COLOR_TEXTO);
        l.texto(fuente, 7.5f, xTexto, yTexto - 45, "Verificar en: " + url, COLOR_GRIS);

        l.y = yBloque - 16;
    }

    private void dibujarPie(Lienzo l) throws IOException {
        String pie = "Representación impresa de la boleta de venta electrónica emitida por " + RAZON_SOCIAL
                + " · Documento generado por SEGITD-HÖSÉG.";
        l.textoCentrado(fuente, 7.5f, ANCHO_PAGINA / 2, MARGEN - 20, pie, COLOR_GRIS);
    }

    // ------------------------------------------------------------------ utilidades

    private String describirCompromiso(Donacion donacion) {
        int n = donacion.getCantidad();
        return donacion.getTipo() == TipoCompromiso.ABRIGO
                ? n + (n == 1 ? " casaca de abrigo donada" : " casacas de abrigo donadas")
                : n + (n == 1 ? " árbol nativo plantado" : " árboles nativos plantados");
    }

    /** Parte un texto en líneas que quepan en {@code anchoMaximo} puntos. */
    private List<String> ajustarLineas(String texto, PDFont fuente, float tamano, float anchoMaximo) throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split("\\s+")) {
            String candidata = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (ancho(candidata, fuente, tamano) > anchoMaximo && !actual.isEmpty()) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(candidata);
            }
        }
        if (!actual.isEmpty()) {
            lineas.add(actual.toString());
        }
        return lineas;
    }

    private float ancho(String texto, PDFont fuente, float tamano) throws IOException {
        return fuente.getStringWidth(sanear(texto)) / 1000f * tamano;
    }

    /**
     * Las fuentes estándar de PDF usan WinAnsiEncoding: cubre acentos, Ö, É,
     * ä, °, · y guiones largos, pero no símbolos como ● o emojis. Cualquier
     * carácter fuera del rango se sustituye para que showText no falle.
     */
    private static String sanear(String texto) {
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            boolean soportado = (c >= 0x20 && c <= 0x7E) || (c >= 0xA0 && c <= 0xFF)
                    || c == '€' || c == '–' || c == '—' || c == '‘' || c == '’' || c == '“' || c == '”' || c == '…' || c == '•';
            sb.append(soportado ? c : '?');
        }
        return sb.toString();
    }

    private static String nombreSeguro(String codigo) {
        return codigo == null ? "sin_codigo" : codigo.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static String formatear(OffsetDateTime fecha) {
        return fecha == null ? "-" : FORMATO_FECHA.format(fecha);
    }

    private static String nvl(String valor, String porDefecto) {
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }

    /**
     * Cursor de dibujo sobre el documento: mantiene la página y el flujo de
     * contenido activos y la coordenada Y actual, abriendo una página nueva
     * cuando el contenido no cabe.
     */
    private final class Lienzo {
        private final PDDocument documento;
        private PDPageContentStream contenido;
        private float y;

        Lienzo(PDDocument documento) throws IOException {
            this.documento = documento;
            nuevaPagina();
        }

        void nuevaPagina() throws IOException {
            if (contenido != null) {
                contenido.close();
            }
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = ALTO_PAGINA - MARGEN;
        }

        void asegurarEspacio(float alto) throws IOException {
            if (y - alto < MARGEN) {
                nuevaPagina();
            }
        }

        void cerrar() throws IOException {
            if (contenido != null) {
                contenido.close();
            }
        }

        void texto(PDFont f, float tamano, float x, float yTexto, String s, Color color) throws IOException {
            contenido.beginText();
            contenido.setFont(f, tamano);
            contenido.setNonStrokingColor(color);
            contenido.newLineAtOffset(x, yTexto);
            contenido.showText(sanear(s));
            contenido.endText();
        }

        void textoDerecha(PDFont f, float tamano, float xDerecha, float yTexto, String s, Color color) throws IOException {
            texto(f, tamano, xDerecha - ancho(s, f, tamano), yTexto, s, color);
        }

        void textoCentrado(PDFont f, float tamano, float xCentro, float yTexto, String s, Color color) throws IOException {
            texto(f, tamano, xCentro - ancho(s, f, tamano) / 2, yTexto, s, color);
        }

        void etiquetaValor(String etiqueta, String valor) throws IOException {
            texto(fuenteNegrita, 9, MARGEN, y, etiqueta, COLOR_GRIS);
            texto(fuente, 9, MARGEN + 95, y, valor, COLOR_TEXTO);
            y -= 13;
        }

        void lineaHorizontal(float yLinea, Color color, float grosor) throws IOException {
            contenido.setStrokingColor(color);
            contenido.setLineWidth(grosor);
            contenido.moveTo(MARGEN, yLinea);
            contenido.lineTo(BORDE_DERECHO, yLinea);
            contenido.stroke();
        }

        /** Rectángulo con borde y/o relleno; cualquiera de los dos colores puede ser null. */
        void rectangulo(float x, float yInferior, float ancho, float alto, Color borde, Color relleno) throws IOException {
            if (relleno != null) {
                contenido.setNonStrokingColor(relleno);
                contenido.addRect(x, yInferior, ancho, alto);
                contenido.fill();
            }
            if (borde != null) {
                contenido.setStrokingColor(borde);
                contenido.setLineWidth(0.6f);
                contenido.addRect(x, yInferior, ancho, alto);
                contenido.stroke();
            }
        }
    }
}
