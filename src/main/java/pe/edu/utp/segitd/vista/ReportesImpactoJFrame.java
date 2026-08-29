package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.ReportesImpactoControlador;
import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.FechaUtil;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

    public ReportesImpactoJFrame() {
        super("SEGITD-HÖSÉG · Reportes de impacto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(560, 280));
        pack();
        setLocationRelativeTo(null);
        cargarComunidades();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new GridBagLayout());
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel titulo = new JLabel("Reporte de impacto");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        raiz.add(titulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        raiz.add(new JLabel("Comunidad:"), gbc);
        gbc.gridx = 1;
        comboComunidad.setRenderer(new TodasListRenderer());
        raiz.add(comboComunidad, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        raiz.add(new JLabel("Desde:"), gbc);
        gbc.gridx = 1;
        raiz.add(spinnerDesde, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        raiz.add(new JLabel("Hasta:"), gbc);
        gbc.gridx = 1;
        raiz.add(spinnerHasta, gbc);

        JButton botonExportar = new JButton("Exportar a Excel");
        botonExportar.addActionListener(e -> exportar());
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        raiz.add(botonExportar, gbc);

        gbc.gridy = 5;
        raiz.add(etiquetaEstado, gbc);

        return raiz;
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

    private static final class TodasListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            Object texto = value == null ? "Todas" : value;
            return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
        }
    }
}
