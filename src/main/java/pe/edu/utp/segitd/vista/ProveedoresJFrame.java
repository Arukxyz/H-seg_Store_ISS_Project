package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.ProveedorControlador;
import pe.edu.utp.segitd.modelo.EstadoPedidoProveedor;
import pe.edu.utp.segitd.modelo.PedidoProveedor;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.Proveedor;
import pe.edu.utp.segitd.servicio.ServicioException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
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

    // COLORES (mismo sistema que GestionProductosJFrame)
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3);
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);
    private final Color COLOR_BORDE = new Color(0xE2, 0xE2, 0xE0);

    private final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private final Font FUENTE_INPUT = new Font("SansSerif", Font.PLAIN, 13);

    public ProveedoresJFrame() {
        super("SEGITD-HÖSÉG · Proveedores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);
        setMinimumSize(new Dimension(1020, 620));
        pack();
        setLocationRelativeTo(null);
        cargarProveedores();
        cargarProductosCombo();
        cargarPedidos();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
        panelTop.setBackground(COLOR_FONDO_VENTANA);

        JLabel titulo = new JLabel("Proveedores y pedidos de reposición");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(COLOR_TEXTO_MAIN);
        panelTop.add(titulo);
        panelTop.add(Box.createVerticalStrut(10));
        panelTop.add(new FranjaDecorativaHoseg());

        raiz.add(panelTop, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("SansSerif", Font.BOLD, 13));
        pestanas.setBackground(COLOR_FONDO_VENTANA);
        pestanas.addTab("Proveedores", construirTabProveedores());
        pestanas.addTab("Pedidos de reposición", construirTabPedidos());
        raiz.add(pestanas, BorderLayout.CENTER);
        return raiz;
    }

    // ---------- Proveedores ----------

    private JPanel construirTabProveedores() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        panel.setBackground(COLOR_FONDO_VENTANA);

        panel.add(construirPanelTablaProveedores(), BorderLayout.CENTER);
        panel.add(construirTarjetaFormularioProveedor(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirPanelTablaProveedores() {
        estilizarTabla(tablaProveedores);
        tablaProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProveedores.getSelectedRow() >= 0) {
                cargarFormularioProveedor(modeloProveedores.obtener(tablaProveedores.convertRowIndexToModel(tablaProveedores.getSelectedRow())));
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO_VENTANA);
        JScrollPane scroll = new JScrollPane(tablaProveedores);
        scroll.setBorder(new LineBorder(COLOR_BORDE, 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirTarjetaFormularioProveedor() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 16));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        agregarCampo(panelFormulario, gbc, 0, "Nombre del taller:", txtNombreTaller);
        agregarCampo(panelFormulario, gbc, 1, "RUC:", txtRuc);
        agregarCampo(panelFormulario, gbc, 2, "Contacto:", txtContacto);
        agregarCampo(panelFormulario, gbc, 3, "Teléfono:", txtTelefono);

        tarjeta.add(panelFormulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new GridLayout(1, 3, 10, 0));
        panelBotones.setBackground(Color.WHITE);
        JButton botonNuevo = new JButton("Nuevo");
        botonNuevo.addActionListener(e -> limpiarFormularioProveedor());
        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardarProveedor());
        JButton botonBaja = new JButton("Dar de baja");
        botonBaja.addActionListener(e -> darDeBajaProveedor());

        estilizarBotonSecundario(botonNuevo, COLOR_PRIMARIO);
        estilizarBotonPrincipal(botonGuardar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        estilizarBotonSecundario(botonBaja, COLOR_BURDEO);

        panelBotones.add(botonNuevo);
        panelBotones.add(botonGuardar);
        panelBotones.add(botonBaja);

        tarjeta.add(panelBotones, BorderLayout.SOUTH);
        return tarjeta;
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
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        panel.setBackground(COLOR_FONDO_VENTANA);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, construirPanelListaPedidos(), construirTarjetaNuevoPedido());
        split.setResizeWeight(0.6);
        split.setBorder(null);
        split.setBackground(COLOR_FONDO_VENTANA);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelListaPedidos() {
        estilizarTabla(tablaPedidos);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesPedido();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.setBorder(construirBordeTitulado("Pedidos"));

        JScrollPane scroll = new JScrollPane(tablaPedidos);
        scroll.setBorder(new LineBorder(COLOR_BORDE, 1));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelBotones.setBackground(COLOR_FONDO_VENTANA);
        botonRecibido.setEnabled(false);
        botonAnularPedido.setEnabled(false);
        estilizarBotonPrincipal(botonRecibido, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        estilizarBotonSecundario(botonAnularPedido, COLOR_BURDEO);
        botonRecibido.addActionListener(e -> marcarRecibido());
        botonAnularPedido.addActionListener(e -> anularPedido());
        panelBotones.add(botonRecibido);
        panelBotones.add(botonAnularPedido);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirTarjetaNuevoPedido() {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel subtitulo = new JLabel("Nuevo pedido");
        subtitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        subtitulo.setForeground(COLOR_TEXTO_MAIN);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(subtitulo, gbc);
        gbc.gridwidth = 1;

        agregarCampo(panel, gbc, 1, "Proveedor:", comboProveedorPedido);
        agregarCampo(panel, gbc, 2, "Producto a reponer:", comboProductoPedido);
        agregarCampo(panel, gbc, 3, "Descripción:", txtDescripcionPedido);
        agregarCampo(panel, gbc, 4, "Cantidad:", spinnerCantidadPedido);

        JButton botonRegistrar = new JButton("Registrar pedido");
        estilizarBotonPrincipal(botonRegistrar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonRegistrar.addActionListener(e -> registrarPedido());
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 4, 6);
        panel.add(botonRegistrar, gbc);

        tarjeta.add(panel, BorderLayout.CENTER);
        return tarjeta;
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

    // ---------- helpers de estilo ----------

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.gridwidth = 1;

        JLabel label = new JLabel(etiqueta);
        label.setFont(FUENTE_LABEL);
        label.setForeground(COLOR_GRIS_TEXTO);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;

        if (campo instanceof JTextField) {
            estilizarComponenteForm((JTextField) campo);
        } else if (campo instanceof JComboBox) {
            campo.setFont(FUENTE_INPUT);
            campo.setBackground(Color.WHITE);
            campo.setBorder(BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1));
        } else if (campo instanceof JSpinner) {
            estilizarComponenteForm((JComponent) ((JSpinner) campo).getEditor());
        }
        panel.add(campo, gbc);
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        tabla.setSelectionForeground(COLOR_TEXTO_MAIN);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(COLOR_BORDE);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private TitledBorder construirBordeTitulado(String titulo) {
        TitledBorder borde = BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE, 1), titulo);
        borde.setTitleFont(new Font("SansSerif", Font.BOLD, 13));
        borde.setTitleColor(COLOR_TEXTO_MAIN);
        return borde;
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
        boton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

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
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

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

    // ---------- modelos de tabla ----------

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