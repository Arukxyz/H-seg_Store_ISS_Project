package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.AuthService;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;

<<<<<<< HEAD
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
=======
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
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
import java.util.Arrays;

/**
 * Pantalla de autenticación (RF-01 / RNF-02). Valida contra Supabase con
 * SHA-256 + salt y aplica el bloqueo temporal tras 3 intentos fallidos
 * (ver AuthService).
 */
public class LoginJFrame extends JFrame {

    private final AuthService authService = new AuthService();

<<<<<<< HEAD
    private final JTextField campoUsuario = new JTextField(15);
    private final JPasswordField campoPassword = new JPasswordField(15);
    private final JLabel etiquetaMensaje = new JLabel(" ");
    private final JButton botonIngresar = new JButton("Ingresar");

    // PALETA DE COLORES HÖSÉG
    private final Color COLOR_FONDO_IZQ = new Color(0x2D, 0x3A, 0x33);  // Verde Sea Pine
    private final Color COLOR_FONDO_DER = new Color(0xF7, 0xF5, 0xF0);  // Beige Claro Neutro
    private final Color COLOR_TEXTO_PRINCIPAL = new Color(0x1A, 0x1A, 0x1A); // Negro Suave
    private final Color COLOR_TEXTO_MUTED = new Color(0x75, 0x75, 0x75);     // Gris sutil
    private final Color COLOR_BOTON = new Color(0x8C, 0x2D, 0x19);     // Burdeo/Terracota Höség
    private final Color COLOR_BOTON_HOVER = new Color(0xA6, 0x3A, 0x24); // Burdeo más claro 
    private final Color COLOR_ERROR = new Color(0xB0, 0x00, 0x20);     // Rojo sutil de error

    // FUENTES ELEGANTES
    private final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 22);
    private final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 12);
    private final Font FUENTE_LABELS = new Font("SansSerif", Font.BOLD, 12);
    private final Font FUENTE_CAMPOS = new Font("SansSerif", Font.PLAIN, 14);



=======
    private final JTextField campoUsuario = new JTextField(18);
    private final JPasswordField campoPassword = new JPasswordField(18);
    private final JLabel etiquetaMensaje = new JLabel(" ");
    private final JButton botonIngresar = new JButton("Ingresar");

