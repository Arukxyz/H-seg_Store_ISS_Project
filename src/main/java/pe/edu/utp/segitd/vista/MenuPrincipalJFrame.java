package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.MenuPrincipalControlador;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.BackupService;
import pe.edu.utp.segitd.servicio.IndicadoresDashboard;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final JLabel valorPedidosPendientes = new JLabel("-");
    private final JLabel valorDonacionesPendientes = new JLabel("-");
    
    private final JProgressBar barraStockCritico = new JProgressBar(0, 100);
    private final JProgressBar barraLotesRuta = new JProgressBar(0, 100);
    private final JLabel lblNumStockCritico = new JLabel("0");
    private final JLabel lblNumLotesRuta = new JLabel("0");

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
        
        setMinimumSize(new Dimension(980, 560)); 
        this.getContentPane().setBackground(COLOR_FONDO_VENTANA);
        pack();
        setLocationRelativeTo(null);
        actualizarPanel(); //
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion(); //
            }
        });
    }

    private JPanel construirContenido() {
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
        raiz.add(construirIndicadores(), BorderLayout.CENTER);
        raiz.add(construirModulos(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirEncabezado() {
        Usuario usuario = SesionUsuario.obtenerInstancia().getUsuarioActual();

        JPanel encabezado = new JPanel(new BorderLayout());
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
        return encabezado;
    }

    private JPanel construirIndicadores() {

        JPanel contenedorControles = new JPanel(new BorderLayout(16, 0));
        contenedorControles.setBackground(COLOR_FONDO_VENTANA);

        JPanel panelTarjetasIzquierda = new JPanel(new GridLayout(1, 3, 14, 0));
        panelTarjetasIzquierda.setBackground(COLOR_FONDO_VENTANA);
        
        panelTarjetasIzquierda.add(tarjeta("Productos activos", valorProductosActivos, new Color(0x00, 0x33, 0xAA))); // Barra Azul
        panelTarjetasIzquierda.add(tarjeta("Pedidos web pendientes", valorPedidosPendientes, new Color(0x6A, 0x1B, 0x9A))); // Barra Morada
        panelTarjetasIzquierda.add(tarjeta("Donaciones por asignar", valorDonacionesPendientes, new Color(0x2E, 0x7D, 0x32))); // Barra Verde
        
        contenedorControles.add(panelTarjetasIzquierda, BorderLayout.CENTER);
        
        // Lado Derecho
        JPanel panelGraficoDerecha = new JPanel(new BorderLayout());
        panelGraficoDerecha.setBackground(Color.WHITE);
        panelGraficoDerecha.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE5, 0xE5, 0xE3), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

                JPanel filaTituloGrafico = new JPanel();
        filaTituloGrafico.setLayout(new BoxLayout(filaTituloGrafico, BoxLayout.Y_AXIS));
        filaTituloGrafico.setBackground(Color.WHITE);

        JLabel tituloGrafico = new JLabel("Estado de Almacén y Logística", SwingConstants.LEFT);
        tituloGrafico.setFont(new Font("SansSerif", Font.BOLD, 13));
        tituloGrafico.setForeground(COLOR_TEXTO_MAIN);
        tituloGrafico.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaTituloGrafico.add(tituloGrafico);
        filaTituloGrafico.add(Box.createVerticalStrut(6));

        JPanel filaBotonDetalle = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        filaBotonDetalle.setBackground(Color.WHITE);
        filaBotonDetalle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton botonVerReporteStock = new JButton("Reporte de stock");
        botonVerReporteStock.setFont(new Font("SansSerif", Font.BOLD, 11));
        botonVerReporteStock.setForeground(COLOR_PRIMARIO);
        botonVerReporteStock.setBackground(Color.WHITE);
        botonVerReporteStock.setFocusPainted(false);
        botonVerReporteStock.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonVerReporteStock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARIO, 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        botonVerReporteStock.addActionListener(e -> new ReporteStockJFrame().setVisible(true));
        botonVerReporteStock.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botonVerReporteStock.setBackground(COLOR_PRIMARIO);
                botonVerReporteStock.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                botonVerReporteStock.setBackground(Color.WHITE);
                botonVerReporteStock.setForeground(COLOR_PRIMARIO);
            }
        });
        filaBotonDetalle.add(botonVerReporteStock);
        filaTituloGrafico.add(filaBotonDetalle);

        panelGraficoDerecha.add(filaTituloGrafico, BorderLayout.NORTH);

        JPanel cuerpoGrafico = new JPanel(new GridLayout(2, 1, 0, 10));
        cuerpoGrafico.setBackground(Color.WHITE);
        cuerpoGrafico.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        barraStockCritico.setForeground(COLOR_BURDEO);
        barraStockCritico.setBackground(new Color(0xF5, 0xF5, 0xF5));
        barraStockCritico.setBorderPainted(false);
        
        barraLotesRuta.setForeground(new Color(0x00, 0x33, 0xAA));
        barraLotesRuta.setBackground(new Color(0xF5, 0xF5, 0xF5));
        barraLotesRuta.setBorderPainted(false);
        
        JPanel fila1 = new JPanel(new BorderLayout(10, 0));
        fila1.setBackground(Color.WHITE);
        JLabel lblEtiq1 = new JLabel("Productos Críticos:");
        lblEtiq1.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEtiq1.setPreferredSize(new Dimension(110, 20));
        lblNumStockCritico.setFont(new Font("SansSerif", Font.BOLD, 12));
        fila1.add(lblEtiq1, BorderLayout.WEST);
        fila1.add(barraStockCritico, BorderLayout.CENTER);
        fila1.add(lblNumStockCritico, BorderLayout.EAST);
        
        JPanel fila2 = new JPanel(new BorderLayout(10, 0));
        fila2.setBackground(Color.WHITE);
        JLabel lblEtiq2 = new JLabel("Lotes en Ruta:");
        lblEtiq2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEtiq2.setPreferredSize(new Dimension(110, 20));
        lblNumLotesRuta.setFont(new Font("SansSerif", Font.BOLD, 12));
        fila2.add(lblEtiq2, BorderLayout.WEST);
        fila2.add(barraLotesRuta, BorderLayout.CENTER);
        fila2.add(lblNumLotesRuta, BorderLayout.EAST);

        cuerpoGrafico.add(fila1);
        cuerpoGrafico.add(fila2);
        
        panelGraficoDerecha.add(cuerpoGrafico, BorderLayout.CENTER);
           panelGraficoDerecha.setPreferredSize(new Dimension(320, 128));
        
        contenedorControles.add(panelGraficoDerecha, BorderLayout.EAST);
        return contenedorControles;
    }

    private JPanel tarjeta(String etiqueta, JLabel valor, Color colorAcento) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new LineBorder(new Color(0xE5, 0xE5, 0xE3), 1));

        JPanel barraInferior = new JPanel();
        barraInferior.setBackground(colorAcento);
        barraInferior.setPreferredSize(new Dimension(10, 4));
        wrapper.add(barraInferior, BorderLayout.SOUTH);

        JPanel cuerpoTarjeta = new JPanel(new BorderLayout(6, 6));
        cuerpoTarjeta.setBackground(Color.WHITE);
        cuerpoTarjeta.setBorder(BorderFactory.createEmptyBorder(16, 10, 12, 10));

        valor.setFont(FUENTE_INDICADOR_NUM);
        valor.setForeground(COLOR_PRIMARIO);
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel descripcion = new JLabel("<html><center>" + etiqueta + "</center></html>", SwingConstants.CENTER);
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descripcion.setForeground(COLOR_GRIS_TEXTO);
        
        cuerpoTarjeta.add(valor, BorderLayout.CENTER);
        cuerpoTarjeta.add(descripcion, BorderLayout.SOUTH);
        
        wrapper.add(cuerpoTarjeta, BorderLayout.CENTER);
        return wrapper;
    }

    ///////////////////////////////////777
    private JPanel construirModulos() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 14, 14));
        panel.setBackground(COLOR_FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

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
            valorPedidosPendientes.setText(String.valueOf(indicadores.pedidosWebPendientes()));
            valorDonacionesPendientes.setText(String.valueOf(indicadores.donacionesPendientes()));
            
            int bajoStock = indicadores.productosBajoStockMinimo();
            int lotesRuta = indicadores.lotesEnRuta();
            
            lblNumStockCritico.setText(String.valueOf(bajoStock));
            lblNumLotesRuta.setText(String.valueOf(lotesRuta));
            
            barraStockCritico.setValue(Math.min(100, (bajoStock * 100) / 10));
            barraLotesRuta.setValue(Math.min(100, (lotesRuta * 100) / 10));
            
        } catch (ServicioException e) {
            limpiarIndicadores();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

     private void limpiarIndicadores() {
        valorProductosActivos.setText("-");
        valorPedidosPendientes.setText("-");
        valorDonacionesPendientes.setText("-");
        
        lblNumStockCritico.setText("0");
        lblNumLotesRuta.setText("0");
        
        barraStockCritico.setValue(0);
        barraLotesRuta.setValue(0);
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

}