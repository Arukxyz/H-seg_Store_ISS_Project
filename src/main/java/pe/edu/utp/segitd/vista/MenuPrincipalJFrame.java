package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.MenuPrincipalControlador;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.BackupService;
import pe.edu.utp.segitd.servicio.IndicadoresDashboard;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;
<<<<<<< HEAD
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
=======

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

/**
 * Dashboard principal: indicadores en vivo y accesos a cada módulo,
 * habilitados según el rol de la sesión activa (sección 8, pantalla 2).
 */
public class MenuPrincipalJFrame extends JFrame {

    private final MenuPrincipalControlador controlador = new MenuPrincipalControlador();
    private final BackupService backupService = new BackupService();

    private final JLabel indicadorConexion = new JLabel();
    private final JLabel valorProductosActivos = new JLabel("-");
    private final JLabel valorBajoStock = new JLabel("-");
    private final JLabel valorPedidosPendientes = new JLabel("-");
    private final JLabel valorDonacionesPendientes = new JLabel("-");
    private final JLabel valorLotesEnRuta = new JLabel("-");

<<<<<<< HEAD
    // CONSTANTES DE DISEÑO 
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3); // Crema neutro muy suave
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);      // Verde Sea Pine 
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);       // Terracota 
    private final Color COLOR_BURDEO_HOVER = new Color(0xA6, 0x3A, 0x24);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);

    private final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    private final Font FUENTE_INDICADOR_NUM = new Font("SansSerif", Font.BOLD, 36);
    private final Font FUENTE_INDICADOR_TXT = new Font("SansSerif", Font.PLAIN, 12);
    private final Font FUENTE_BOTONES = new Font("SansSerif", Font.BOLD, 13);

      public MenuPrincipalJFrame() {
        super("SEGITD-HÖSÉG · Panel principal");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        //ventana contenedora
        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);
        
        setMinimumSize(new Dimension(880, 520)); 
        this.getContentPane().setBackground(COLOR_FONDO_VENTANA);
        pack();
        setLocationRelativeTo(null);
        actualizarPanel(); //
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion(); //
=======
    public MenuPrincipalJFrame() {
        super("SEGITD-HÖSÉG · Panel principal");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(760, 480));
        pack();
        setLocationRelativeTo(null);
        actualizarPanel();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion();
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
            }
        });
    }

    private JPanel construirContenido() {
<<<<<<< HEAD
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);
        
        JPanel contenedorNorte = new JPanel();
        contenedorNorte.setLayout(new BoxLayout(contenedorNorte, BoxLayout.Y_AXIS));
        contenedorNorte.setBackground(COLOR_FONDO_VENTANA);
        
        contenedorNorte.add(construirEncabezado());
        contenedorNorte.add(Box.createVerticalStrut(14));
        
        contenedorNorte.add(new FranjaDecorativaHoseg());
        
        raiz.add(contenedorNorte, BorderLayout.NORTH);
