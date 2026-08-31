package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.ReportesImpactoControlador;
import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.FechaUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Exportación del reporte de impacto a Excel (RF-07, sección 8 pantalla 6).
 * Solo ADMINISTRADOR — el botón ya viene deshabilitado para ENCARGADO en
 * el menú principal.
 */
public class ReportesImpactoJFrame extends JFrame {

    private final ReportesImpactoControlador controlador = new ReportesImpactoControlador();

    private final JComboBox<Comunidad> comboComunidad = new JComboBox<>();
    private final JSpinner spinnerDesde = FechaUtil.crearSpinnerFecha(-90);
    private final JSpinner spinnerHasta = FechaUtil.crearSpinnerFecha(1);
    private final JLabel etiquetaEstado = new JLabel(" ");

    // COLORES (mismo sistema que GestionProductosJFrame)
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3);
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);
    private final Color COLOR_EXITO = new Color(0x2E, 0x7D, 0x32);

    private final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private final Font FUENTE_INPUT = new Font("SansSerif", Font.PLAIN, 13);

    public ReportesImpactoJFrame() {
        super("SEGITD-HÖSÉG · Reportes de impacto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(620, 380));
        pack();
        setLocationRelativeTo(null);
        cargarComunidades();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        raiz.setBackground(COLOR_FONDO_VENTANA);

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
        panelTop.setBackground(COLOR_FONDO_VENTANA);

        JLabel titulo = new JLabel("Reporte de impacto");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(COLOR_TEXTO_MAIN);
        panelTop.add(titulo);
        panelTop.add(Box.createVerticalStrut(10));
        panelTop.add(new FranjaDecorativaHoseg());

        raiz.add(panelTop, BorderLayout.NORTH);
        raiz.add(construirTarjetaFormulario(), BorderLayout.CENTER);
        return raiz;
    }

    private JPanel construirTarjetaFormulario() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 20));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2, 0xE2, 0xE0), 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0.35;

        comboComunidad.setRenderer(new TodasListRenderer());
        agregarCampo(form, gbc, 0, "Comunidad:", comboComunidad);

        agregarCampo(form, gbc, 1, "Desde:", spinnerDesde);
        agregarCampo(form, gbc, 2, "Hasta:", spinnerHasta);

        tarjeta.add(form, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBackground(Color.WHITE);

        JButton botonExportar = new JButton("Exportar a Excel");
        estilizarBotonPrincipal(botonExportar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonExportar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonExportar.addActionListener(e -> exportar());
        panelInferior.add(botonExportar);
        panelInferior.add(Box.createVerticalStrut(12));

        etiquetaEstado.setFont(new Font("SansSerif", Font.PLAIN, 13));
        etiquetaEstado.setForeground(COLOR_EXITO);
        etiquetaEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelInferior.add(etiquetaEstado);

        tarjeta.add(panelInferior, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0.35;

        JLabel label = new JLabel(etiqueta);
        label.setFont(FUENTE_LABEL);
        label.setForeground(COLOR_GRIS_TEXTO);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;

        if (campo instanceof JComboBox) {
            campo.setFont(FUENTE_INPUT);
            campo.setBackground(Color.WHITE);
            campo.setBorder(BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1));
        } else if (campo instanceof JSpinner) {
            estilizarComponenteForm((JComponent) ((JSpinner) campo).getEditor());
        }
        panel.add(campo, gbc);
    }

    private void cargarComunidades() {
        try {
            comboComunidad.removeAllItems();
            comboComunidad.addItem(null);
            controlador.listarComunidades().forEach(comboComunidad::addItem);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportar() {
        Comunidad comunidad = (Comunidad) comboComunidad.getSelectedItem();
        OffsetDateTime desde = FechaUtil.inicioDelDia((Date) spinnerDesde.getValue());
        OffsetDateTime hasta = FechaUtil.finDelDia((Date) spinnerHasta.getValue());

        try {
            File archivo = controlador.exportar(desde, hasta, comunidad == null ? null : comunidad.getId());
            etiquetaEstado.setText("Generado: " + archivo.getName());
            abrirArchivo(archivo);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirArchivo(File archivo) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(archivo);
                return;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "El reporte se generó en " + archivo.getAbsolutePath() + " pero no se pudo abrir automáticamente.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Reporte generado en " + archivo.getAbsolutePath(),
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void estilizarComponenteForm(JComponent comp) {
        comp.setFont(FUENTE_INPUT);
        comp.setBackground(Color.WHITE);
        comp.setForeground(COLOR_TEXTO_MAIN);
        comp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void estilizarBotonPrincipal(JButton boton, Color fondo, Color hover) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(hover);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(fondo);
            }
        });
    }

    private static final class TodasListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            Object texto = value == null ? "Todas" : value;
            return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
        }
    }

    private static class FranjaDecorativaHoseg extends JComponent {
        public FranjaDecorativaHoseg() {
            setPreferredSize(new Dimension(100, 4));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            LinearGradientPaint degradado = new LinearGradientPaint(
                    0, 0, getWidth(), 0,
                    new float[]{0.0f, 0.35f, 0.70f, 1.0f},
                    new Color[]{
                            new Color(0x00, 0x33, 0xAA),
                            new Color(0x6A, 0x1B, 0x9A),
                            new Color(0xD8, 0x1B, 0x60),
                            new Color(0xD3, 0x2F, 0x2F)
                    }
            );
            g2d.setPaint(degradado);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
