package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.GestionProductosControlador;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.TipoCompromiso;
import pe.edu.utp.segitd.modelo.TipoStock;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

/**
 * Catálogo de productos e inventario dual (RF-02 / RF-03, sección 8
 * pantalla 3). ENCARGADO puede consultar; solo ADMINISTRADOR puede
 * crear, editar, dar de baja o ajustar stock.
 */
public class GestionProductosJFrame extends JFrame {

    private final GestionProductosControlador controlador = new GestionProductosControlador();
    private final boolean esAdministrador =
            SesionUsuario.obtenerInstancia().getUsuarioActual().getRol() == RolUsuario.ADMINISTRADOR;

    private final ModeloTablaProductos modeloTabla = new ModeloTablaProductos();
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtCodigo = new JTextField(14);
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtMarca = new JTextField(20);
    private final JTextField txtCategoria = new JTextField(20);
    private final JTextField txtColeccion = new JTextField(20);
    private final JTextField txtTalla = new JTextField(6);
    private final JTextField txtDescripcion = new JTextField(20);
    private final JTextField txtUrlImagen = new JTextField(20);
    private final JTextField txtPrecio = new JTextField(10);
    private final JSpinner spinnerStockComercial = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
    private final JSpinner spinnerStockMinimo = new JSpinner(new SpinnerNumberModel(5, 0, 1_000_000, 1));
    private final JCheckBox checkAplicaTripleImpacto = new JCheckBox("Aplica triple impacto", true);
    private final JComboBox<TipoCompromiso> comboTipoCompromiso =
            new JComboBox<>(new TipoCompromiso[]{null, TipoCompromiso.ABRIGO, TipoCompromiso.ARBOL});
    private final JCheckBox checkVisibleWeb = new JCheckBox("Visible en la web", true);

    private final JButton botonNuevo = new JButton("Nuevo");
    private final JButton botonGuardar = new JButton("Guardar");
    private final JButton botonEliminar = new JButton("Dar de baja");
    private final JButton botonAjustarStock = new JButton("Ajustar stock");

    private String codigoEnEdicion;

    public GestionProductosJFrame() {
        super("SEGITD-HÖSÉG · Gestión de productos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(920, 560));
        aplicarControlAcceso();
        pack();
        setLocationRelativeTo(null);
        cargarProductos();
        limpiarFormulario();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirPanelTabla(), construirPanelFormulario());
        splitPane.setResizeWeight(0.6);
        raiz.add(splitPane, BorderLayout.CENTER);
        return raiz;
    }

    private JPanel construirPanelTabla() {
        tabla.setDefaultRenderer(Object.class, new ResaltadoStockBajoRenderer());
        tabla.setRowHeight(22);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                cargarFormulario(modeloTabla.obtener(tabla.convertRowIndexToModel(tabla.getSelectedRow())));
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JLabel leyenda = new JLabel("Las filas en rojo tienen el stock comercial en el mínimo o por debajo.");
        leyenda.setForeground(Color.GRAY);
        panel.add(leyenda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(construirFormulario(), BorderLayout.CENTER);
        panel.add(construirBotones(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        int fila = 0;
        agregarCampo(panel, gbc, fila++, "Código:", txtCodigo);
        agregarCampo(panel, gbc, fila++, "Nombre:", txtNombre);
        agregarCampo(panel, gbc, fila++, "Marca:", txtMarca);
        agregarCampo(panel, gbc, fila++, "Categoría:", txtCategoria);
        agregarCampo(panel, gbc, fila++, "Colección:", txtColeccion);
        agregarCampo(panel, gbc, fila++, "Talla:", txtTalla);
        agregarCampo(panel, gbc, fila++, "Descripción:", txtDescripcion);
        agregarCampo(panel, gbc, fila++, "URL imagen:", txtUrlImagen);
        agregarCampo(panel, gbc, fila++, "Precio (S/):", txtPrecio);
        agregarCampo(panel, gbc, fila++, "Stock comercial inicial:", spinnerStockComercial);
        agregarCampo(panel, gbc, fila++, "Stock mínimo:", spinnerStockMinimo);

        gbc.gridy = fila++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(checkAplicaTripleImpacto, gbc);

        gbc.gridy = fila++;
        panel.add(new JLabel("Tipo de compromiso:"), gbc);
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        panel.add(comboTipoCompromiso, gbc);

        gbc.gridy = fila;
        panel.add(checkVisibleWeb, gbc);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
        panel.add(campo, gbc);
    }

    private JPanel construirBotones() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 8, 0));
        botonNuevo.addActionListener(e -> limpiarFormulario());
        botonGuardar.addActionListener(e -> guardar());
        botonEliminar.addActionListener(e -> eliminar());
        botonAjustarStock.addActionListener(e -> mostrarDialogoAjusteStock());
        panel.add(botonNuevo);
        panel.add(botonGuardar);
        panel.add(botonEliminar);
        panel.add(botonAjustarStock);
        return panel;
    }

    private void aplicarControlAcceso() {
        if (esAdministrador) {
            return;
        }
        botonNuevo.setEnabled(false);
        botonGuardar.setEnabled(false);
        botonEliminar.setEnabled(false);
        botonAjustarStock.setEnabled(false);
    }