=======
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        raiz.add(construirEncabezado(), BorderLayout.NORTH);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        raiz.add(construirIndicadores(), BorderLayout.CENTER);
        raiz.add(construirModulos(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirEncabezado() {
        Usuario usuario = SesionUsuario.obtenerInstancia().getUsuarioActual();

        JPanel encabezado = new JPanel(new BorderLayout());
<<<<<<< HEAD
        encabezado.setBackground(COLOR_FONDO_VENTANA);

        //titulo izq
        JLabel titulo = new JLabel("Höség Store — Back office");
        titulo.setForeground(COLOR_TEXTO_MAIN);
        encabezado.add(titulo, BorderLayout.WEST);

        //contenedor der
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setBackground(COLOR_FONDO_VENTANA);

        //estado conexion
        indicadorConexion.setFont(new Font("SansSerif", Font.BOLD, 12));
        derecha.add(indicadorConexion);

        //usuario
        JLabel lblUser = new JLabel("👤 " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        lblUser.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblUser.setForeground(COLOR_GRIS_TEXTO);
        derecha.add(lblUser);
        
        //boton actualizar
        JButton botonActualizar = crearBotónHeader("Actualizar", COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonActualizar.addActionListener(e -> actualizarPanel()); //
        derecha.add(botonActualizar);

        //boton respaldar
        JButton botonRespaldar = crearBotónHeader("Respaldar ahora", COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonRespaldar.setEnabled(usuario.getRol() == RolUsuario.ADMINISTRADOR); //
        botonRespaldar.addActionListener(e -> respaldarAhora()); //
        derecha.add(botonRespaldar);
        
        //boton cerrar sesion
        JButton botonCerrarSesion = crearBotónHeader("Cerrar sesión", COLOR_BURDEO, COLOR_BURDEO_HOVER);
        botonCerrarSesion.addActionListener(e -> cerrarSesion()); //
        derecha.add(botonCerrarSesion);

        encabezado.add(derecha, BorderLayout.EAST);
=======

        JLabel titulo = new JLabel("Höség Store — Back office");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        encabezado.add(titulo, BorderLayout.WEST);

        JPanel derecha = new JPanel();
        derecha.add(indicadorConexion);
        derecha.add(new JLabel(usuario.getNombre() + " (" + usuario.getRol() + ")"));
        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(e -> actualizarPanel());
        derecha.add(botonActualizar);
        JButton botonRespaldar = new JButton("Respaldar ahora");
        botonRespaldar.setEnabled(usuario.getRol() == RolUsuario.ADMINISTRADOR);
        botonRespaldar.addActionListener(e -> respaldarAhora());
        derecha.add(botonRespaldar);
        JButton botonCerrarSesion = new JButton("Cerrar sesión");
        botonCerrarSesion.addActionListener(e -> cerrarSesion());
        derecha.add(botonCerrarSesion);
        encabezado.add(derecha, BorderLayout.EAST);

>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        return encabezado;
    }

    private JPanel construirIndicadores() {
<<<<<<< HEAD
        JPanel panel = new JPanel(new GridLayout(1, 5, 14, 14));
        panel.setBackground(COLOR_FONDO_VENTANA);
        
=======
        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 12));
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        panel.add(tarjeta("Productos activos", valorProductosActivos));
        panel.add(tarjeta("Bajo stock mínimo", valorBajoStock));
        panel.add(tarjeta("Pedidos web pendientes", valorPedidosPendientes));
        panel.add(tarjeta("Donaciones por asignar", valorDonacionesPendientes));
        panel.add(tarjeta("Lotes en ruta", valorLotesEnRuta));
        return panel;
    }

    private JPanel tarjeta(String etiqueta, JLabel valor) {
<<<<<<< HEAD
        JPanel tarjeta = new JPanel(new BorderLayout(8, 8));
        tarjeta.setBackground(Color.WHITE); 
        
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0xE5, 0xE5, 0xE3), 1),
                BorderFactory.createEmptyBorder(20, 10, 20, 10)));
        
        valor.setFont(FUENTE_INDICADOR_NUM);
        valor.setForeground(COLOR_PRIMARIO); 
        valor.setHorizontalAlignment(SwingConstants.CENTER); 

       JLabel descripcion = new JLabel("<html><center>" + etiqueta + "</center></html>", SwingConstants.CENTER);
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descripcion.setForeground(COLOR_GRIS_TEXTO);
        
