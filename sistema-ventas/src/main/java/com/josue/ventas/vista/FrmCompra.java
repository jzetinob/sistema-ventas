/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.CompraController;
import com.josue.ventas.controlador.ProductoController;
import com.josue.ventas.controlador.ProveedorController;
import com.josue.ventas.modelo.Compra;
import com.josue.ventas.modelo.CompraDetalle;
import com.josue.ventas.modelo.Producto;
import com.josue.ventas.modelo.Proveedor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Registro de compras a proveedores (maestro-detalle). Al guardar, la
 * existencia de cada producto aumenta en la cantidad comprada.
 *
 * @author josue zetino
 */
public class FrmCompra extends javax.swing.JInternalFrame {

    private final CompraController controller = new CompraController();
    private final ProveedorController proveedorController = new ProveedorController();
    private final ProductoController productoController = new ProductoController();

    private final Map<String, Proveedor> proveedores = new LinkedHashMap<>();
    private final Map<String, Producto> productos = new LinkedHashMap<>();
    private Compra compraActual = new Compra();

    private final JLabel lblNumero = new JLabel();
    private final JLabel lblFecha = new JLabel();
    private final CampoBusqueda campoProveedor = new CampoBusqueda();
    private final CampoBusqueda campoProducto = new CampoBusqueda();
    private final JTextField txtCantidad = new JTextField(6);
    private final JTextField txtCosto = new JTextField(8);
    private final JLabel lblTotal = new JLabel("0.00");
    private final JLabel lblReferencia = new JLabel(" ");
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Código", "Producto", "Cantidad", "Costo unitario", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public FrmCompra() {
        super("Registro de Compra", true, true, true, true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        construirPantalla();
        cargarCatalogos();
        nuevaCompra();
        pack();
    }