>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
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
<<<<<<< HEAD

        //panel principal
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2));
        panelPrincipal.setPreferredSize(new Dimension(680, 400)); 

        //panel izquierdo
        //nose si la ruta les falle a ustedes:'v
        JPanel panelIzquierdo = new PanelFondoImagen("/images/login-banner.jpg");
        panelIzquierdo.setBorder(new EmptyBorder(40, 30, 40, 30));

        //logo
        JLabel labelLogo = new JLabel("🔥"); 
        labelLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        labelLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelLogo.setForeground(COLOR_FONDO_DER);

        JLabel txtHoseg = new JLabel("HÖSÉG");
        txtHoseg.setFont(new Font("SansSerif", Font.BOLD, 28));
        txtHoseg.setForeground(Color.WHITE);
        txtHoseg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txtEslogan = new JLabel("Sostenibilidad & Aventura");
        txtEslogan.setFont(FUENTE_SUBTITULO);
        txtEslogan.setForeground(new Color(0xCE, 0xD4, 0xDA));
        txtEslogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelIzquierdo.add(Box.createVerticalGlue());
        panelIzquierdo.add(labelLogo);
        panelIzquierdo.add(Box.createVerticalStrut(15));
        panelIzquierdo.add(txtHoseg);
        panelIzquierdo.add(Box.createVerticalStrut(5));
        panelIzquierdo.add(txtEslogan);
        panelIzquierdo.add(Box.createVerticalGlue());

        //panel derecho
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setBackground(COLOR_FONDO_DER);
        panelDerecho.setBorder(new EmptyBorder(30, 40, 30, 40));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        //titulo
        JLabel titulo = new JLabel("Back office");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(COLOR_TEXTO_PRINCIPAL);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelDerecho.add(titulo, gbc);

        //subtitulo
         JLabel subtitulo = new JLabel("Ingresa tus credenciales para continuar");
        subtitulo.setFont(FUENTE_SUBTITULO);
        subtitulo.setForeground(COLOR_TEXTO_MUTED);
        gbc.gridy = 1;
        panelDerecho.add(subtitulo, gbc);

        //espaciador
         gbc.gridy = 2;
        panelDerecho.add(Box.createVerticalStrut(10), gbc);

        //usuario
        JLabel lblUsuario = new JLabel("USUARIO");
        lblUsuario.setFont(FUENTE_LABELS);
        lblUsuario.setForeground(COLOR_TEXTO_MUTED);
        gbc.gridy = 3;
        panelDerecho.add(lblUsuario, gbc);

        estilizarCampoTexto(campoUsuario);
        gbc.gridy = 4;
        panelDerecho.add(campoUsuario, gbc);

        //contraseña
        JLabel lblPassword = new JLabel("CONTRASEÑA");
        lblPassword.setFont(FUENTE_LABELS);
        lblPassword.setForeground(COLOR_TEXTO_MUTED);
        gbc.gridy = 5;
        panelDerecho.add(lblPassword, gbc);

        estilizarCampoTexto(campoPassword);
        gbc.gridy = 6;
        panelDerecho.add(campoPassword, gbc);

        //mensaje
        etiquetaMensaje.setFont(new Font("SansSerif", Font.PLAIN, 11));
        etiquetaMensaje.setForeground(COLOR_ERROR);
        gbc.gridy = 7;
        panelDerecho.add(etiquetaMensaje, gbc);

        //boton ingresar
        botonIngresar.setFont(new Font("SansSerif", Font.BOLD, 14));
        botonIngresar.setBackground(COLOR_BOTON);
        botonIngresar.setForeground(Color.WHITE);
        botonIngresar.setFocusPainted(false);
        botonIngresar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        botonIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonIngresar.setHorizontalAlignment(SwingConstants.CENTER);

        //simulador
         botonIngresar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if(botonIngresar.isEnabled()) botonIngresar.setBackground(COLOR_BOTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(botonIngresar.isEnabled()) botonIngresar.setBackground(COLOR_BOTON);
            }
        });

        gbc.gridy = 8;
        panelDerecho.add(botonIngresar, gbc);

        //panel
        panelPrincipal.add(panelIzquierdo);
        panelPrincipal.add(panelDerecho);

        getRootPane().setDefaultButton(botonIngresar);
        setContentPane(panelPrincipal);
    }

     private void estilizarCampoTexto(JTextField campo) {
        campo.setFont(FUENTE_CAMPOS);
        campo.setBackground(Color.WHITE);
        campo.setForeground(COLOR_TEXTO_PRINCIPAL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }


=======
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

>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
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
<<<<<<< HEAD


    //Clase para la imagen
        // Panel avanzado que escala la imagen y dibuja la franja multicolor de Höség
    private static class PanelFondoImagen extends JPanel {
        private Image imagen;

        public PanelFondoImagen(String rutaIcono) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            try {
                java.net.URL url = getClass().getResource(rutaIcono);
                if (url != null) {
                    this.imagen = new ImageIcon(url).getImage();
                }
            } catch (Exception e) {
                System.out.println("No se pudo cargar la imagen, usando color plano.");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Activar calidad alta para los gráficos
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (imagen != null) {
                // 1. Dibujar la imagen estirada
                g2d.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
                
                // 2. Capa oscura elegante para legibilidad del texto blanco
                g2d.setColor(new Color(0, 0, 0, 140)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // Fondo verde oscuro de respaldo si no hay imagen
                g2d.setColor(new Color(0x2D, 0x3A, 0x33));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            // 3. LA FRANJA MULTICOLOR (Dibujada en la parte inferior del panel izquierdo)
            int altoFranja = 6; // Grosor de la línea en píxeles
            int yFranja = getHeight() - altoFranja; // Posición abajo del todo

            // Degradado lineal horizontal: va desde Azul -> Morado -> Rosa -> Rojo (como tu foto)
            LinearGradientPaint degradadoHoseg = new LinearGradientPaint(
                0, yFranja, getWidth(), yFranja,
                new float[]{0.0f, 0.35f, 0.70f, 1.0f}, // Puntos de transición
                new Color[]{
                    new Color(0x00, 0x33, 0xAA), // Azul
                    new Color(0x6A, 0x1B, 0x9A), // Morado
                    new Color(0xD8, 0x1B, 0x60), // Rosa / Magenta
                    new Color(0xD3, 0x2F, 0x2F)  // Rojo
                }
            );

            g2d.setPaint(degradadoHoseg);
            g2d.fillRect(0, yFranja, getWidth(), altoFranja);
        }
    }

}

=======
}
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
