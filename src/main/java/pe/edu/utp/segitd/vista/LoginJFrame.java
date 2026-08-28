package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.AuthService;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

/**
 * Pantalla de autenticación (RF-01 / RNF-02). Valida contra Supabase con
 * SHA-256 + salt y aplica el bloqueo temporal tras 3 intentos fallidos
 * (ver AuthService).
 */
public class LoginJFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private final JTextField campoUsuario = new JTextField(18);
    private final JPasswordField campoPassword = new JPasswordField(18);
    private final JLabel etiquetaMensaje = new JLabel(" ");
    private final JButton botonIngresar = new JButton("Ingresar");

    public LoginJFrame() {
        super("SEGITD-HÖSÉG · Ingreso");
        construirInterfaz();
        botonIngresar.addActionListener(e -> intentarIngresar());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Höség Store — Back office");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(campoUsuario, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panel.add(campoPassword, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        etiquetaMensaje.setForeground(new Color(0xB0, 0x00, 0x20));
        panel.add(etiquetaMensaje, gbc);

        gbc.gridy = 4;
        panel.add(botonIngresar, gbc);

        getRootPane().setDefaultButton(botonIngresar);
        setContentPane(panel);
    }

    private void intentarIngresar() {
        String usuario = campoUsuario.getText().trim();
        char[] password = campoPassword.getPassword();

        if (usuario.isEmpty() || password.length == 0) {
            mostrarMensaje("Ingresa usuario y contraseña.");
            return;
        }

        botonIngresar.setEnabled(false);
        try {
            Usuario autenticado = authService.autenticar(usuario, password);
            SesionUsuario.obtenerInstancia().iniciarSesion(autenticado);
            abrirMenuPrincipal();
        } catch (ServicioException ex) {
            mostrarMensaje(ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            botonIngresar.setEnabled(true);
        }
    }

    private void abrirMenuPrincipal() {
        new MenuPrincipalJFrame().setVisible(true);
        dispose();
    }

    private void mostrarMensaje(String mensaje) {
        etiquetaMensaje.setText(mensaje);
    }
}