    private void construirPantalla() {
        JPanel encabezado = new JPanel(new GridBagLayout());
        encabezado.setBorder(BorderFactory.createTitledBorder("Datos de la Compra"));
        agregar(encabezado, 0, 0, "No. compra:", lblNumero);
        agregar(encabezado, 0, 2, "Fecha:", lblFecha);
        agregar(encabezado, 1, 0, "Proveedor:", campoProveedor);

        JPanel lineas = new JPanel(new GridBagLayout());
        lineas.setBorder(BorderFactory.createTitledBorder("Agregar Producto"));
        agregar(lineas, 0, 0, "Producto:", campoProducto);
        agregar(lineas, 1, 0, "Cantidad:", txtCantidad);
        agregar(lineas, 1, 2, "Costo unitario:", txtCosto);
        JButton btnAgregar = new JButton("Agregar");
        JButton btnQuitar = new JButton("Quitar");
        btnAgregar.addActionListener(e -> agregarProducto());
        btnQuitar.addActionListener(e -> quitarProducto());
        JPanel botonesLinea = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botonesLinea.add(btnAgregar);
        botonesLinea.add(btnQuitar);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 1;
        lineas.add(botonesLinea, c);
        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 4;
        r.gridy = 0;
        r.insets = new Insets(4, 10, 4, 4);
        r.anchor = GridBagConstraints.LINE_START;
        lblReferencia.setForeground(java.awt.Color.DARK_GRAY);
        lineas.add(lblReferencia, r);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(lineas, BorderLayout.CENTER);

        JButton btnGuardar = new JButton("Guardar compra");
        JButton btnLimpiar = new JButton("Limpiar");
        btnGuardar.addActionListener(e -> guardarCompra());
        btnLimpiar.addActionListener(e -> nuevaCompra());
        lblTotal.setFont(lblTotal.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        abajo.add(new JLabel("Total Q"));
        abajo.add(lblTotal);
        abajo.add(btnLimpiar);
        abajo.add(btnGuardar);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new java.awt.Dimension(640, 200));

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(abajo, BorderLayout.SOUTH);
        setContentPane(contenido);

        campoProducto.addActionListener(e -> productoElegido());
        // si el producto se escribio completo sin elegirlo de la lista, igual se completa al pasar al siguiente campo
        java.awt.event.FocusAdapter completar = new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                completarDatosProducto();
            }
        };
        txtCantidad.addFocusListener(completar);
        txtCosto.addFocusListener(completar);
    }

    private void agregar(JPanel panel, int fila, int columna, String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = columna + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        if (campo instanceof CampoBusqueda) {
            c.gridwidth = 3;
            campo.setPreferredSize(new java.awt.Dimension(320, campo.getPreferredSize().height));
        }
        panel.add(campo, c);
    }

    /** Los buscadores releen proveedores y productos cada vez que se entra a ellos. */
    private void cargarCatalogos() {
        campoProveedor.setFuente(this::recargarProveedores);
        campoProducto.setFuente(this::recargarProductos);
    }

    private java.util.List<String> recargarProveedores() {
        proveedores.clear();
        for (Proveedor p : proveedorController.GetProveedores()) {
            proveedores.put(p.getNit() + " - " + p.getNombre(), p);
        }
        return new ArrayList<>(proveedores.keySet());
    }

    private java.util.List<String> recargarProductos() {
        productos.clear();
        for (Producto p : productoController.GetProductos()) {
            productos.put(p.getCodigo() + " - " + p.getNombre(), p);
        }
        return new ArrayList<>(productos.keySet());
    }

    private void nuevaCompra() {
        compraActual = new Compra();
        compraActual.setFecha(LocalDate.now());
        lblNumero.setText(controller.ObtenerSiguienteNumeroCompra());
        lblFecha.setText(compraActual.getFecha().toString());
        campoProveedor.setText("");
        limpiarLinea();
        modeloTabla.setRowCount(0);
        lblTotal.setText("0.00");
    }

    private void productoElegido() {
        txtCosto.setText(""); // se eligio otro producto: se propone su propio costo
        completarDatosProducto();
        txtCantidad.requestFocusInWindow();
    }

    /**
     * Muestra el precio de venta y la existencia del producto elegido, y
     * propone como costo el de su ultima compra (si el campo esta vacio).
     * El costo es lo que se le paga al proveedor, distinto del precio de venta.
     */
    private void completarDatosProducto() {
        Producto producto = productos.get(campoProducto.getText().trim());
        if (producto == null) {
            lblReferencia.setText(" ");
            return;
        }
        Double ultimoCosto = controller.UltimoCosto(producto.getId());
        lblReferencia.setText(String.format("<html>Precio de venta: Q %.2f · Existencia: %d<br>%s</html>",
                producto.getPrecio(), producto.getExistencia(),
                ultimoCosto != null ? String.format("Último costo: Q %.2f", ultimoCosto) : "Primera compra de este producto"));
        if (ultimoCosto != null && txtCosto.getText().trim().isEmpty()) {
            txtCosto.setText(String.format(java.util.Locale.US, "%.2f", ultimoCosto));
        }
    }

    private void agregarProducto() {
        Producto producto = productos.get(campoProducto.getText().trim());
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "Elija un producto del catálogo (escriba y seleccione de la lista).");
            return;
        }
        for (CompraDetalle d : compraActual.getDetalles()) {
            if (d.getProducto().getId() == producto.getId()) {
                JOptionPane.showMessageDialog(this, "Ese producto ya está en la compra. Quítelo si desea cambiar la cantidad.");
                return;
            }
        }
        int cantidad;
        double costo;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            costo = Double.parseDouble(txtCosto.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad y costo deben ser números válidos.");
            return;
        }
        if (cantidad <= 0 || costo <= 0) {
            JOptionPane.showMessageDialog(this, "Cantidad y costo deben ser mayores a 0.");
            return;
        }
        CompraDetalle detalle = new CompraDetalle(producto, cantidad, costo);
        compraActual.agregarDetalle(detalle);
        modeloTabla.addRow(new Object[]{producto.getCodigo(), producto.getNombre(), cantidad,
            String.format("%.2f", costo), String.format("%.2f", detalle.calcularSubtotal())});
        lblTotal.setText(String.format("%.2f", compraActual.getTotal()));
        limpiarLinea();
    }

    private void quitarProducto() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para quitar.");
            return;
        }
        compraActual.eliminarDetalle(fila);
        modeloTabla.removeRow(fila);
        lblTotal.setText(String.format("%.2f", compraActual.getTotal()));
    }

    private void guardarCompra() {
        Proveedor proveedor = proveedores.get(campoProveedor.getText().trim());
        if (proveedor == null) {
            JOptionPane.showMessageDialog(this, "Elija un proveedor del catálogo.");
            return;
        }
        if (compraActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto a la compra.");
            return;
        }
        compraActual.setProveedor(proveedor);
        if (!controller.Guardar(compraActual)) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar la compra. No se aplicó ningún cambio.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Compra " + compraActual.getNumeroCompra()
                + " guardada. La existencia de los productos fue actualizada.");
        cargarCatalogos();
        nuevaCompra();
    }

    private void limpiarLinea() {
        campoProducto.setText("");
        txtCantidad.setText("");
        txtCosto.setText("");
        lblReferencia.setText(" ");
    }
}
