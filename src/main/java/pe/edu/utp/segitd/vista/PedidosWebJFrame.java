package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.PedidosWebControlador;
import pe.edu.utp.segitd.modelo.DetalleVenta;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoVenta;
import pe.edu.utp.segitd.modelo.Venta;
import pe.edu.utp.segitd.servicio.ServicioException;

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
import javax.swing.SpinnerDateModel;
import javax.swing.Timer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
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
    private final JSpinner spinnerDesde = crearSpinnerFecha(-30);
    private final JSpinner spinnerHasta = crearSpinnerFecha(1);

    private final JButton botonConfirmar = new JButton("Confirmar pedido");
    private final JButton botonAnular = new JButton("Anular pedido");
    private final JLabel etiquetaEstado = new JLabel(" ");

    private final Timer timerActualizacion = new Timer(30_000, e -> cargarPedidos());

    public PedidosWebJFrame() {
        super("SEGITD-HÖSÉG · Pedidos web");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(980, 620));
        pack();
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
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        raiz.add(construirFiltros(), BorderLayout.NORTH);

        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, construirPanelPedidos(), construirPanelDetalle());
        splitVertical.setResizeWeight(0.55);
        raiz.add(splitVertical, BorderLayout.CENTER);

        raiz.add(construirPanelAcciones(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirFiltros() {
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
        return panel;
    }

    private JPanel construirPanelPedidos() {
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDetalleSeleccionado();
            }
        });
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pedidos"));
        panel.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelDetalle() {
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
        return panel;
    }

    private void cargarPedidos() {
        try {
            EstadoVenta estado = (EstadoVenta) comboEstado.getSelectedItem();
            OffsetDateTime desde = inicioDelDia((Date) spinnerDesde.getValue());
            OffsetDateTime hasta = finDelDia((Date) spinnerHasta.getValue());
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

    private void actualizarBotones() {
        Venta venta = ventaSeleccionada();
        botonConfirmar.setEnabled(venta != null && venta.getEstado() == EstadoVenta.PENDIENTE);
        botonAnular.setEnabled(venta != null && venta.getEstado() != EstadoVenta.ANULADO);
    }

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

    private static JSpinner crearSpinnerFecha(int diasDesdeHoy) {
        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_MONTH, diasDesdeHoy);
        SpinnerDateModel modelo = new SpinnerDateModel(calendario.getTime(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(modelo);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        return spinner;
    }

    private static OffsetDateTime inicioDelDia(Date fecha) {
        LocalDate local = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return local.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private static OffsetDateTime finDelDia(Date fecha) {
        LocalDate local = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return local.atTime(LocalTime.of(23, 59, 59)).atZone(ZoneId.systemDefault()).toOffsetDateTime();
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
}
