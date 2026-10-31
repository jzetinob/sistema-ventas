package com.josue.ventas.vista;

import com.josue.ventas.controlador.OrdenVentaController;
import com.josue.ventas.modelo.OrdenVenta;
import com.josue.ventas.modelo.OrdenVentaDetalle;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Ordenes de venta que llegan desde la app movil (API REST): lista con
 * buscador y, abajo, el detalle y las observaciones de la orden seleccionada.
 * Una orden PENDIENTE se puede confirmar o anular.
 *
 * @author josue zetino
 */
public class FrmOrdenesVenta extends javax.swing.JInternalFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrdenVentaController controller = new OrdenVentaController();
    private List<OrdenVenta> ordenes = new ArrayList<>();

    private final DefaultTableModel modeloOrdenes = modeloSoloLectura("Número", "Fecha", "Cliente", "NIT", "Registró", "Estado", "Total");
    private final DefaultTableModel modeloDetalle = modeloSoloLectura("Código", "Producto", "Cantidad", "Precio", "Subtotal");
    private final JTable tablaOrdenes = new JTable(modeloOrdenes);
    private final JLabel lblObservaciones = new JLabel(" ");

    public FrmOrdenesVenta() {
        super("Órdenes de Venta (desde la app móvil)", true, true, true, true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tablaOrdenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaOrdenes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });

        JPanel detalle = new JPanel(new BorderLayout(0, 6));
        detalle.setBorder(BorderFactory.createTitledBorder("Detalle de la orden seleccionada"));
        detalle.add(new JScrollPane(new JTable(modeloDetalle)), BorderLayout.CENTER);
        detalle.add(lblObservaciones, BorderLayout.SOUTH);

        JPanel lista = BarraBusqueda.crear(tablaOrdenes, new JScrollPane(tablaOrdenes), "Órdenes de venta");
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lista, detalle);
        division.setResizeWeight(0.6);
        division.setPreferredSize(new java.awt.Dimension(760, 420));

        JButton btnRefrescar = new JButton("Actualizar lista");
        JButton btnConfirmar = new JButton("Confirmar orden");
        JButton btnAnular = new JButton("Anular orden");
        btnRefrescar.addActionListener(e -> refrescar());
        btnConfirmar.addActionListener(e -> confirmar());
        btnAnular.addActionListener(e -> anular());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnRefrescar);
        botones.add(btnConfirmar);
        botones.add(btnAnular);

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.add(division, BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);

        refrescar();
        pack();
    }

    private static DefaultTableModel modeloSoloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void refrescar() {
        ordenes = controller.GetOrdenes();
        modeloOrdenes.setRowCount(0);
        for (OrdenVenta o : ordenes) {
            modeloOrdenes.addRow(new Object[]{o.getNumeroOrden(), o.getFecha().format(FORMATO_FECHA),
                o.getCliente().getNombre(), o.getCliente().getNit(),
                o.getUsuario() != null ? o.getUsuario().getUsuario() : "",
                o.getEstado().name(), String.format("%.2f", o.getTotal())});
        }
        modeloDetalle.setRowCount(0);
        lblObservaciones.setText(" ");
    }

    private OrdenVenta ordenSeleccionada() {
        int fila = FiltroTabla.filaDelModelo(tablaOrdenes);
        return fila == -1 ? null : ordenes.get(fila);
    }

    private void mostrarDetalle() {
        modeloDetalle.setRowCount(0);
        lblObservaciones.setText(" ");
        OrdenVenta orden = ordenSeleccionada();
        if (orden == null) {
            return;
        }
        // el detalle se pide a la base solo para la orden seleccionada
        orden.setDetalles(controller.GetDetalles(orden.getId()));
        for (OrdenVentaDetalle d : orden.getDetalles()) {
            modeloDetalle.addRow(new Object[]{d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                String.format("%.2f", d.getPrecioUnitario()), String.format("%.2f", d.calcularSubtotal())});
        }
        String obs = orden.getObservaciones();
        lblObservaciones.setText("Observaciones: " + (obs == null || obs.isBlank() ? "(ninguna)" : obs));
    }

    private void confirmar() {
        OrdenVenta orden = ordenSeleccionada();
        mostrarResultado(controller.Confirmar(orden), orden, "confirmada");
    }

    private void anular() {
        OrdenVenta orden = ordenSeleccionada();
        if (orden != null && orden.estaPendiente() && JOptionPane.showConfirmDialog(this,
                "¿Anular la orden " + orden.getNumeroOrden() + "?", "Anular orden",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        mostrarResultado(controller.Anular(orden), orden, "anulada");
    }

    private void mostrarResultado(String error, OrdenVenta orden, String accion) {
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Órdenes de venta", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Orden " + orden.getNumeroOrden() + " " + accion + ".");
        refrescar();
    }
}
