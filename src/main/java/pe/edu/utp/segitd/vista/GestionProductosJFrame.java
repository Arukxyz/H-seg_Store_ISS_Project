package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.GestionProductosControlador;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.TipoCompromiso;
import pe.edu.utp.segitd.modelo.TipoStock;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;

<<<<<<< HEAD
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

=======
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
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
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

<<<<<<< HEAD
     // COLORES
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3); // Crema suave
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);      // Verde Sea Pine
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);       // Terracota
    private final Color COLOR_BURDEO_HOVER = new Color(0xA6, 0x3A, 0x24);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);
    private final Color COLOR_ALERTA_PASTEL = new Color(0xFF, 0xEB, 0xEE);  // Rojo sutil

    private final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private final Font FUENTE_INPUT = new Font("SansSerif", Font.PLAIN, 13);

    public GestionProductosJFrame() {
        super("SEGITD-HÖSÉG · Gestión de productos");  
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);

        setMinimumSize(new Dimension(1050, 500));
=======
    public GestionProductosJFrame() {
        super("SEGITD-HÖSÉG · Gestión de productos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(920, 560));
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        aplicarControlAcceso();
        pack();
        setLocationRelativeTo(null);
        cargarProductos();
        limpiarFormulario();
    }

    private JPanel construirContenido() {
<<<<<<< HEAD
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);

        //encabezado
        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
        panelTop.setBackground(COLOR_FONDO_VENTANA);

        JLabel lblTituloVentana = new JLabel("Catálogo y Gestión de Productos");
        lblTituloVentana.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTituloVentana.setForeground(COLOR_TEXTO_MAIN);
        panelTop.add(lblTituloVentana);
        panelTop.add(Box.createVerticalStrut(10));
        panelTop.add(new FranjaDecorativaHoseg());

        raiz.add(panelTop, BorderLayout.NORTH);

        //split
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirPanelTabla(), construirPanelFormulario());
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        splitPane.setBackground(COLOR_FONDO_VENTANA);
        
        if (splitPane.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
            ((javax.swing.plaf.basic.BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(null);
        }

=======
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirPanelTabla(), construirPanelFormulario());
        splitPane.setResizeWeight(0.6);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        raiz.add(splitPane, BorderLayout.CENTER);
        return raiz;
    }

    private JPanel construirPanelTabla() {
        tabla.setDefaultRenderer(Object.class, new ResaltadoStockBajoRenderer());
<<<<<<< HEAD
        tabla.setRowHeight(28);
        //aqui afecta¿
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        tabla.setSelectionForeground(COLOR_TEXTO_MAIN);
        tabla.setShowVerticalLines(false); 
        tabla.setGridColor(new Color(0xE2, 0xE2, 0xE0));
       
       
        //cabecera tabla
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

=======
        tabla.setRowHeight(22);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                cargarFormulario(modeloTabla.obtener(tabla.convertRowIndexToModel(tabla.getSelectedRow())));
            }
        });

<<<<<<< HEAD
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(new LineBorder(new Color(0xE2, 0xE2, 0xE0), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel leyenda = new JLabel("● Las filas en fondo rojizo tienen el stock comercial en el mínimo o por debajo.");
        leyenda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        leyenda.setForeground(COLOR_BURDEO);
        panel.add(leyenda, BorderLayout.SOUTH);

=======
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JLabel leyenda = new JLabel("Las filas en rojo tienen el stock comercial en el mínimo o por debajo.");
        leyenda.setForeground(Color.GRAY);
        panel.add(leyenda, BorderLayout.SOUTH);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        return panel;
    }

    private JPanel construirPanelFormulario() {
<<<<<<< HEAD
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);  
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2, 0xE2, 0xE0), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        
        
        JScrollPane scrollForm = new JScrollPane(construirFormulario());
        scrollForm.setBorder(null);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollForm, BorderLayout.CENTER);
