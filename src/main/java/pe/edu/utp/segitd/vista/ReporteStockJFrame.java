package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.ReporteStockControlador;
import pe.edu.utp.segitd.modelo.EstadoStock;
import pe.edu.utp.segitd.modelo.FilaReporteStock;
import pe.edu.utp.segitd.servicio.ReporteStockService;
import pe.edu.utp.segitd.servicio.ServicioException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Reporte de nivel de stock del catálogo (RF-03), en 3 pestañas: sin stock,
 * críticos y disponibles
 */
public class ReporteStockJFrame extends JFrame {

    private final ReporteStockControlador controlador = new ReporteStockControlador();

    private final JTabbedPane pestanas = new JTabbedPane();

    private final ModeloSinStock modeloSinStock = new ModeloSinStock();
    private final JTable tablaSinStock = new JTable(modeloSinStock);
    private final ModeloCriticos modeloCriticos = new ModeloCriticos();
    private final JTable tablaCriticos = new JTable(modeloCriticos);
    private final ModeloDisponibles modeloDisponibles = new ModeloDisponibles();
    private final JTable tablaDisponibles = new JTable(modeloDisponibles);

    private final JLabel valorSinStock = new JLabel("0");
    private final JLabel valorCriticos = new JLabel("0");
    private final JLabel valorProximos = new JLabel("0");
    private final JLabel valorDisponibles = new JLabel("0");

    private ReporteStockService.ReporteStock reporteActual;

    /** Opción del combo que significa "no filtrar por categoría". */
    private static final String TODAS_CATEGORIAS = "Todas las categorías";
    private final JComboBox<String> comboCategoria = new JComboBox<>();
    /** Evita que repoblar el combo dispare el filtro antes de tiempo. */
    private boolean actualizandoCombo = false;

