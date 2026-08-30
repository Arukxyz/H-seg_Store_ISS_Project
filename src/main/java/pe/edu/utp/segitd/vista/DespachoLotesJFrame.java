package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.DespachoControlador;
import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.Donacion;
import pe.edu.utp.segitd.modelo.EstadoLote;
import pe.edu.utp.segitd.modelo.LoteDonacion;
import pe.edu.utp.segitd.modelo.Ong;
import pe.edu.utp.segitd.servicio.ServicioException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestión de lotes y destinos de donación (RF-05, sección 8 pantalla 5).
 * ADMINISTRADOR y ENCARGADO tienen acceso total según la tabla de roles.
 */
public class DespachoLotesJFrame extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DespachoControlador controlador = new DespachoControlador();

    private final JComboBox<Comunidad> comboComunidad = new JComboBox<>();
    private final JComboBox<Ong> comboOng = new JComboBox<>();
    private final ModeloDonacionesSeleccionables modeloDonacionesPendientes = new ModeloDonacionesSeleccionables();
    private final JTable tablaDonacionesPendientes = new JTable(modeloDonacionesPendientes);

    private final ModeloLotes modeloLotes = new ModeloLotes();
    private final JTable tablaLotes = new JTable(modeloLotes);
    private final ModeloDonacionesLote modeloDonacionesLote = new ModeloDonacionesLote();
    private final JTable tablaDonacionesLote = new JTable(modeloDonacionesLote);
    private final JButton botonEnRuta = new JButton("Marcar en ruta");
    private final JButton botonEntregado = new JButton("Marcar entregado");

    
    //colores
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3); // Crema suave
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);      // Verde Sea Pine
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);       // Terracota
    private final Color COLOR_BURDEO_HOVER = new Color(0xA6, 0x3A, 0x24);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);

    private final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private final Font FUENTE_INPUT = new Font("SansSerif", Font.PLAIN, 13);
    private final Font FUENTE_PANEL_TITULO = new Font("SansSerif", Font.BOLD, 14);


    public DespachoLotesJFrame() {
        super("SEGITD-HÖSÉG · Despacho de lotes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);
        setPreferredSize(new Dimension(1020, 610));
        setMinimumSize(new Dimension(980, 560));
        pack();
        setSize(1020, 610);
        setLocationRelativeTo(null);
        cargarSelectores();
        cargarDonacionesPendientes();
        cargarLotes();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        raiz.setBackground(COLOR_FONDO_VENTANA);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(FUENTE_LABEL);
        pestanas.setBackground(Color.WHITE);
        pestanas.setForeground(COLOR_PRIMARIO);

        pestanas.addTab("Nuevo lote", construirTabNuevoLote());
        pestanas.addTab("Lotes", construirTabLotes());
        raiz.add(pestanas, BorderLayout.CENTER);
        return raiz;
    }

    private JPanel construirTabNuevoLote() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        JPanel panelSelectores = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panelSelectores.setBackground(Color.WHITE);

        comboComunidad.setFont(FUENTE_INPUT); comboComunidad.setBackground(Color.WHITE);
        comboComunidad.setBorder(BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1));
        comboOng.setFont(FUENTE_INPUT); comboOng.setBackground(Color.WHITE);
        comboOng.setBorder(BorderFactory.createLineBorder(new Color(0xD3, 0xD3, 0xD3), 1));

        JLabel lblCom = new JLabel("Comunidad:"); lblCom.setFont(FUENTE_LABEL); lblCom.setForeground(COLOR_GRIS_TEXTO);
        JLabel lblOng = new JLabel("ONG:"); lblOng.setFont(FUENTE_LABEL); lblOng.setForeground(COLOR_GRIS_TEXTO);

        panelSelectores.add(lblCom);
        panelSelectores.add(comboComunidad);
        panelSelectores.add(lblOng);
        panelSelectores.add(comboOng);

        JButton botonCrear = new JButton("Crear lote con las seleccionadas");
        estilizarBotonPrincipal(botonCrear, COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonCrear.addActionListener(e -> crearLote());
        panelSelectores.add(botonCrear);
        
        panel.add(panelSelectores, BorderLayout.NORTH);
        
        estilizarTablaElegante(tablaDonacionesPendientes);
        JScrollPane scroll = new JScrollPane(tablaDonacionesPendientes);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirTabLotes() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(COLOR_FONDO_VENTANA);
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, construirPanelListaLotes(), construirPanelDetalleLote());
        split.setResizeWeight(0.5);
        split.setDividerLocation(180); // Ajuste matemático del alto de tabla
        split.setBorder(null);
        split.setBackground(COLOR_FONDO_VENTANA);
        
        if (split.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
            ((javax.swing.plaf.basic.BasicSplitPaneUI) split.getUI()).getDivider().setBorder(null);
        }
        
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelListaLotes() {
        estilizarTablaElegante(tablaLotes);
        tablaLotes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaLotes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDetalleLote();
            }
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE2, 0xE0), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        
        JLabel lblTit = new JLabel("Historial de Lotes Generados");
        lblTit.setFont(FUENTE_PANEL_TITULO); lblTit.setForeground(COLOR_TEXTO_MAIN);
        lblTit.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        panel.add(lblTit, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(tablaLotes);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelDetalleLote() {
        estilizarTablaElegante(tablaDonacionesLote);
        
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE2, 0xE0), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        
        JLabel lblTit = new JLabel("Donaciones Incluidas en el Lote");
        lblTit.setFont(FUENTE_PANEL_TITULO); lblTit.setForeground(COLOR_TEXTO_MAIN);
        lblTit.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        panel.add(lblTit, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(tablaDonacionesLote);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEE, 0xEE, 0xEE), 1));
        panel.add(scroll, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        panelBotones.setBackground(Color.WHITE);
        
        botonEnRuta.setEnabled(false);
        botonEntregado.setEnabled(false);
        
        estilizarBotonSecundario(botonEnRuta, COLOR_PRIMARIO);
        estilizarBotonPrincipal(botonEntregado, COLOR_BURDEO, COLOR_BURDEO_HOVER); // Entregado resalta en terracota
        
        botonEnRuta.addActionListener(e -> marcarEnRuta());
        botonEntregado.addActionListener(e -> marcarEntregado());
        
        panelBotones.add(botonEnRuta);
        panelBotones.add(botonEntregado);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarSelectores() {
        try {
            comboComunidad.removeAllItems();
            controlador.listarComunidades().forEach(comboComunidad::addItem);
            comboOng.removeAllItems();
            controlador.listarOngs().forEach(comboOng::addItem);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDonacionesPendientes() {
        try {
            modeloDonacionesPendientes.actualizar(controlador.listarDonacionesPendientes());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarLotes() {
        try {
            modeloLotes.actualizar(controlador.listarLotes());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalleLote() {
        LoteDonacion lote = loteSeleccionado();
        if (lote == null) {
            modeloDonacionesLote.actualizar(List.of());
        } else {
            try {
                modeloDonacionesLote.actualizar(controlador.listarDonacionesPorLote(lote.getId()));
            } catch (ServicioException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        botonEnRuta.setEnabled(lote != null && lote.getEstado() == EstadoLote.PENDIENTE);
        botonEntregado.setEnabled(lote != null && lote.getEstado() == EstadoLote.EN_RUTA);
    }

    private LoteDonacion loteSeleccionado() {
        int fila = tablaLotes.getSelectedRow();
        return fila < 0 ? null : modeloLotes.obtener(tablaLotes.convertRowIndexToModel(fila));
    }

    private void crearLote() {
        Comunidad comunidad = (Comunidad) comboComunidad.getSelectedItem();
        Ong ong = (Ong) comboOng.getSelectedItem();
        List<Integer> ids = modeloDonacionesPendientes.idsSeleccionadas();

        if (comunidad == null || ong == null) {
            JOptionPane.showMessageDialog(this, "Selecciona comunidad y ONG.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos una donación.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LoteDonacion lote = controlador.crearLote(comunidad.getId(), ong.getId(), ids);
            JOptionPane.showMessageDialog(this, "Lote " + lote.getCodigoLote() + " creado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarDonacionesPendientes();
            cargarLotes();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            cargarDonacionesPendientes();
        }
    }

    private void marcarEnRuta() {
        cambiarEstado(EstadoLote.EN_RUTA);
    }

    private void marcarEntregado() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Marcar el lote como ENTREGADO? Esto descuenta el stock comprometido de cada producto.",
                "Confirmar entrega", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            cambiarEstado(EstadoLote.ENTREGADO);
        }
    }

    private void cambiarEstado(EstadoLote nuevoEstado) {
        LoteDonacion lote = loteSeleccionado();
        if (lote == null) {
            return;
        }
        int idLote = lote.getId();
        try {
            controlador.cambiarEstadoLote(idLote, nuevoEstado);
            cargarLotes();
            seleccionarLotePorId(idLote);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarLotePorId(int idLote) {
        for (int fila = 0; fila < modeloLotes.getRowCount(); fila++) {
            if (modeloLotes.obtener(fila).getId().equals(idLote)) {
                tablaLotes.setRowSelectionInterval(fila, fila);
                return;
            }
        }
    }

    private static final class ModeloDonacionesSeleccionables extends AbstractTableModel {
        private final String[] columnas = {"", "Producto", "Cantidad", "Tipo"};
        private List<Donacion> donaciones = List.of();
        private final Set<Integer> seleccionadas = new HashSet<>();

        void actualizar(List<Donacion> nuevas) {
            this.donaciones = nuevas;
            seleccionadas.clear();
            fireTableDataChanged();
        }

        List<Integer> idsSeleccionadas() {
            return donaciones.stream()
                    .map(Donacion::getId)
                    .filter(seleccionadas::contains)
                    .toList();
        }

        @Override
        public int getRowCount() {
            return donaciones.size();
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
        public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == 0;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Donacion d = donaciones.get(fila);
            return switch (columna) {
                case 0 -> seleccionadas.contains(d.getId());
                case 1 -> d.getNombreProducto();
                case 2 -> d.getCantidad();
                case 3 -> d.getTipo();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object valor, int fila, int columna) {
            if (columna != 0) {
                return;
            }
            Integer id = donaciones.get(fila).getId();
            if (Boolean.TRUE.equals(valor)) {
                seleccionadas.add(id);
            } else {
                seleccionadas.remove(id);
            }
            fireTableCellUpdated(fila, columna);
        }
    }

    private static final class ModeloLotes extends AbstractTableModel {
        private final String[] columnas = {"Código", "Comunidad", "ONG", "Estado", "Creado", "Despachado"};
        private List<LoteDonacion> lotes = List.of();

        void actualizar(List<LoteDonacion> nuevos) {
            this.lotes = nuevos;
            fireTableDataChanged();
        }

        LoteDonacion obtener(int fila) {
            return lotes.get(fila);
        }

        @Override
        public int getRowCount() {
            return lotes.size();
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
            LoteDonacion l = lotes.get(fila);
            return switch (columna) {
                case 0 -> l.getCodigoLote();
                case 1 -> l.getComunidadNombre();
                case 2 -> l.getOngNombre();
                case 3 -> l.getEstado();
                case 4 -> formatear(l.getFechaCreacion());
                case 5 -> formatear(l.getFechaDespacho());
                default -> null;
            };
        }

        private String formatear(OffsetDateTime fecha) {
            return fecha == null ? "" : FORMATO_FECHA.format(fecha);
        }
    }

    private static final class ModeloDonacionesLote extends AbstractTableModel {
        private final String[] columnas = {"Producto", "Cantidad", "Tipo", "Estado"};
        private List<Donacion> donaciones = List.of();

        void actualizar(List<Donacion> nuevas) {
            this.donaciones = nuevas;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return donaciones.size();
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
            Donacion d = donaciones.get(fila);
            return switch (columna) {
                case 0 -> d.getNombreProducto();
                case 1 -> d.getCantidad();
                case 2 -> d.getTipo();
                case 3 -> d.getEstado();
                default -> null;
            };
        }
    }

    //metodos extras
        private void estilizarTablaElegante(JTable t) {
        t.setRowHeight(26);
        t.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        t.setSelectionForeground(COLOR_TEXTO_MAIN);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));

        javax.swing.table.JTableHeader header = t.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
        
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    private void estilizarBotonPrincipal(JButton boton, Color fondo, Color hover) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (boton.isEnabled()) boton.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { if (boton.isEnabled()) boton.setBackground(fondo); }
        });
    }

    private void estilizarBotonSecundario(JButton boton, Color colorBorde) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setBackground(Color.WHITE);
        boton.setForeground(colorBorde);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBorde, 1), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) { boton.setBackground(colorBorde); boton.setForeground(Color.WHITE); }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) { boton.setBackground(Color.WHITE); boton.setForeground(colorBorde); }
            }
        });
    }

}
