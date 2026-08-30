package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.PedidosWebControlador;
import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoVenta;
import pe.edu.utp.segitd.modelo.Venta;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.FechaUtil;

<<<<<<< HEAD

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.table.AbstractTableModel;

=======
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Recepción y gestión de pedidos web (RF-04, sección 8 pantalla 4). Un
 * Timer refresca la lista cada 30 s para evidenciar en vivo que la web y
 * el escritorio comparten la misma base de datos.
 */
public class PedidosWebJFrame extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PedidosWebControlador controlador = new PedidosWebControlador();

    private final ModeloTablaPedidos modeloPedidos = new ModeloTablaPedidos();
    private final JTable tablaPedidos = new JTable(modeloPedidos);
    private final ModeloTablaDetalle modeloDetalle = new ModeloTablaDetalle();
    private final JTable tablaDetalle = new JTable(modeloDetalle);
    private final ModeloTablaDonaciones modeloDonaciones = new ModeloTablaDonaciones();
    private final JTable tablaDonaciones = new JTable(modeloDonaciones);

    private final JComboBox<EstadoVenta> comboEstado =
            new JComboBox<>(new EstadoVenta[]{null, EstadoVenta.PENDIENTE, EstadoVenta.PAGADO, EstadoVenta.ANULADO});
    private final JSpinner spinnerDesde = FechaUtil.crearSpinnerFecha(-30);
    private final JSpinner spinnerHasta = FechaUtil.crearSpinnerFecha(1);

    private final JButton botonConfirmar = new JButton("Confirmar pedido");
    private final JButton botonAnular = new JButton("Anular pedido");
    private final JLabel etiquetaEstado = new JLabel(" ");

    private final Timer timerActualizacion = new Timer(30_000, e -> cargarPedidos());

<<<<<<< HEAD

    //DISEÑO
     private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3); // Crema suave
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);      // Verde Sea Pine
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);       // Terracota / Fuego
    private final Color COLOR_BURDEO_HOVER = new Color(0xA6, 0x3A, 0x24);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);

    private final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private final Font FUENTE_INPUT = new Font("SansSerif", Font.PLAIN, 13);
    private final Font FUENTE_PANEL_TITULO = new Font("SansSerif", Font.BOLD, 14);

    public PedidosWebJFrame() {
        super("SEGITD-HÖSÉG · Pedidos web");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);

        setPreferredSize(new Dimension(1050, 610));
        setMinimumSize(new Dimension(980, 560));
        pack();
        setSize(1050, 610);
=======
    public PedidosWebJFrame() {
        super("SEGITD-HÖSÉG · Pedidos web");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(980, 620));
        pack();
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        setLocationRelativeTo(null);
        cargarPedidos();
        timerActualizacion.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timerActualizacion.stop();
            }
        });
    }

    private JPanel construirContenido() {
<<<<<<< HEAD
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);
        
        JPanel panelTopCompleto = new JPanel();
        panelTopCompleto.setLayout(new BoxLayout(panelTopCompleto, BoxLayout.Y_AXIS));
        panelTopCompleto.setBackground(COLOR_FONDO_VENTANA);
        panelTopCompleto.add(construirFiltros());
        panelTopCompleto.add(Box.createVerticalStrut(12));
        panelTopCompleto.add(new FranjaDecorativaHoseg());
        
        raiz.add(panelTopCompleto, BorderLayout.NORTH);

        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, construirPanelPedidos(), construirPanelDetalle());
        splitVertical.setResizeWeight(0.5);
        splitVertical.setDividerLocation(220); 
        splitVertical.setBorder(null);
        splitVertical.setBackground(COLOR_FONDO_VENTANA);
        if (splitVertical.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
            ((javax.swing.plaf.basic.BasicSplitPaneUI) splitVertical.getUI()).getDivider().setBorder(null);
        }

        raiz.add(splitVertical, BorderLayout.CENTER);
=======
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        raiz.add(construirFiltros(), BorderLayout.NORTH);

        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, construirPanelPedidos(), construirPanelDetalle());
        splitVertical.setResizeWeight(0.55);
        raiz.add(splitVertical, BorderLayout.CENTER);