    // COLORES (paleta Höség)
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3);
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);
    private final Color COLOR_BURDEO_HOVER = new Color(0xA6, 0x3A, 0x24);
    private final Color COLOR_AMBAR = new Color(0xB8, 0x7A, 0x00);
    private final Color COLOR_CRITICO_TEXTO = new Color(0xC6, 0x28, 0x28);
    private final Color COLOR_DISPONIBLE_TEXTO = new Color(0x2E, 0x7D, 0x32);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);
    private final Color COLOR_BORDE = new Color(0xE2, 0xE2, 0xE0);
    private final Color COLOR_FILA_SIN_STOCK = new Color(0xF2, 0xE0, 0xDC);
    private final Color COLOR_FILA_CRITICO = new Color(0xFF, 0xEB, 0xEE);
    private final Color COLOR_FILA_PROXIMO = new Color(0xFF, 0xF4, 0xD9);

    private final Font FUENTE_PANEL_TITULO = new Font("SansSerif", Font.BOLD, 14);

    public ReporteStockJFrame() {
        super("SEGITD-HÖSÉG · Reporte de stock");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);

        setMinimumSize(new Dimension(1080, 640));
        pack();
        setLocationRelativeTo(null);
        cargarReporte();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
        panelTop.setBackground(COLOR_FONDO_VENTANA);

        JLabel lblTitulo = new JLabel("Reporte de Stock de Productos");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(COLOR_TEXTO_MAIN);
        panelTop.add(lblTitulo);
        panelTop.add(Box.createVerticalStrut(6));

        
        panelTop.add(Box.createVerticalStrut(10));
        panelTop.add(new FranjaDecorativaHoseg());
        panelTop.add(Box.createVerticalStrut(14));
        panelTop.add(construirResumen());
        panelTop.add(Box.createVerticalStrut(10));
        panelTop.add(construirFiltroCategoria());

        raiz.add(panelTop, BorderLayout.NORTH);

        pestanas.setFont(new Font("SansSerif", Font.BOLD, 12));
        pestanas.setBackground(Color.WHITE);
        pestanas.setForeground(COLOR_PRIMARIO);
        pestanas.addTab("Sin stock", construirPanelSinStock());
        pestanas.addTab("Críticos", construirPanelCriticos());
        pestanas.addTab("Disponibles", construirPanelDisponibles());
        raiz.add(pestanas, BorderLayout.CENTER);

        raiz.add(construirBotonesInferiores(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirResumen() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.add(tarjetaResumen("Sin stock", valorSinStock, COLOR_BURDEO));
        panel.add(tarjetaResumen("Críticos", valorCriticos, COLOR_CRITICO_TEXTO));
        panel.add(tarjetaResumen("Próximos a agotar", valorProximos, COLOR_AMBAR));
        panel.add(tarjetaResumen("Disponibles", valorDisponibles, COLOR_DISPONIBLE_TEXTO));
        return panel;
    }

    /** Combo de categoría: filtra las 3 tablas y los contadores de arriba. */
    private JPanel construirFiltroCategoria() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(COLOR_FONDO_VENTANA);

        JLabel etiqueta = new JLabel("Categoría:");
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
        etiqueta.setForeground(COLOR_TEXTO_MAIN);
        panel.add(etiqueta);

        comboCategoria.addItem(TODAS_CATEGORIAS);
        comboCategoria.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comboCategoria.addActionListener(e -> {
            if (!actualizandoCombo) {
                aplicarFiltroCategoria();
            }
        });
        panel.add(comboCategoria);
        return panel;
    }

    private JPanel tarjetaResumen(String etiqueta, JLabel valor, Color colorAcento) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new LineBorder(COLOR_BORDE, 1));

        JPanel barra = new JPanel();
        barra.setBackground(colorAcento);
        barra.setPreferredSize(new Dimension(10, 4));
        wrapper.add(barra, BorderLayout.SOUTH);

        JPanel cuerpo = new JPanel(new BorderLayout(4, 4));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));

        valor.setFont(new Font("SansSerif", Font.BOLD, 26));
        valor.setForeground(colorAcento);
        valor.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel descripcion = new JLabel(etiqueta, SwingConstants.CENTER);
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descripcion.setForeground(COLOR_GRIS_TEXTO);

        cuerpo.add(valor, BorderLayout.CENTER);
        cuerpo.add(descripcion, BorderLayout.SOUTH);
        wrapper.add(cuerpo, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel construirPanelSinStock() {
        estilizarTablaElegante(tablaSinStock);
        tablaSinStock.setDefaultRenderer(Object.class, new ResaltadoFilaRenderer(EstadoStock.SIN_STOCK, null));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JLabel titulo = new JLabel("Reporte de productos que NO tienen stock");
        titulo.setFont(FUENTE_PANEL_TITULO);
        titulo.setForeground(COLOR_BURDEO);
        panel.add(titulo, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tablaSinStock);
        scroll.setBorder(new LineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelCriticos() {
        estilizarTablaElegante(tablaCriticos);
        tablaCriticos.setDefaultRenderer(Object.class, new ResaltadoFilaRenderer(EstadoStock.CRITICO, null));

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Reporte de productos en estado crítico");
        titulo.setFont(FUENTE_PANEL_TITULO);
        titulo.setForeground(COLOR_CRITICO_TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezado.add(titulo);

        JLabel subtitulo = new JLabel("Ordenados de menor a mayor stock comercial (el más urgente primero).");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitulo.setForeground(COLOR_GRIS_TEXTO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezado.add(subtitulo);

        panel.add(encabezado, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tablaCriticos);
        scroll.setBorder(new LineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelDisponibles() {
        estilizarTablaElegante(tablaDisponibles);
        tablaDisponibles.setDefaultRenderer(Object.class, new ResaltadoFilaRenderer(null, modeloDisponibles));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Reporte de productos disponibles");
        titulo.setFont(FUENTE_PANEL_TITULO);
        titulo.setForeground(COLOR_PRIMARIO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezado.add(titulo);

        JLabel subtitulo = new JLabel("Ordenados de menor a mayor stock comercial.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitulo.setForeground(COLOR_GRIS_TEXTO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezado.add(subtitulo);

        panel.add(encabezado, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tablaDisponibles);
        scroll.setBorder(new LineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirBotonesInferiores() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JButton botonActualizar = new JButton("Actualizar");
        estilizarBotonSecundario(botonActualizar, COLOR_PRIMARIO);
        botonActualizar.addActionListener(e -> cargarReporte());

        JButton botonExportar = new JButton("Exportar a Excel");
        estilizarBotonPrincipal(botonExportar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonExportar.addActionListener(e -> exportarExcel());

        panel.add(botonActualizar);
        panel.add(botonExportar);
        return panel;
    }

    private void cargarReporte() {
        try {
            reporteActual = controlador.generarReporte();
            repoblarCategorias();
            aplicarFiltroCategoria();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Reconstruye las opciones del combo con las categorías presentes en el reporte actual. */
    private void repoblarCategorias() {
        String seleccionPrevia = (String) comboCategoria.getSelectedItem();

        // TreeMap con clave case-insensitive: "casacas" y "Casacas" se fusionan solos
        // (se conserva la primera forma de escritura encontrada). Solo palabras REALMENTE
        // distintas (p. ej. "Accesorio" vs "Accesorios") seguirán apareciendo por separado,
        // porque para el sistema son dos textos diferentes, no una variación de mayúsculas.
        java.util.Map<String, String> categoriasPorClave = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Stream.of(reporteActual.sinStock(), reporteActual.criticos(), reporteActual.disponibles())
                .flatMap(List::stream)
                .map(FilaReporteStock::categoria)
                .map(c -> c == null ? null : c.trim())
                .filter(c -> c != null && !c.isEmpty())
                .forEach(c -> categoriasPorClave.putIfAbsent(c, c));

        List<String> categorias = List.copyOf(categoriasPorClave.values());

        actualizandoCombo = true;
        comboCategoria.removeAllItems();
        comboCategoria.addItem(TODAS_CATEGORIAS);
        categorias.forEach(comboCategoria::addItem);

        if (seleccionPrevia != null && (TODAS_CATEGORIAS.equals(seleccionPrevia) || categorias.contains(seleccionPrevia))) {
            comboCategoria.setSelectedItem(seleccionPrevia);
        } else {
            comboCategoria.setSelectedItem(TODAS_CATEGORIAS);
        }
        actualizandoCombo = false;
    }

    private List<FilaReporteStock> filtrarPorCategoria(List<FilaReporteStock> lista, String categoria) {
        if (categoria == null || TODAS_CATEGORIAS.equals(categoria)) {
            return lista;
        }
        return lista.stream()
                .filter(f -> f.categoria() != null && categoria.equalsIgnoreCase(f.categoria().trim()))
                .toList();
    }

    /** Filtra las 3 tablas y recalcula los contadores según la categoría elegida (o todas). */
    private void aplicarFiltroCategoria() {
        if (reporteActual == null) {
            return;
        }
        String categoria = (String) comboCategoria.getSelectedItem();

        List<FilaReporteStock> sinStock = filtrarPorCategoria(reporteActual.sinStock(), categoria);
        List<FilaReporteStock> criticos = filtrarPorCategoria(reporteActual.criticos(), categoria);
        List<FilaReporteStock> disponibles = filtrarPorCategoria(reporteActual.disponibles(), categoria);

        modeloSinStock.actualizar(sinStock);
        modeloCriticos.actualizar(criticos);
        modeloDisponibles.actualizar(disponibles);

        int totalSinStock = sinStock.size();
        int totalCriticos = criticos.size();
        int totalDisponiblesTotal = disponibles.size();
        int totalProximos = (int) disponibles.stream().filter(f -> f.estado() == EstadoStock.PROXIMO_A_AGOTAR).count();

        valorSinStock.setText(String.valueOf(totalSinStock));
        valorCriticos.setText(String.valueOf(totalCriticos));
        valorProximos.setText(String.valueOf(totalProximos));
        valorDisponibles.setText(String.valueOf(totalDisponiblesTotal - totalProximos));

        pestanas.setTitleAt(0, "Sin stock (" + totalSinStock + ")");
        pestanas.setTitleAt(1, "Críticos (" + totalCriticos + ")");
        pestanas.setTitleAt(2, "Disponibles (" + totalDisponiblesTotal + ")");
    }

    private void exportarExcel() {
        if (reporteActual == null) {
            return;
        }
        try {
            File archivo = controlador.exportarAExcel(reporteActual);
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

    // ---------- estilo (paleta y tipografías Höség) ----------

    private void estilizarTablaElegante(JTable t) {
        t.setRowHeight(28);
        t.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        t.setSelectionForeground(COLOR_TEXTO_MAIN);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
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

    private void estilizarBotonSecundario(JButton boton, Color colorBorde) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setBackground(Color.WHITE);
        boton.setForeground(colorBorde);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBorde, 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(colorBorde);
                    boton.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(Color.WHITE);
                    boton.setForeground(colorBorde);
                }
            }
        });
    }

    private static class FranjaDecorativaHoseg extends JComponent {
        FranjaDecorativaHoseg() {
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

    /**
     * Resalta cada fila según el estado de la tabla
     */
    private final class ResaltadoFilaRenderer extends DefaultTableCellRenderer {
        private final EstadoStock estadoFijo;
        private final ModeloDisponibles modeloConsulta;

        ResaltadoFilaRenderer(EstadoStock estadoFijo, ModeloDisponibles modeloConsulta) {
            this.estadoFijo = estadoFijo;
            this.modeloConsulta = modeloConsulta;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component componente = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (isSelected) {
                componente.setBackground(new Color(0xE2, 0xE8, 0xF0));
                componente.setForeground(COLOR_TEXTO_MAIN);
                return componente;
            }

            Color fondo = Color.WHITE;
            Color texto = COLOR_TEXTO_MAIN;

            if (estadoFijo == EstadoStock.SIN_STOCK) {
                fondo = COLOR_FILA_SIN_STOCK;
                texto = COLOR_BURDEO;
            } else if (estadoFijo == EstadoStock.CRITICO) {
                fondo = COLOR_FILA_CRITICO;
                texto = COLOR_CRITICO_TEXTO;
            } else if (modeloConsulta != null) {
                EstadoStock estado = modeloConsulta.obtener(table.convertRowIndexToModel(row)).estado();
                if (estado == EstadoStock.PROXIMO_A_AGOTAR) {
                    fondo = COLOR_FILA_PROXIMO;
                    texto = COLOR_AMBAR;
                }
            }
            componente.setBackground(fondo);
            componente.setForeground(texto);
            return componente;
        }
    }

    private static final class ModeloSinStock extends AbstractTableModel {
        private final String[] columnas = {"Código", "Nombre", "Categoría", "Talla", "Precio (S/)", "Stock mínimo"};
        private List<FilaReporteStock> filas = List.of();

        void actualizar(List<FilaReporteStock> nuevas) {
            this.filas = nuevas;
            fireTableDataChanged();
        }

        FilaReporteStock obtener(int fila) {
            return filas.get(fila);
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int columna) {
            return columnas[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            FilaReporteStock f = filas.get(fila);
            return switch (columna) {
                case 0 -> f.codigo();
                case 1 -> f.nombre();
                case 2 -> f.categoria();
                case 3 -> f.talla();
                case 4 -> f.precio();
                case 5 -> f.stockMinimo();
                default -> null;
            };
        }
    }

    /** Ya llega ordenada de menor a mayor stock comercial (ver ReporteStockService). */
    private static final class ModeloCriticos extends AbstractTableModel {
        private final String[] columnas =
                {"Código", "Nombre", "Categoría", "Talla", "Precio (S/)", "Stock comercial", "Stock mínimo"};
        private List<FilaReporteStock> filas = List.of();

        void actualizar(List<FilaReporteStock> nuevas) {
            this.filas = nuevas;
            fireTableDataChanged();
        }

        FilaReporteStock obtener(int fila) {
            return filas.get(fila);
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int columna) {
            return columnas[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            FilaReporteStock f = filas.get(fila);
            return switch (columna) {
                case 0 -> f.codigo();
                case 1 -> f.nombre();
                case 2 -> f.categoria();
                case 3 -> f.talla();
                case 4 -> f.precio();
                case 5 -> f.stockComercial();
                case 6 -> f.stockMinimo();
                default -> null;
            };
        }
    }

    /** Ya llega ordenada de menor a mayor stock comercial (ver ReporteStockService). */
    private static final class ModeloDisponibles extends AbstractTableModel {
        private final String[] columnas =
                {"Código", "Nombre", "Categoría", "Talla", "Precio (S/)", "Stock comercial", "Stock mínimo", "Estado"};
        private List<FilaReporteStock> filas = List.of();

        void actualizar(List<FilaReporteStock> nuevas) {
            this.filas = nuevas;
            fireTableDataChanged();
        }

        FilaReporteStock obtener(int fila) {
            return filas.get(fila);
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int columna) {
            return columnas[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            FilaReporteStock f = filas.get(fila);
            return switch (columna) {
                case 0 -> f.codigo();
                case 1 -> f.nombre();
                case 2 -> f.categoria();
                case 3 -> f.talla();
                case 4 -> f.precio();
                case 5 -> f.stockComercial();
                case 6 -> f.stockMinimo();
                case 7 -> f.estado().getEtiqueta();
                default -> null;
            };
        }
    }
}