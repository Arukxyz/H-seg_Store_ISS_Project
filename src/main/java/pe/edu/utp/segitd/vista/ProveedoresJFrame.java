package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.ProveedorControlador;
import pe.edu.utp.segitd.modelo.EstadoPedidoProveedor;
import pe.edu.utp.segitd.modelo.PedidoProveedor;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.Proveedor;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CRUD de proveedores y pedidos de reposición (RF-06, sección 8 pantalla 7).
 * Solo ADMINISTRADOR — ya viene deshabilitado en el menú para ENCARGADO.
 */
public class ProveedoresJFrame extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProveedorControlador controlador = new ProveedorControlador();

    // --- pestaña Proveedores ---
    private final ModeloProveedores modeloProveedores = new ModeloProveedores();
    private final JTable tablaProveedores = new JTable(modeloProveedores);
    private final JTextField txtNombreTaller = new JTextField(20);
    private final JTextField txtRuc = new JTextField(12);
    private final JTextField txtContacto = new JTextField(20);
    private final JTextField txtTelefono = new JTextField(14);
    private Integer idProveedorEnEdicion;

    // --- pestaña Pedidos ---
    private final ModeloPedidos modeloPedidos = new ModeloPedidos();
    private final JTable tablaPedidos = new JTable(modeloPedidos);
    private final JComboBox<Proveedor> comboProveedorPedido = new JComboBox<>();
    private final JComboBox<Producto> comboProductoPedido = new JComboBox<>();
    private final JTextField txtDescripcionPedido = new JTextField(20);
    private final JSpinner spinnerCantidadPedido = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
    private final JButton botonRecibido = new JButton("Marcar recibido");
    private final JButton botonAnularPedido = new JButton("Anular pedido");

    public ProveedoresJFrame() {
        super("SEGITD-HÖSÉG · Proveedores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(980, 600));
        pack();
        setLocationRelativeTo(null);
        cargarProveedores();
        cargarProductosCombo();
        cargarPedidos();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Proveedores", construirTabProveedores());
        pestanas.addTab("Pedidos de reposición", construirTabPedidos());
        raiz.add(pestanas, BorderLayout.CENTER);
        return raiz;
    }

    // ---------- Proveedores ----------

    private JPanel construirTabProveedores() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProveedores.getSelectedRow() >= 0) {
                cargarFormularioProveedor(modeloProveedores.obtener(tablaProveedores.convertRowIndexToModel(tablaProveedores.getSelectedRow())));
            }
        });
        panel.add(new JScrollPane(tablaProveedores), BorderLayout.CENTER);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Nombre del taller:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNombreTaller, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        panelFormulario.add(new JLabel("RUC:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtRuc, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panelFormulario.add(new JLabel("Contacto:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtContacto, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panelFormulario.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtTelefono, gbc);

        JPanel panelBotones = new JPanel(new GridLayout(1, 3, 8, 0));
        JButton botonNuevo = new JButton("Nuevo");
        botonNuevo.addActionListener(e -> limpiarFormularioProveedor());
        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardarProveedor());
        JButton botonBaja = new JButton("Dar de baja");
        botonBaja.addActionListener(e -> darDeBajaProveedor());
        panelBotones.add(botonNuevo);
        panelBotones.add(botonGuardar);
        panelBotones.add(botonBaja);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panelFormulario.add(panelBotones, gbc);

        panel.add(panelFormulario, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarProveedores() {
        try {
            List<Proveedor> proveedores = controlador.listarProveedores();
            modeloProveedores.actualizar(proveedores);
            comboProveedorPedido.removeAllItems();
            proveedores.forEach(comboProveedorPedido::addItem);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarFormularioProveedor(Proveedor p) {
        idProveedorEnEdicion = p.getId();
        txtNombreTaller.setText(p.getNombreTaller());
        txtRuc.setText(nvl(p.getRuc()));
        txtContacto.setText(nvl(p.getContacto()));
        txtTelefono.setText(nvl(p.getTelefono()));
    }

    private void limpiarFormularioProveedor() {
        idProveedorEnEdicion = null;
        txtNombreTaller.setText("");
        txtRuc.setText("");
        txtContacto.setText("");
        txtTelefono.setText("");
        tablaProveedores.clearSelection();
    }

    private void guardarProveedor() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(idProveedorEnEdicion);
        proveedor.setNombreTaller(txtNombreTaller.getText().trim());
        proveedor.setRuc(vacioComoNulo(txtRuc.getText()));
        proveedor.setContacto(vacioComoNulo(txtContacto.getText()));
        proveedor.setTelefono(vacioComoNulo(txtTelefono.getText()));
        proveedor.setActivo(true);

        try {
            if (idProveedorEnEdicion == null) {
                controlador.crearProveedor(proveedor);
            } else {
                controlador.actualizarProveedor(proveedor);
            }
            cargarProveedores();
            limpiarFormularioProveedor();
            JOptionPane.showMessageDialog(this, "Proveedor guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void darDeBajaProveedor() {
        if (idProveedorEnEdicion == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un proveedor de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Dar de baja este proveedor?", "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.desactivarProveedor(idProveedorEnEdicion);
            cargarProveedores();
            limpiarFormularioProveedor();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Pedidos ----------

    private JPanel construirTabPedidos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, construirPanelListaPedidos(), construirPanelNuevoPedido());
        split.setResizeWeight(0.65);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelListaPedidos() {
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesPedido();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Pedidos"));
        panel.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botonRecibido.setEnabled(false);
        botonAnularPedido.setEnabled(false);
        botonRecibido.addActionListener(e -> marcarRecibido());
        botonAnularPedido.addActionListener(e -> anularPedido());
        panelBotones.add(botonRecibido);
        panelBotones.add(botonAnularPedido);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirPanelNuevoPedido() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo pedido"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        panel.add(new JLabel("Proveedor:"), gbc);
        gbc.gridx = 1;
        panel.add(comboProveedorPedido, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Producto a reponer:"), gbc);
        gbc.gridx = 1;
        panel.add(comboProductoPedido, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        panel.add(txtDescripcionPedido, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 1;
        panel.add(spinnerCantidadPedido, gbc);

        JButton botonRegistrar = new JButton("Registrar pedido");
        botonRegistrar.addActionListener(e -> registrarPedido());
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(botonRegistrar, gbc);

        return panel;
    }

    private void cargarProductosCombo() {
        try {
            comboProductoPedido.removeAllItems();
            controlador.listarProductos().forEach(comboProductoPedido::addItem);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarPedidos() {
        try {
            modeloPedidos.actualizar(controlador.listarPedidos());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        actualizarBotonesPedido();
    }

    private PedidoProveedor pedidoSeleccionado() {
        int fila = tablaPedidos.getSelectedRow();
        return fila < 0 ? null : modeloPedidos.obtener(tablaPedidos.convertRowIndexToModel(fila));
    }

    private void actualizarBotonesPedido() {
        PedidoProveedor pedido = pedidoSeleccionado();
        boolean solicitado = pedido != null && pedido.getEstado() == EstadoPedidoProveedor.SOLICITADO;
        botonRecibido.setEnabled(solicitado);
        botonAnularPedido.setEnabled(solicitado);
    }

    private void registrarPedido() {
        Proveedor proveedor = (Proveedor) comboProveedorPedido.getSelectedItem();
        Producto producto = (Producto) comboProductoPedido.getSelectedItem();
        String descripcion = txtDescripcionPedido.getText().trim();
        int cantidad = ((Number) spinnerCantidadPedido.getValue()).intValue();

        if (proveedor == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un proveedor.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            controlador.crearPedido(proveedor.getId(), producto == null ? null : producto.getCodigo(), descripcion, cantidad);
            txtDescripcionPedido.setText("");
            spinnerCantidadPedido.setValue(1);
            cargarPedidos();
            JOptionPane.showMessageDialog(this, "Pedido registrado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void marcarRecibido() {
        PedidoProveedor pedido = pedidoSeleccionado();
        if (pedido == null) {
            return;
        }
        try {
            controlador.marcarRecibido(pedido.getId());
            cargarPedidos();
            JOptionPane.showMessageDialog(this, "Pedido recibido: stock actualizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void anularPedido() {
        PedidoProveedor pedido = pedidoSeleccionado();
        if (pedido == null) {
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Anular este pedido?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.anularPedido(pedido.getId());
            cargarPedidos();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }

    private String vacioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static final class ModeloProveedores extends AbstractTableModel {
        private final String[] columnas = {"Taller", "RUC", "Contacto", "Teléfono"};
        private List<Proveedor> proveedores = List.of();

        void actualizar(List<Proveedor> nuevos) {
            this.proveedores = nuevos;
            fireTableDataChanged();
        }

        Proveedor obtener(int fila) {
            return proveedores.get(fila);
        }

        @Override
        public int getRowCount() {
            return proveedores.size();
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
            Proveedor p = proveedores.get(fila);
            return switch (columna) {
                case 0 -> p.getNombreTaller();
                case 1 -> p.getRuc();
                case 2 -> p.getContacto();
                case 3 -> p.getTelefono();
                default -> null;
            };
        }
    }

    private static final class ModeloPedidos extends AbstractTableModel {
        private final String[] columnas = {"Proveedor", "Producto", "Descripción", "Cantidad", "Fecha", "Estado"};
        private List<PedidoProveedor> pedidos = List.of();

        void actualizar(List<PedidoProveedor> nuevos) {
            this.pedidos = nuevos;
            fireTableDataChanged();
        }

        PedidoProveedor obtener(int fila) {
            return pedidos.get(fila);
        }

        @Override
        public int getRowCount() {
            return pedidos.size();
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
            PedidoProveedor p = pedidos.get(fila);
            return switch (columna) {
                case 0 -> p.getNombreTaller();
                case 1 -> p.getNombreProducto() == null ? "—" : p.getNombreProducto();
                case 2 -> p.getDescripcion();
                case 3 -> p.getCantidad();
                case 4 -> FORMATO_FECHA.format(p.getFecha());
                case 5 -> p.getEstado();
                default -> null;
            };
        }
    }
}
