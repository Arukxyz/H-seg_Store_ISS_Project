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
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

    public DespachoLotesJFrame() {
        super("SEGITD-HÖSÉG · Despacho de lotes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setMinimumSize(new Dimension(980, 620));
        pack();
        setLocationRelativeTo(null);
        cargarSelectores();
        cargarDonacionesPendientes();
        cargarLotes();
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Nuevo lote", construirTabNuevoLote());
        pestanas.addTab("Lotes", construirTabLotes());
        raiz.add(pestanas, BorderLayout.CENTER);
        return raiz;
    }

    private JPanel construirTabNuevoLote() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel panelSelectores = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelSelectores.add(new JLabel("Comunidad:"));
        panelSelectores.add(comboComunidad);
        panelSelectores.add(new JLabel("ONG:"));
        panelSelectores.add(comboOng);
        JButton botonCrear = new JButton("Crear lote con las seleccionadas");
        botonCrear.addActionListener(e -> crearLote());
        panelSelectores.add(botonCrear);
        panel.add(panelSelectores, BorderLayout.NORTH);

        panel.add(new JScrollPane(tablaDonacionesPendientes), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirTabLotes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, construirPanelListaLotes(), construirPanelDetalleLote());
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelListaLotes() {
        tablaLotes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaLotes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDetalleLote();
            }
        });
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lotes"));
        panel.add(new JScrollPane(tablaLotes), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelDetalleLote() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Donaciones del lote"));
        panel.add(new JScrollPane(tablaDonacionesLote), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botonEnRuta.setEnabled(false);
        botonEntregado.setEnabled(false);
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
}
