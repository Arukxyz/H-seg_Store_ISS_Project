package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.UsuariosControlador;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.ServicioException;

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

    public UsuariosJFrame() {
        super("SEGITD-HÖSÉG · Gestión de usuarios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(760, 520));
        pack();
        setLocationRelativeTo(null);
        cargarUsuarios();
        limpiarFormulario();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirPanelTabla(), construirPanelFormulario());
        split.setResizeWeight(0.6);
        raiz.add(split, BorderLayout.CENTER);
        return raiz;
    }

    private JPanel construirPanelTabla() {
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() >= 0) {
                cargarFormulario(modeloUsuarios.obtener(tablaUsuarios.convertRowIndexToModel(tablaUsuarios.getSelectedRow())));
            }
        });
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
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

        return panel;
    }

    private JPanel construirBotones() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
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
}
