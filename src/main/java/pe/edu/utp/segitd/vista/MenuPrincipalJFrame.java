package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.MenuPrincipalControlador;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.IndicadoresDashboard;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;

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

/**
 * Dashboard principal: indicadores en vivo y accesos a cada módulo,
 * habilitados según el rol de la sesión activa (sección 8, pantalla 2).
 */
public class MenuPrincipalJFrame extends JFrame {

    private final MenuPrincipalControlador controlador = new MenuPrincipalControlador();

    private final JLabel indicadorConexion = new JLabel();
    private final JLabel valorProductosActivos = new JLabel("-");
    private final JLabel valorBajoStock = new JLabel("-");
    private final JLabel valorPedidosPendientes = new JLabel("-");
    private final JLabel valorDonacionesPendientes = new JLabel("-");
    private final JLabel valorLotesEnRuta = new JLabel("-");

    public MenuPrincipalJFrame() {
        super("SEGITD-HÖSÉG · Panel principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(760, 480));
        pack();
        setLocationRelativeTo(null);
        actualizarPanel();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        raiz.add(construirEncabezado(), BorderLayout.NORTH);
        raiz.add(construirIndicadores(), BorderLayout.CENTER);
        raiz.add(construirModulos(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirEncabezado() {
        Usuario usuario = SesionUsuario.obtenerInstancia().getUsuarioActual();

        JPanel encabezado = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Höség Store — Back office");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        encabezado.add(titulo, BorderLayout.WEST);

        JPanel derecha = new JPanel();
        derecha.add(indicadorConexion);
        derecha.add(new JLabel(usuario.getNombre() + " (" + usuario.getRol() + ")"));
        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(e -> actualizarPanel());
        derecha.add(botonActualizar);
        JButton botonCerrarSesion = new JButton("Cerrar sesión");
        botonCerrarSesion.addActionListener(e -> cerrarSesion());
        derecha.add(botonCerrarSesion);
        encabezado.add(derecha, BorderLayout.EAST);

        return encabezado;
    }

    private JPanel construirIndicadores() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 12));
        panel.add(tarjeta("Productos activos", valorProductosActivos));
        panel.add(tarjeta("Bajo stock mínimo", valorBajoStock));
        panel.add(tarjeta("Pedidos web pendientes", valorPedidosPendientes));
        panel.add(tarjeta("Donaciones por asignar", valorDonacionesPendientes));
        panel.add(tarjeta("Lotes en ruta", valorLotesEnRuta));
        return panel;
    }

    private JPanel tarjeta(String etiqueta, JLabel valor) {
        JPanel tarjeta = new JPanel(new BorderLayout(4, 4));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 28f));
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel descripcion = new JLabel(etiqueta, SwingConstants.CENTER);
        tarjeta.add(valor, BorderLayout.CENTER);
        tarjeta.add(descripcion, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel construirModulos() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        Usuario usuario = SesionUsuario.obtenerInstancia().getUsuarioActual();
        boolean esAdministrador = usuario.getRol() == RolUsuario.ADMINISTRADOR;

        panel.add(botonModulo("Gestión de productos", true, () -> new GestionProductosJFrame().setVisible(true)));
        panel.add(botonModulo("Pedidos web", true, null));
        panel.add(botonModulo("Despacho de lotes", true, null));
        panel.add(botonModulo("Reportes de impacto", esAdministrador, null));
        panel.add(botonModulo("Pedidos a proveedores", esAdministrador, null));
        panel.add(botonModulo("Gestión de usuarios", esAdministrador, null));

        return panel;
    }

    private JButton botonModulo(String texto, boolean habilitado, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setEnabled(habilitado);
        // TODO: los módulos con accion == null se implementan en los pasos 7 a 10.
        boton.addActionListener(e -> {
            if (accion != null) {
                accion.run();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Módulo \"" + texto + "\" pendiente de implementación.",
                        "En construcción", JOptionPane.INFORMATION_MESSAGE);
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
}