    private void cargarProductos() {
        try {
            modeloTabla.actualizar(controlador.listarProductos());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarFormulario(Producto p) {
        codigoEnEdicion = p.getCodigo();
        txtCodigo.setText(p.getCodigo());
        txtCodigo.setEditable(false);
        txtNombre.setText(p.getNombre());
        txtMarca.setText(nvl(p.getMarca()));
        txtCategoria.setText(p.getCategoria());
        txtColeccion.setText(nvl(p.getColeccion()));
        txtTalla.setText(nvl(p.getTalla()));
        txtDescripcion.setText(nvl(p.getDescripcion()));
        txtUrlImagen.setText(nvl(p.getUrlImagen()));
        txtPrecio.setText(p.getPrecio().toPlainString());
        spinnerStockComercial.setValue(p.getStockComercial());
        spinnerStockComercial.setEnabled(false);
        spinnerStockMinimo.setValue(p.getStockMinimo());
        checkAplicaTripleImpacto.setSelected(p.isAplicaTripleImpacto());
        comboTipoCompromiso.setSelectedItem(p.getTipoCompromiso());
        checkVisibleWeb.setSelected(p.isVisibleWeb());
    }

    private void limpiarFormulario() {
        codigoEnEdicion = null;
        txtCodigo.setText("");
        txtCodigo.setEditable(esAdministrador);
        txtNombre.setText("");
        txtMarca.setText("");
        txtCategoria.setText("");
        txtColeccion.setText("");
        txtTalla.setText("");
        txtDescripcion.setText("");
        txtUrlImagen.setText("");
        txtPrecio.setText("");
        spinnerStockComercial.setValue(0);
        spinnerStockComercial.setEnabled(esAdministrador);
        spinnerStockMinimo.setValue(5);
        checkAplicaTripleImpacto.setSelected(true);
        comboTipoCompromiso.setSelectedItem(null);
        checkVisibleWeb.setSelected(true);
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            Producto producto = leerFormulario();
            if (codigoEnEdicion == null) {
                controlador.crearProducto(producto);
            } else {
                producto.setCodigo(codigoEnEdicion);
                controlador.actualizarProducto(producto);
            }
            cargarProductos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Producto guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Producto leerFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String categoria = txtCategoria.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || categoria.isEmpty()) {
            throw new ServicioException("Código, nombre y categoría son obligatorios.");
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            throw new ServicioException("El precio debe ser un número válido.");
        }
        if (precio.signum() < 0) {
            throw new ServicioException("El precio no puede ser negativo.");
        }

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setMarca(vacioComoNulo(txtMarca.getText()));
        producto.setCategoria(categoria);
        producto.setColeccion(vacioComoNulo(txtColeccion.getText()));
        producto.setTalla(vacioComoNulo(txtTalla.getText()));
        producto.setDescripcion(vacioComoNulo(txtDescripcion.getText()));
        producto.setUrlImagen(vacioComoNulo(txtUrlImagen.getText()));
        producto.setPrecio(precio);
        producto.setStockComercial(((Number) spinnerStockComercial.getValue()).intValue());
        producto.setStockMinimo(((Number) spinnerStockMinimo.getValue()).intValue());
        producto.setAplicaTripleImpacto(checkAplicaTripleImpacto.isSelected());
        producto.setTipoCompromiso((TipoCompromiso) comboTipoCompromiso.getSelectedItem());
        producto.setVisibleWeb(checkVisibleWeb.isSelected());
        producto.setActivo(true);
        return producto;
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Producto producto = modeloTabla.obtener(tabla.convertRowIndexToModel(fila));
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja \"" + producto.getNombre() + "\"? Dejará de estar visible en el catálogo.",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.desactivarProducto(producto.getCodigo());
            cargarProductos();
            limpiarFormulario();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoAjusteStock() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Producto producto = modeloTabla.obtener(tabla.convertRowIndexToModel(fila));

        JComboBox<TipoStock> comboTipo = new JComboBox<>(TipoStock.values());
        JSpinner spinnerCantidad = new JSpinner(new SpinnerNumberModel(0, -100_000, 100_000, 1));
        JTextField txtMotivo = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Producto: " + producto.getNombre()));
        panel.add(new JLabel("Tipo de stock:"));
        panel.add(comboTipo);
        panel.add(new JLabel("Cantidad (negativo para descontar):"));
        panel.add(spinnerCantidad);
        panel.add(new JLabel("Motivo:"));
        panel.add(txtMotivo);

        int resultado = JOptionPane.showConfirmDialog(this, panel, "Ajuste manual de stock",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        int delta = ((Number) spinnerCantidad.getValue()).intValue();
        String motivo = txtMotivo.getText().trim();
        if (delta == 0 || motivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Indica una cantidad distinta de cero y un motivo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controlador.ajustarStock(producto.getCodigo(), (TipoStock) comboTipo.getSelectedItem(), delta, motivo);
            cargarProductos();
            JOptionPane.showMessageDialog(this, "Ajuste registrado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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

    private final class ModeloTablaProductos extends AbstractTableModel {
        private final String[] columnas = {
                "Código", "Nombre", "Categoría", "Talla", "Precio",
                "Stock Comercial", "Stock Comprometido (Ayuda Social)", "Stock Mínimo"
        };

        private List<Producto> productos = List.of();

        void actualizar(List<Producto> nuevos) {
            this.productos = nuevos;
            fireTableDataChanged();
        }

        Producto obtener(int fila) {
            return productos.get(fila);
        }

        @Override
        public int getRowCount() {
            return productos.size();
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
            Producto p = productos.get(fila);
            return switch (columna) {
                case 0 -> p.getCodigo();
                case 1 -> p.getNombre();
                case 2 -> p.getCategoria();
                case 3 -> p.getTalla();
                case 4 -> p.getPrecio();
                case 5 -> p.getStockComercial();
                case 6 -> p.getStockComprometido();
                case 7 -> p.getStockMinimo();
                default -> null;
            };
        }
    }

    private final class ResaltadoStockBajoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component componente = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Producto producto = modeloTabla.obtener(table.convertRowIndexToModel(row));
                componente.setBackground(
                        producto.getStockComercial() <= producto.getStockMinimo() ? new Color(0xFF, 0xEB, 0xEE) : Color.WHITE);
            }
            return componente;
        }
    }
}