=======
        JPanel tarjeta = new JPanel(new BorderLayout(4, 4));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 28f));
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel descripcion = new JLabel(etiqueta, SwingConstants.CENTER);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
        tarjeta.add(valor, BorderLayout.CENTER);
        tarjeta.add(descripcion, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel construirModulos() {
<<<<<<< HEAD
        JPanel panel = new JPanel(new GridLayout(2, 3, 14, 14));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
=======
        JPanel panel = new JPanel(new GridLayout(2, 3, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987

        Usuario usuario = SesionUsuario.obtenerInstancia().getUsuarioActual();
        boolean esAdministrador = usuario.getRol() == RolUsuario.ADMINISTRADOR;

        panel.add(botonModulo("Gestión de productos", true, () -> new GestionProductosJFrame().setVisible(true)));
        panel.add(botonModulo("Pedidos web", true, () -> new PedidosWebJFrame().setVisible(true)));
        panel.add(botonModulo("Despacho de lotes", true, () -> new DespachoLotesJFrame().setVisible(true)));
        panel.add(botonModulo("Reportes de impacto", esAdministrador, () -> new ReportesImpactoJFrame().setVisible(true)));
        panel.add(botonModulo("Pedidos a proveedores", esAdministrador, () -> new ProveedoresJFrame().setVisible(true)));
        panel.add(botonModulo("Gestión de usuarios", esAdministrador, () -> new UsuariosJFrame().setVisible(true)));

        return panel;
    }

    private JButton botonModulo(String texto, boolean habilitado, Runnable accion) {
        JButton boton = new JButton(texto);
<<<<<<< HEAD
        boton.setEnabled(habilitado); //
        boton.setFont(FUENTE_BOTONES);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        if (habilitado) {
            boton.setBackground(Color.WHITE);
            boton.setForeground(COLOR_PRIMARIO);
            
            boton.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(COLOR_PRIMARIO, 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)));


            boton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    boton.setBackground(COLOR_PRIMARIO);
                    boton.setForeground(Color.WHITE);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    boton.setBackground(Color.WHITE);
                    boton.setForeground(COLOR_PRIMARIO);
                }
            });
        } else {
            boton.setBackground(new Color(0xE0, 0xE0, 0xE0));
            boton.setForeground(new Color(0x9E, 0x9E, 0x9E));
            boton.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(0xD3, 0xD3, 0xD3), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)));    
        }


        // TO-DO: los módulos con accion == null se implementan en los pasos 7 a 10.
        boton.addActionListener(e -> {
            if (accion != null) {
                accion.run(); //
            } else {
                JOptionPane.showMessageDialog(this,
                        "Módulo \"" + texto + "\" pendiente de implementación.",
                        "En construcción", JOptionPane.INFORMATION_MESSAGE); //
            }
        });
        return boton;
    }

    //metodo crear boton
    private JButton crearBotónHeader(String texto, Color base, Color hover) {
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
=======
        boton.setEnabled(habilitado);
        // TODO: los módulos con accion == null se implementan en los pasos 7 a 10.
        boton.addActionListener(e -> {
            if (accion != null) {
                accion.run();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Módulo \"" + texto + "\" pendiente de implementación.",
                        "En construcción", JOptionPane.INFORMATION_MESSAGE);
>>>>>>> f718887c04f41f7c1e70427ecaf1e0db29752987
            }
        });
        return boton;
    }

    private void actualizarPanel() {
        boolean conectado = controlador.verificarConexion();
        indicadorConexion.setText(conectado ? "● Conectado" : "● Sin conexión");
        indicadorConexion.setForeground(conectado ? new Color(0x2E, 0x7D, 0x32) : new Color(0xB0, 0x00, 0x20));

        if (!conectado) {
            limpiarIndicadores();
            return;
        }

        try {
            IndicadoresDashboard indicadores = controlador.cargarIndicadores();
            valorProductosActivos.setText(String.valueOf(indicadores.productosActivos()));
            valorBajoStock.setText(String.valueOf(indicadores.productosBajoStockMinimo()));
            valorPedidosPendientes.setText(String.valueOf(indicadores.pedidosWebPendientes()));
            valorDonacionesPendientes.setText(String.valueOf(indicadores.donacionesPendientes()));
            valorLotesEnRuta.setText(String.valueOf(indicadores.lotesEnRuta()));
        } catch (ServicioException e) {
            limpiarIndicadores();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarIndicadores() {
        valorProductosActivos.setText("-");
        valorBajoStock.setText("-");
        valorPedidosPendientes.setText("-");
        valorDonacionesPendientes.setText("-");
        valorLotesEnRuta.setText("-");
    }

    private void cerrarSesion() {
        controlador.cerrarSesion();
        dispose();
        new LoginJFrame().setVisible(true);
    }

    private void respaldarAhora() {
        try {
            Path carpeta = backupService.ejecutarBackup();
            JOptionPane.showMessageDialog(this, "Respaldo generado en " + carpeta.toAbsolutePath(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** RNF-05: respaldo automático al cerrar si pasaron más de 24 h desde el último. */
    private void cerrarAplicacion() {
        try {
            if (backupService.debeRespaldarAutomaticamente()) {
                backupService.ejecutarBackup();
            }
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo generar el respaldo automático: " + e.getMessage(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
        System.exit(0);
    }
<<<<<<< HEAD


    //FRANJA
    private static class FranjaDecorativaHoseg extends JComponent {
        public FranjaDecorativaHoseg() {
            setPreferredSize(new Dimension(100, 4));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Activar calidad alta
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Degradado lineal horizontal: Azul -> Morado -> Rosa -> Rojo
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
