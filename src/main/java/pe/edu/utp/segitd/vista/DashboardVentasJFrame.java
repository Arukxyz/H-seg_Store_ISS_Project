package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.DashboardVentasControlador;
import pe.edu.utp.segitd.modelo.CategoriaDemanda;
import pe.edu.utp.segitd.modelo.FilaDemandaProducto;
import pe.edu.utp.segitd.modelo.FilaRankingProducto;
import pe.edu.utp.segitd.modelo.IndicadorVentasPeriodo;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.FechaUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

/** Dashboard de Ventas y Demanda (valor agregado): indicadores, ranking de productos y demanda real. */
public class DashboardVentasJFrame extends JFrame {

    private final DashboardVentasControlador controlador = new DashboardVentasControlador();

    private final JSpinner spinnerDesde = FechaUtil.crearSpinnerFecha(-30);
    private final JSpinner spinnerHasta = FechaUtil.crearSpinnerFecha(1);

    private final JLabel valorPedidos = new JLabel("-");
    private final JLabel valorUnidades = new JLabel("-");
    private final JLabel valorMonto = new JLabel("-");
    private final JLabel valorTicket = new JLabel("-");

    private final ModeloRanking modeloRanking = new ModeloRanking();
    private final JTable tablaRanking = new JTable(modeloRanking);
    private final ModeloDemanda modeloDemanda = new ModeloDemanda();
    private final JTable tablaDemanda = new JTable(modeloDemanda);

    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3);
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);

    public DashboardVentasJFrame() {
        super("SEGITD-HÖSÉG · Dashboard de Ventas y Demanda");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(1000, 620));
        pack();
        setLocationRelativeTo(null);
        actualizar();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(14, 14));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);
            JPanel panelSuperior = new JPanel(new BorderLayout(0, 14));
            panelSuperior.setBackground(COLOR_FONDO_VENTANA);
            
            panelSuperior.add(new BarraDecorativaHoseg(), BorderLayout.NORTH);
            panelSuperior.add(construirFiltros(), BorderLayout.CENTER);

            raiz.add(panelSuperior, BorderLayout.NORTH);
            raiz.add(construirCentro(), BorderLayout.CENTER);
            return raiz;
    }

    /////
    /// 
    
       private JPanel construirFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setBackground(COLOR_FONDO_VENTANA);

        JLabel lblDesde = new JLabel("Desde:"); 
        lblDesde.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblDesde.setForeground(COLOR_GRIS_TEXTO);
        
        JLabel lblHasta = new JLabel("Hasta:"); 
        lblHasta.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblHasta.setForeground(COLOR_GRIS_TEXTO);
        
        spinnerDesde.setFont(new Font("SansSerif", Font.PLAIN, 12));
        spinnerHasta.setFont(new Font("SansSerif", Font.PLAIN, 12));

        panel.add(lblDesde); panel.add(spinnerDesde);
        panel.add(lblHasta); panel.add(spinnerHasta);

        JButton botonFiltrar = new JButton("Actualizar");
        estilizarBotonPrincipal(botonFiltrar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonFiltrar.addActionListener(e -> actualizar());
        panel.add(botonFiltrar);

        JButton botonExportar = new JButton("Exportar a Excel");
        estilizarBotonSecundario(botonExportar, COLOR_PRIMARIO);
        botonExportar.addActionListener(e -> exportar());
        panel.add(botonExportar);

        return panel;
    }

    private JPanel construirCentro() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.add(construirTarjetas(), BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("SansSerif", Font.BOLD, 12));
        pestanas.setForeground(COLOR_PRIMARIO);
        
        pestanas.addTab("Ranking de productos", construirPanelTabla(tablaRanking));
        
        tablaDemanda.setDefaultRenderer(Object.class, new AlertaDemandaRenderer());
        pestanas.addTab("Análisis de demanda", construirPanelTabla(tablaDemanda));
        
        panel.add(pestanas, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirTarjetas() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.add(tarjeta("Pedidos confirmados", valorPedidos));
        panel.add(tarjeta("Unidades vendidas", valorUnidades));
        panel.add(tarjeta("Monto vendido (S/)", valorMonto));
        panel.add(tarjeta("Ticket promedio (S/)", valorTicket));
        return panel;
    }

    private JPanel tarjeta(String etiqueta, JLabel valor) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE8, 0xF0), 1, true),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));
        
        valor.setFont(new Font("SansSerif", Font.BOLD, 26)); 
        valor.setForeground(COLOR_PRIMARIO); 
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel desc = new JLabel(etiqueta, SwingConstants.CENTER);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(COLOR_GRIS_TEXTO);
        
        wrapper.add(valor, BorderLayout.CENTER);
        wrapper.add(desc, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel construirPanelTabla(JTable tabla) {
        tabla.setRowHeight(26);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        tabla.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        tabla.setSelectionForeground(COLOR_TEXTO_MAIN);
        
        javax.swing.table.JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

////
/// 
    private void actualizar() {
        try {
            OffsetDateTime desde = FechaUtil.inicioDelDia((Date) spinnerDesde.getValue());
            OffsetDateTime hasta = FechaUtil.finDelDia((Date) spinnerHasta.getValue());

            IndicadorVentasPeriodo ind = controlador.calcularIndicadores(desde, hasta);
            valorPedidos.setText(String.valueOf(ind.pedidosConfirmados()));
            valorUnidades.setText(String.valueOf(ind.unidadesVendidas()));
            valorMonto.setText(ind.montoVendido().toPlainString());
            valorTicket.setText(ind.ticketPromedio().toPlainString());

            modeloRanking.actualizar(controlador.rankingProductos(desde, hasta));
            modeloDemanda.actualizar(controlador.analizarDemanda());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportar() {
        try {
            OffsetDateTime desde = FechaUtil.inicioDelDia((Date) spinnerDesde.getValue());
            OffsetDateTime hasta = FechaUtil.finDelDia((Date) spinnerHasta.getValue());
            File archivo = controlador.exportar(desde, hasta);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
            } else {
                JOptionPane.showMessageDialog(this, "Reporte generado en " + archivo.getAbsolutePath());
            }
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


      private void estilizarBotonPrincipal(JButton boton, Color fondo, Color hover) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (boton.isEnabled()) boton.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { if (boton.isEnabled()) boton.setBackground(fondo); }
        });
    }

    private void estilizarBotonSecundario(JButton boton, Color colorBorde) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBackground(Color.WHITE);
        boton.setForeground(colorBorde);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBorde, 1), BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) { boton.setBackground(colorBorde); boton.setForeground(Color.WHITE); }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) { boton.setBackground(Color.WHITE); boton.setForeground(colorBorde); }
            }
        });
    }

     private static final class ModeloRanking extends AbstractTableModel {
        private final String[] columnas = {"Código", "Producto", "Unidades vendidas", "Ingreso generado (S/)"};
        private List<FilaRankingProducto> filas = List.of();

        void actualizar(List<FilaRankingProducto> nuevas) { this.filas = nuevas; fireTableDataChanged(); }
        @Override public int getRowCount() { return filas.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int columna) {
            FilaRankingProducto f = filas.get(fila);
            return switch (columna) {
                case 0 -> f.codigoProducto();
                case 1 -> f.nombreProducto();
                case 2 -> f.unidadesVendidas();
                case 3 -> f.ingresoGenerado();
                default -> null;
            };
        }
    }

    private static final class ModeloDemanda extends AbstractTableModel {
        private final String[] columnas = {"Código", "Producto", "Stock comercial", "Stock mínimo", "Consumo prom./día", "Demanda"};
        private List<FilaDemandaProducto> filas = List.of();

        void actualizar(List<FilaDemandaProducto> nuevas) { this.filas = nuevas; fireTableDataChanged(); }
        FilaDemandaProducto obtener(int fila) { return filas.get(fila); }
        @Override public int getRowCount() { return filas.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int columna) {
            FilaDemandaProducto f = filas.get(fila);
            return switch (columna) {
                case 0 -> f.codigoProducto();
                case 1 -> f.nombreProducto();
                case 2 -> f.stockComercial();
                case 3 -> f.stockMinimoActual();
                case 4 -> Math.round(f.consumoPromedioDiario() * 100) / 100.0;
                case 5 -> f.categoria();
                default -> null;
            };
        }
    }

    private final class AlertaDemandaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            FilaDemandaProducto f = modeloDemanda.obtener(table.convertRowIndexToModel(row));
            boolean bajoMinimo = f.stockComercial() <= f.stockMinimoActual();
            
            if (!isSelected) {
                if (f.categoria() == CategoriaDemanda.ALTA && bajoMinimo) {
                    c.setBackground(new Color(0xFF, 0xEB, 0xEE));
                    c.setForeground(new Color(0x8C, 0x2D, 0x19)); 
                } else if (f.categoria() == CategoriaDemanda.SIN_MOVIMIENTO) {
                    c.setBackground(new Color(0xF5, 0xF5, 0xF5)); 
                    c.setForeground(new Color(0x77, 0x77, 0x77)); 
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(new Color(0x1A, 0x1A, 0x1A));
                }
            }
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return c;
        }
    }