>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        raiz.add(construirPanelAcciones(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirFiltros() {
<<<<<<< HEAD
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panel.setBackground(COLOR_FONDO_VENTANA);

        comboEstado.setRenderer(new EstadoListRenderer());
        comboEstado.setFont(FUENTE_INPUT);
        comboEstado.setBackground(Color.WHITE);
        comboEstado.setBorder(new LineBorder(new Color(0xD3, 0xD3, 0xD3), 1));

        estilizarSpinnerFecha(spinnerDesde);
        estilizarSpinnerFecha(spinnerHasta);

        JLabel lblEstado = new JLabel("Estado:"); lblEstado.setFont(FUENTE_LABEL); lblEstado.setForeground(COLOR_GRIS_TEXTO);
        JLabel lblDesde = new JLabel("Desde:"); lblDesde.setFont(FUENTE_LABEL); lblDesde.setForeground(COLOR_GRIS_TEXTO);
        JLabel lblHasta = new JLabel("Hasta:"); lblHasta.setFont(FUENTE_LABEL); lblHasta.setForeground(COLOR_GRIS_TEXTO);

        panel.add(lblEstado);
        panel.add(comboEstado);
        panel.add(lblDesde);
        panel.add(spinnerDesde);
        panel.add(lblHasta);
        panel.add(spinnerHasta);

        JButton botonFiltrar = crearBotonHeader("Filtrar", COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonFiltrar.addActionListener(e -> cargarPedidos());
        panel.add(botonFiltrar);

        JButton botonActualizar = crearBotonHeader("Actualizar", COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonActualizar.addActionListener(e -> cargarPedidos());
        panel.add(botonActualizar);

        etiquetaEstado.setFont(new Font("SansSerif", Font.ITALIC, 12));
        etiquetaEstado.setForeground(COLOR_GRIS_TEXTO);
        panel.add(etiquetaEstado);

=======
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        comboEstado.setRenderer(new EstadoListRenderer());
        panel.add(new JLabel("Estado:"));
        panel.add(comboEstado);
        panel.add(new JLabel("Desde:"));
        panel.add(spinnerDesde);
        panel.add(new JLabel("Hasta:"));
        panel.add(spinnerHasta);
        JButton botonFiltrar = new JButton("Filtrar");
        botonFiltrar.addActionListener(e -> cargarPedidos());
        panel.add(botonFiltrar);
        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(e -> cargarPedidos());
        panel.add(botonActualizar);
        panel.add(etiquetaEstado);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        return panel;
    }

    private JPanel construirPanelPedidos() {
<<<<<<< HEAD
        estilizarTablaElegante(tablaPedidos);

=======
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDetalleSeleccionado();
            }
        });
<<<<<<< HEAD

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0xE2, 0xE2, 0xE0), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        
        JLabel lblSeccion = new JLabel("Historial de Pedidos Web");
        lblSeccion.setFont(FUENTE_PANEL_TITULO);
        lblSeccion.setForeground(COLOR_TEXTO_MAIN);
        lblSeccion.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(lblSeccion, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(tablaPedidos);
        scrollPane.setBorder(new javax.swing.border.LineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panel.add(scrollPane, BorderLayout.CENTER);
=======
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pedidos"));
        panel.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        return panel;
    }

    private JPanel construirPanelDetalle() {
<<<<<<< HEAD
         JPanel panelContenedorDual = new JPanel(new GridLayout(1, 2, 16, 0));
        panelContenedorDual.setBackground(COLOR_FONDO_VENTANA);
        panelContenedorDual.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        estilizarTablaElegante(tablaDetalle);
        JPanel panelDetalle = crearContenedorTarjeta("Detalle de la Compra");
        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(new javax.swing.border.LineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panelDetalle.add(scrollDetalle, BorderLayout.CENTER);
        panelContenedorDual.add(panelDetalle);

        estilizarTablaElegante(tablaDonaciones);
        JPanel panelDonaciones = crearContenedorTarjeta("Impacto Social — Donaciones Generadas");
        JScrollPane scrollDonaciones = new JScrollPane(tablaDonaciones);
        scrollDonaciones.setBorder(new javax.swing.border.LineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panelDonaciones.add(scrollDonaciones, BorderLayout.CENTER);
        panelContenedorDual.add(panelDonaciones);

        return panelContenedorDual;
    }

    private JPanel construirPanelAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        panel.setBackground(COLOR_FONDO_VENTANA);
        
        botonConfirmar.setEnabled(false);
        botonAnular.setEnabled(false);
        
        estilizarBotonPrincipal(botonConfirmar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        estilizarBotonSecundario(botonAnular, COLOR_BURDEO);

        botonConfirmar.addActionListener(e -> confirmarSeleccionado());
        botonAnular.addActionListener(e -> anularSeleccionado());
        
        panel.add(botonAnular);
        panel.add(botonConfirmar);
=======
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));

        JPanel panelDetalle = new JPanel(new BorderLayout());
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del pedido"));
        panelDetalle.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);
        panel.add(panelDetalle);

        JPanel panelDonaciones = new JPanel(new BorderLayout());
        panelDonaciones.setBorder(BorderFactory.createTitledBorder("Donaciones generadas"));
        panelDonaciones.add(new JScrollPane(tablaDonaciones), BorderLayout.CENTER);
        panel.add(panelDonaciones);

        return panel;
    }

    private JPanel construirPanelAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botonConfirmar.setEnabled(false);
        botonAnular.setEnabled(false);
        botonConfirmar.addActionListener(e -> confirmarSeleccionado());
        botonAnular.addActionListener(e -> anularSeleccionado());
        panel.add(botonConfirmar);
        panel.add(botonAnular);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        return panel;
    }

    private void cargarPedidos() {
        try {
            EstadoVenta estado = (EstadoVenta) comboEstado.getSelectedItem();
            OffsetDateTime desde = FechaUtil.inicioDelDia((Date) spinnerDesde.getValue());
            OffsetDateTime hasta = FechaUtil.finDelDia((Date) spinnerHasta.getValue());
            modeloPedidos.actualizar(controlador.listarPedidos(estado, desde, hasta));
            etiquetaEstado.setText("Actualizado: " + FORMATO_FECHA.format(OffsetDateTime.now()));
        } catch (ServicioException e) {
            etiquetaEstado.setText("Error al actualizar.");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        actualizarBotones();
    }

    private void cargarDetalleSeleccionado() {
        Venta venta = ventaSeleccionada();
        if (venta == null) {
            modeloDetalle.actualizar(List.of());
            modeloDonaciones.actualizar(List.of());
            actualizarBotones();
            return;
        }
        try {
            modeloDetalle.actualizar(controlador.listarDetalle(venta.getId()));
            modeloDonaciones.actualizar(controlador.listarDonaciones(venta.getId()));
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        actualizarBotones();
    }

<<<<<<< HEAD

    //
        private void actualizarBotones() {
        Venta venta = ventaSeleccionada();
        
        boolean sePuedeConfirmar = venta != null && venta.getEstado() == EstadoVenta.PENDIENTE;
        boolean sePuedeAnular = venta != null && venta.getEstado() != EstadoVenta.ANULADO;
        
        botonConfirmar.setEnabled(sePuedeConfirmar);
        botonAnular.setEnabled(sePuedeAnular);
        
        if (!sePuedeConfirmar) {
            botonConfirmar.setBackground(new Color(0xE0, 0xE0, 0xE0)); 
            botonConfirmar.setForeground(new Color(0x9E, 0x9E, 0x9E));
        } else {
            botonConfirmar.setBackground(COLOR_PRIMARIO); 
            botonConfirmar.setForeground(Color.WHITE);
        }
        
        if (!sePuedeAnular) {
            botonAnular.setBackground(new Color(0xE0, 0xE0, 0xE0));
            botonAnular.setForeground(new Color(0x9E, 0x9E, 0x9E));
            botonAnular.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(0xBD, 0xBD, 0xBD), 1), 
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        } else {
            botonAnular.setBackground(Color.WHITE);
            botonAnular.setForeground(COLOR_BURDEO);
            botonAnular.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(COLOR_BURDEO, 1), 
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        }
    }


=======
    private void actualizarBotones() {
        Venta venta = ventaSeleccionada();
        botonConfirmar.setEnabled(venta != null && venta.getEstado() == EstadoVenta.PENDIENTE);
        botonAnular.setEnabled(venta != null && venta.getEstado() != EstadoVenta.ANULADO);
    }

>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
    private Venta ventaSeleccionada() {
        int fila = tablaPedidos.getSelectedRow();
        return fila < 0 ? null : modeloPedidos.obtener(tablaPedidos.convertRowIndexToModel(fila));
    }

    private void confirmarSeleccionado() {
        Venta venta = ventaSeleccionada();
        if (venta == null) {
            return;
        }
        try {
            controlador.confirmarPedido(venta.getId());
            JOptionPane.showMessageDialog(this, "Pedido confirmado: stock actualizado y donaciones generadas.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPedidos();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void anularSeleccionado() {
        Venta venta = ventaSeleccionada();
        if (venta == null) {
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Anular el pedido " + venta.getCodigoComprobante() + "? Esto revierte el stock si ya estaba pagado.",
                "Confirmar anulación", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.anularPedido(venta.getId());
            JOptionPane.showMessageDialog(this, "Pedido anulado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPedidos();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class EstadoListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            Object texto = value == null ? "Todos" : value;
            return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
        }
    }

    private static final class ModeloTablaPedidos extends AbstractTableModel {
        private final String[] columnas = {"Comprobante", "Fecha", "Cliente", "Estado", "Total"};
        private List<Venta> ventas = List.of();

        void actualizar(List<Venta> nuevas) {
            this.ventas = nuevas;
            fireTableDataChanged();
        }

        Venta obtener(int fila) {
            return ventas.get(fila);
        }

        @Override
        public int getRowCount() {
            return ventas.size();
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
            Venta v = ventas.get(fila);
            return switch (columna) {
                case 0 -> v.getCodigoComprobante();
                case 1 -> FORMATO_FECHA.format(v.getFecha());
                case 2 -> v.getClienteNombre();
                case 3 -> v.getEstado();
                case 4 -> v.getTotal();
                default -> null;
            };
        }
    }

    private static final class ModeloTablaDetalle extends AbstractTableModel {
        private final String[] columnas = {"Producto", "Cantidad", "Precio unit.", "Subtotal"};
        private List<DetalleVenta> lineas = List.of();

        void actualizar(List<DetalleVenta> nuevas) {
            this.lineas = nuevas;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return lineas.size();
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
            DetalleVenta d = lineas.get(fila);
            return switch (columna) {
                case 0 -> d.getNombreProducto();
                case 1 -> d.getCantidad();
                case 2 -> d.getPrecioUnitario();
                case 3 -> d.getSubtotal();
                default -> null;
            };
        }
    }

    private static final class ModeloTablaDonaciones extends AbstractTableModel {
        private final String[] columnas = {"Producto", "Cantidad", "Tipo", "Estado"};
        private List<Donacion> donaciones = List.of();

        void actualizar(List<Donacion> nuevas) {
            this.donaciones = nuevas;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return donaciones.size();
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
            Donacion d = donaciones.get(fila);
            return switch (columna) {
                case 0 -> d.getNombreProducto();
                case 1 -> d.getCantidad();
                case 2 -> d.getTipo();
                case 3 -> d.getEstado();
                default -> null;
            };
        }
    }
<<<<<<< HEAD

    //metodos extras
    private void estilizarTablaElegante(JTable t) {
        t.setRowHeight(26); 
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        
        DefaultTableCellRenderer renderizadorPadding = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        t.setDefaultRenderer(Object.class, renderizadorPadding);
    }

    private JPanel crearContenedorTarjeta(String titulo) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0xE2, 0xE2, 0xE0), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FUENTE_PANEL_TITULO);
        lblTitulo.setForeground(COLOR_TEXTO_MAIN);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        return tarjeta;
    }

    private void estilizarBotonPrincipal(JButton boton, Color fondo, Color hover) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (boton.isEnabled()) boton.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { if (boton.isEnabled()) boton.setBackground(fondo); }
        });
    }

    private void estilizarBotonSecundario(JButton boton, Color colorBorde) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setBackground(Color.WHITE);
        boton.setForeground(colorBorde);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(colorBorde, 1), BorderFactory.createEmptyBorder(10, 16, 10, 16)));
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

    private JButton crearBotonHeader(String texto, Color base, Color hover) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBackground(base);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(base);
            }
        });
        return boton;
    }

    private void estilizarSpinnerFecha(JSpinner spinner) {
        spinner.setFont(FUENTE_INPUT);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField txt = ((JSpinner.DefaultEditor) editor).getTextField();
            txt.setBackground(Color.WHITE);
            txt.setForeground(COLOR_TEXTO_MAIN);
            txt.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0xD3, 0xD3, 0xD3), 1),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)
            ));
        }
    }

    //CLASE FRANJA
    private static class FranjaDecorativaHoseg extends JComponent {
        public FranjaDecorativaHoseg() {
            setPreferredSize(new Dimension(100, 4));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Degradado lineal de Höség: Azul -> Morado -> Rosa -> Rojo
            LinearGradientPaint degradado = new LinearGradientPaint(
                0, 0, getWidth(), 0,
                new float[]{0.0f, 0.35f, 0.70f, 1.0f},
                new Color[]{
                    new Color(0x00, 0x33, 0xAA), // Azul
                    new Color(0x6A, 0x1B, 0x9A), // Morado
                    new Color(0xD8, 0x1B, 0x60), // Rosa / Magenta
                    new Color(0xD3, 0x2F, 0x2F)  // Rojo
                }
            );

            g2d.setPaint(degradado);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }


=======
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
}
