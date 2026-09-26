/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.CompraController;
import com.josue.ventas.modelo.Compra;
import com.josue.ventas.modelo.CompraDetalle;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Consulta de compras registradas: lista con buscador y, abajo, el detalle
 * de la compra seleccionada. Permite anular una compra (revierte la
 * existencia que habia sumado).
 *
 * @author josue zetino
 */
public class FrmListaCompras extends javax.swing.JInternalFrame {

    private final CompraController controller = new CompraController();
    private List<Compra> compras = new ArrayList<>();

    private final DefaultTableModel modeloCompras = modeloSoloLectura("No.", "Número", "Fecha", "Proveedor", "Registró", "Total");
    private final DefaultTableModel modeloDetalle = modeloSoloLectura("Código", "Producto", "Cantidad", "Costo unitario", "Subtotal");
    private final JTable tablaCompras = new JTable(modeloCompras);

    public FrmListaCompras() {
        super("Compras Registradas", true, true, true, true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tablaCompras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCompras.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });

        JPanel detalle = new JPanel(new BorderLayout());
        detalle.setBorder(BorderFactory.createTitledBorder("Detalle de la compra seleccionada"));
        detalle.add(new JScrollPane(new JTable(modeloDetalle)), BorderLayout.CENTER);

        JPanel lista = BarraBusqueda.crear(tablaCompras, new JScrollPane(tablaCompras), "Compras registradas");
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lista, detalle);
        division.setResizeWeight(0.6);
        division.setPreferredSize(new java.awt.Dimension(680, 380));

        JButton btnAnular = new JButton("Anular compra");
        JButton btnRefrescar = new JButton("Actualizar lista");
        btnAnular.addActionListener(e -> anular());
        btnRefrescar.addActionListener(e -> refrescar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnRefrescar);
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
        compras = controller.GetCompras();
        modeloCompras.setRowCount(0);
        for (Compra c : compras) {
            modeloCompras.addRow(new Object[]{c.getId(), c.getNumeroCompra(), c.getFecha().toString(),
                c.getProveedor().getNombre(), c.getUsuario() != null ? c.getUsuario().getUsuario() : "",
                String.format("%.2f", c.getTotal())});
        }
        modeloDetalle.setRowCount(0);
    }

    private Compra compraSeleccionada() {
        int fila = FiltroTabla.filaDelModelo(tablaCompras);
        return fila == -1 ? null : compras.get(fila);
    }

    private void mostrarDetalle() {
        modeloDetalle.setRowCount(0);
        Compra compra = compraSeleccionada();
        if (compra == null) {
            return;
        }
        for (CompraDetalle d : compra.getDetalles()) {
            modeloDetalle.addRow(new Object[]{d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                String.format("%.2f", d.getCostoUnitario()), String.format("%.2f", d.calcularSubtotal())});
        }
    }

    private void anular() {
        Compra compra = compraSeleccionada();
        if (compra == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una compra para anular.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Anular la compra " + compra.getNumeroCompra() + "? Se restará de la existencia lo que se había sumado.",
                "Anular compra", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        if (!controller.Anular(compra.getId())) {
            JOptionPane.showMessageDialog(this, "No se puede anular: parte de lo comprado ya se vendió "
                    + "(la existencia no alcanza para revertirla).", "Anular compra", JOptionPane.WARNING_MESSAGE);
            return;
        }
        refrescar();
    }
}