=======
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(construirFormulario(), BorderLayout.CENTER);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        panel.add(construirBotones(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
<<<<<<< HEAD
        panel.setBackground(Color.WHITE); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
=======
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987

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

<<<<<<< HEAD
        checkAplicaTripleImpacto.setFont(FUENTE_LABEL);
        checkAplicaTripleImpacto.setBackground(Color.WHITE);
        checkAplicaTripleImpacto.setForeground(COLOR_TEXTO_MAIN);
        gbc.gridy = fila++; 
        gbc.gridx = 0; 
        gbc.gridwidth = 2;
        panel.add(checkAplicaTripleImpacto, gbc);

        JLabel lblCompromiso = new JLabel("Tipo de compromiso:");
        lblCompromiso.setFont(FUENTE_LABEL);
        lblCompromiso.setForeground(COLOR_GRIS_TEXTO);
        gbc.gridy = fila++;
        panel.add(lblCompromiso, gbc);

        comboTipoCompromiso.setFont(FUENTE_INPUT);
        comboTipoCompromiso.setBackground(Color.WHITE);

        comboTipoCompromiso.setBorder(BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1));
=======
        gbc.gridy = fila++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(checkAplicaTripleImpacto, gbc);

        gbc.gridy = fila++;
        panel.add(new JLabel("Tipo de compromiso:"), gbc);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        panel.add(comboTipoCompromiso, gbc);

<<<<<<< HEAD
        checkVisibleWeb.setFont(FUENTE_LABEL);
        checkVisibleWeb.setBackground(Color.WHITE);
        checkVisibleWeb.setForeground(COLOR_TEXTO_MAIN);
=======
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        gbc.gridy = fila;
        panel.add(checkVisibleWeb, gbc);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
<<<<<<< HEAD
        gbc.weightx = 0.3; 

        JLabel label = new JLabel(etiqueta);
        label.setFont(FUENTE_LABEL);
        label.setForeground(COLOR_GRIS_TEXTO);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        if (campo instanceof JTextField) {
            estilizarComponenteForm((JTextField) campo);
        } else if (campo instanceof JSpinner) {
            estilizarComponenteForm((JComponent) ((JSpinner) campo).getEditor());
            campo.setBackground(Color.WHITE);
        }
=======
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        panel.add(campo, gbc);
    }

    private JPanel construirBotones() {
<<<<<<< HEAD
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(Color.WHITE);
        
        estilizarBotonSecundario(botonNuevo, COLOR_PRIMARIO);
        estilizarBotonPrincipal(botonGuardar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        estilizarBotonSecundario(botonEliminar, COLOR_BURDEO); 
        estilizarBotonSecundario(botonAjustarStock, COLOR_PRIMARIO);

=======
        JPanel panel = new JPanel(new GridLayout(1, 4, 8, 0));
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        botonNuevo.addActionListener(e -> limpiarFormulario());
        botonGuardar.addActionListener(e -> guardar());
        botonEliminar.addActionListener(e -> eliminar());
        botonAjustarStock.addActionListener(e -> mostrarDialogoAjusteStock());
<<<<<<< HEAD
        
=======
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
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
<<<<<<< HEAD

        Color grisDeshabilitado = new Color(0xE0, 0xE0, 0xE0);
        Color textoDeshabilitado = new Color(0x9E, 0x9E, 0x9E);

        JButton[] botones = {botonNuevo, botonGuardar, botonEliminar, botonAjustarStock};
        for (JButton b : botones) {
            b.setEnabled(false);
            b.setBackground(grisDeshabilitado);
            b.setForeground(textoDeshabilitado);
            b.setBorder(BorderFactory.createLineBorder(new Color(0xBD, 0xBD, 0xBD), 1));
        }
=======
        botonNuevo.setEnabled(false);
        botonGuardar.setEnabled(false);
        botonEliminar.setEnabled(false);
        botonAjustarStock.setEnabled(false);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
    }

    private void cargarProductos() {
        try {
            modeloTabla.actualizar(controlador.listarProductos());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

<<<<<<< HEAD
    //metodos estilos
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

=======
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
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

<<<<<<< HEAD
        comboTipo.setFont(FUENTE_INPUT);
        comboTipo.setBackground(Color.WHITE);
        comboTipo.setBorder(BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1));

        spinnerCantidad.setFont(FUENTE_INPUT);
        estilizarComponenteForm((JComponent) spinnerCantidad.getEditor());
        spinnerCantidad.setBackground(Color.WHITE);

        estilizarComponenteForm(txtMotivo);
        
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBackground(COLOR_FONDO_VENTANA);
        
        JLabel lblProd = new JLabel("Producto: " + producto.getNombre());
        lblProd.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblProd.setForeground(COLOR_TEXTO_MAIN);
        panel.add(lblProd);

      String[] etiquetas = {"Tipo de stock:", "Cantidad (negativo para descontar):", "Motivo:"};
        JComponent[] componentes = {comboTipo, spinnerCantidad, txtMotivo};

        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(FUENTE_LABEL);
            lbl.setForeground(COLOR_GRIS_TEXTO);
            panel.add(lbl);
            panel.add(componentes[i]);
        }
=======
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Producto: " + producto.getNombre()));
        panel.add(new JLabel("Tipo de stock:"));
        panel.add(comboTipo);
        panel.add(new JLabel("Cantidad (negativo para descontar):"));
        panel.add(spinnerCantidad);
        panel.add(new JLabel("Motivo:"));
        panel.add(txtMotivo);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987

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

<<<<<<< HEAD
    //
=======
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
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
<<<<<<< HEAD
            
            Producto producto = modeloTabla.obtener(table.convertRowIndexToModel(row));
            boolean esBajoStock = producto.getStockComercial() <= producto.getStockMinimo();

            if (isSelected) {
                componente.setBackground(new Color(0xE2, 0xE8, 0xF0));
                componente.setForeground(COLOR_TEXTO_MAIN);
                
                if (esBajoStock) {
                    componente.setForeground(COLOR_BURDEO);
                }
            } else {
                componente.setBackground(esBajoStock ? COLOR_ALERTA_PASTEL : Color.WHITE);
                componente.setForeground(COLOR_TEXTO_MAIN);
            }
            
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            
            return componente;
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
            LinearGradientPaint degradado = new LinearGradientPaint(
                0, 0, getWidth(), 0,
                new float[]{0.0f, 0.35f, 0.70f, 1.0f},
                new Color[]{
                    new Color(0x00, 0x33, 0xAA), // Azul
                    new Color(0x6A, 0x1B, 0x9A), // Morado
                    new Color(0xD8, 0x1B, 0x60), // Rosa
                    new Color(0xD3, 0x2F, 0x2F)  // Rojo
                }
            );
            g2d.setPaint(degradado);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }


=======
            if (!isSelected) {
                Producto producto = modeloTabla.obtener(table.convertRowIndexToModel(row));
                componente.setBackground(
                        producto.getStockComercial() <= producto.getStockMinimo() ? new Color(0xFF, 0xEB, 0xEE) : Color.WHITE);
            }
            return componente;
        }
    }
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
}