private static class BarraDecorativaHoseg extends JPanel {
    public BarraDecorativaHoseg() {
        setPreferredSize(new Dimension(10, 6)); 
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      
        LinearGradientPaint degradadoHoseg = new LinearGradientPaint(
            0, 0, getWidth(), 0,
            new float[]{0.0f, 0.35f, 0.70f, 1.0f},
            new Color[]{
                new Color(0x00, 0x33, 0xAA), // Azul
                new Color(0x6A, 0x1B, 0x9A), // Morado
                new Color(0xD8, 0x1B, 0x60), // Rosa
                new Color(0xD3, 0x2F, 0x2F)  // Rojo
            }
        );
        g2d.setPaint(degradadoHoseg);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}


private JPanel crearTarjetaMetrica(String titulo, String valor) {
    JPanel tarjeta = new JPanel(new BorderLayout(5, 5));
    tarjeta.setBackground(Color.WHITE);
    // Borde gris suave y redondeado muy estético
    tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE2, 0xE8, 0xF0), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
    
    JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
    lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
    lblTitulo.setForeground(new Color(0x55, 0x55, 0x55)); // Gris elegante
    
    JLabel lblValor = new JLabel(valor, SwingConstants.CENTER);
    lblValor.setFont(new Font("SansSerif", Font.BOLD, 22)); // Número grande
    lblValor.setForeground(new Color(0x2D, 0x3A, 0x33));  // Verde Sea Pine de Höség
    
    tarjeta.add(lblValor, BorderLayout.CENTER);
    tarjeta.add(lblTitulo, BorderLayout.SOUTH);
    
    return tarjeta;
}
    
}