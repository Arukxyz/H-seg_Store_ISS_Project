package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.UsuariosControlador;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.ServicioException;

<<<<<<< HEAD
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
=======
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
import java.util.Arrays;
import java.util.List;

/**
 * Registro y administración de usuarios internos (RF-08, sección 8
 * pantalla 8). Solo ADMINISTRADOR — ya viene deshabilitado en el menú
 * para ENCARGADO.
 */
public class UsuariosJFrame extends JFrame {

    private final UsuariosControlador controlador = new UsuariosControlador();

    private final ModeloUsuarios modeloUsuarios = new ModeloUsuarios();
    private final JTable tablaUsuarios = new JTable(modeloUsuarios);

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtUsername = new JTextField(16);
    private final JPasswordField txtPassword = new JPasswordField(16);
    private final JComboBox<RolUsuario> comboRol = new JComboBox<>(RolUsuario.values());

    private final JButton botonNuevo = new JButton("Nuevo");
    private final JButton botonGuardar = new JButton("Guardar");
    private final JButton botonDesactivar = new JButton("Desactivar");
    private final JButton botonResetearPassword = new JButton("Resetear contraseña");

    private Integer idEnEdicion;

<<<<<<< HEAD
    // COLORES (mismo sistema que GestionProductosJFrame)
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3);
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);

    private final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private final Font FUENTE_INPUT = new Font("SansSerif", Font.PLAIN, 13);

    public UsuariosJFrame() {
        super("SEGITD-HÖSÉG · Gestión de usuarios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);
        setMinimumSize(new Dimension(880, 540));
=======
    public UsuariosJFrame() {
        super("SEGITD-HÖSÉG · Gestión de usuarios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(760, 520));
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        pack();
        setLocationRelativeTo(null);
        cargarUsuarios();
        limpiarFormulario();
    }

    private JPanel construirContenido() {
<<<<<<< HEAD
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
        panelTop.setBackground(COLOR_FONDO_VENTANA);

        JLabel titulo = new JLabel("Gestión de usuarios internos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(COLOR_TEXTO_MAIN);
        panelTop.add(titulo);
        panelTop.add(Box.createVerticalStrut(10));
        panelTop.add(new FranjaDecorativaHoseg());

        raiz.add(panelTop, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirPanelTabla(), construirPanelFormulario());
        split.setResizeWeight(0.6);
        split.setBorder(null);
        split.setBackground(COLOR_FONDO_VENTANA);
        if (split.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
            ((javax.swing.plaf.basic.BasicSplitPaneUI) split.getUI()).getDivider().setBorder(null);
        }

=======
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirPanelTabla(), construirPanelFormulario());
        split.setResizeWeight(0.6);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        raiz.add(split, BorderLayout.CENTER);
        return raiz;
    }

    private JPanel construirPanelTabla() {
<<<<<<< HEAD
        tablaUsuarios.setRowHeight(28);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        tablaUsuarios.setSelectionForeground(COLOR_TEXTO_MAIN);
        tablaUsuarios.setShowVerticalLines(false);
        tablaUsuarios.setGridColor(new Color(0xE2, 0xE2, 0xE0));

        JTableHeader header = tablaUsuarios.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

=======
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() >= 0) {
                cargarFormulario(modeloUsuarios.obtener(tablaUsuarios.convertRowIndexToModel(tablaUsuarios.getSelectedRow())));
            }
        });
<<<<<<< HEAD

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setBorder(new LineBorder(new Color(0xE2, 0xE2, 0xE0), 1));
        panel.add(scrollPane, BorderLayout.CENTER);
=======
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
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

=======
        JPanel panel = new JPanel(new BorderLayout(0, 12));
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        panel.add(construirFormulario(), BorderLayout.CENTER);
        panel.add(construirBotones(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
<<<<<<< HEAD
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        agregarCampo(panel, gbc, 0, "Nombre:", txtNombre);
        agregarCampo(panel, gbc, 1, "Usuario:", txtUsername);
        agregarCampo(panel, gbc, 2, "Contraseña inicial:", txtPassword);
        agregarCampo(panel, gbc, 3, "Rol:", comboRol);
=======
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        panel.add(txtNombre, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Contraseña inicial:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1;
        panel.add(comboRol, gbc);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987

        return panel;
    }

<<<<<<< HEAD
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

        if (campo instanceof JTextField) {
            estilizarComponenteForm((JTextField) campo);
        } else if (campo instanceof JComboBox) {
            campo.setFont(FUENTE_INPUT);
            campo.setBackground(Color.WHITE);
            campo.setBorder(BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1));
        }
        panel.add(campo, gbc);
    }

    private JPanel construirBotones() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBackground(Color.WHITE);

        estilizarBotonSecundario(botonNuevo, COLOR_PRIMARIO);
        estilizarBotonPrincipal(botonGuardar, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        estilizarBotonSecundario(botonDesactivar, COLOR_BURDEO);
        estilizarBotonSecundario(botonResetearPassword, COLOR_PRIMARIO);

=======
    private JPanel construirBotones() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        botonNuevo.addActionListener(e -> limpiarFormulario());
        botonGuardar.addActionListener(e -> guardar());
        botonDesactivar.addActionListener(e -> desactivar());
        botonResetearPassword.addActionListener(e -> resetearPassword());
        panel.add(botonNuevo);
        panel.add(botonGuardar);
        panel.add(botonDesactivar);
        panel.add(botonResetearPassword);
        return panel;
    }

    private void cargarUsuarios() {
        try {
            modeloUsuarios.actualizar(controlador.listarUsuarios());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarFormulario(Usuario u) {
        idEnEdicion = u.getId();
        txtNombre.setText(u.getNombre());
        txtUsername.setText(u.getUsername());
        txtUsername.setEditable(false);
        txtPassword.setText("");
        txtPassword.setEnabled(false);
        comboRol.setSelectedItem(u.getRol());
        botonDesactivar.setEnabled(u.getId() != controlador.idUsuarioActual());
        botonResetearPassword.setEnabled(true);
    }

    private void limpiarFormulario() {
        idEnEdicion = null;
        txtNombre.setText("");
        txtUsername.setText("");
        txtUsername.setEditable(true);
        txtPassword.setText("");
        txtPassword.setEnabled(true);
        comboRol.setSelectedItem(RolUsuario.ENCARGADO);
        botonDesactivar.setEnabled(false);
        botonResetearPassword.setEnabled(false);
        tablaUsuarios.clearSelection();
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        RolUsuario rol = (RolUsuario) comboRol.getSelectedItem();

        try {
            if (idEnEdicion == null) {
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword());
                controlador.crearUsuario(nombre, username, password, rol);
            } else {
                controlador.actualizarUsuario(idEnEdicion, nombre, rol);
            }
            cargarUsuarios();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Usuario guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            char[] password = txtPassword.getPassword();
            Arrays.fill(password, '\0');
        }
    }

    private void desactivar() {
        if (idEnEdicion == null) {
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Desactivar este usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.desactivarUsuario(idEnEdicion);
            cargarUsuarios();
            limpiarFormulario();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetearPassword() {
        if (idEnEdicion == null) {
            return;
        }
        JPasswordField campoNueva = new JPasswordField(16);
<<<<<<< HEAD
        estilizarComponenteForm(campoNueva);
=======
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        int resultado = JOptionPane.showConfirmDialog(this, campoNueva, "Nueva contraseña",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }
        char[] nueva = campoNueva.getPassword();
        try {
            controlador.resetearPassword(idEnEdicion, new String(nueva));
            JOptionPane.showMessageDialog(this, "Contraseña restablecida.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(nueva, '\0');
        }
    }

<<<<<<< HEAD
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
    private static final class ModeloUsuarios extends AbstractTableModel {
        private final String[] columnas = {"Nombre", "Usuario", "Rol", "Activo"};
        private List<Usuario> usuarios = List.of();

        void actualizar(List<Usuario> nuevos) {
            this.usuarios = nuevos;
            fireTableDataChanged();
        }

        Usuario obtener(int fila) {
            return usuarios.get(fila);
        }

        @Override
        public int getRowCount() {
            return usuarios.size();
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
            Usuario u = usuarios.get(fila);
            return switch (columna) {
                case 0 -> u.getNombre();
                case 1 -> u.getUsername();
                case 2 -> u.getRol();
                case 3 -> u.isActivo() ? "Sí" : "No";
                default -> null;
            };
        }
    }
<<<<<<< HEAD

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
=======
}
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
