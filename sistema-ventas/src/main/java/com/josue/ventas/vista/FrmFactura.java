/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
josue zetino
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.FacturaController;
import com.josue.ventas.controlador.ProductoController;
import com.josue.ventas.controlador.ClienteController;
import com.josue.ventas.modelo.Factura;
import com.josue.ventas.modelo.FacturaDetalle;
import com.josue.ventas.modelo.Producto;
import com.josue.ventas.modelo.Cliente;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author josue zetino
 */
public class FrmFactura extends javax.swing.JInternalFrame {

    FacturaController controller;
    ProductoController productoController;
    ClienteController clienteController;
    DefaultTableModel modeloTabla;
    Factura facturaActual;

    public FrmFactura() {
        initComponents();
        controller = new FacturaController();
        productoController = new ProductoController();
        clienteController = new ClienteController();
        facturaActual = new Factura();
        configurarTabla();
        fechaActual();
        cargarBuscadores();
        txtNumFactura.setText(controller.obtenerSiguienteNumeroFactura());
    }

    private void cargarBuscadores() {
        List<String> etiquetasProductos = new ArrayList<>();
        List<Producto> productos = productoController.GetProductos();
        for (Producto p : productos) {
            etiquetasProductos.add(p.getCodigo() + " - " + p.getNombre());
        }
        campoBuscarProducto.setElementos(etiquetasProductos);

        List<String> etiquetasClientes = new ArrayList<>();
        List<Cliente> clientes = clienteController.GetClientes();
        for (Cliente c : clientes) {
            etiquetasClientes.add(c.getNit() + " - " + c.getNombre());
        }
        campoBuscarCliente.setElementos(etiquetasClientes);

        campoBuscarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productoSeleccionado();
            }
        });
        campoBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clienteSeleccionado();
            }
        });
    }

    private void productoSeleccionado() {
        String texto = campoBuscarProducto.getText().trim();
        for (Producto p : productoController.GetProductos()) {
            if ((p.getCodigo() + " - " + p.getNombre()).equalsIgnoreCase(texto)
                    || p.getCodigo().equalsIgnoreCase(texto)) {
                txtPrecio.setText(String.format("%.2f", p.getPrecio()));
                return;
            }
        }
    }

    private void clienteSeleccionado() {
        String texto = campoBuscarCliente.getText().trim();
        for (Cliente c : clienteController.GetClientes()) {
            if ((c.getNit() + " - " + c.getNombre()).equalsIgnoreCase(texto)
                    || c.getNit().equalsIgnoreCase(texto)) {
                txtCliente.setText(c.getNombre());
                txtNit.setText(c.getNit());
                return;
            }
        }
    }

    private void configurarTabla() {
        String[] columnas = {"Producto", "Cantidad", "Precio", "Subtotal"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTableProductos.setModel(modeloTabla);
    }

    private void fechaActual() {
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        txtFecha.setText(LocalDate.now().format(sdf));
    }

    private void agregarProducto() {
        String producto = campoBuscarProducto.getText().trim();
        String cantidadText = txtCantidad.getText().trim();
        String precioText = txtPrecio.getText().trim();

        if (producto.isEmpty() || cantidadText.isEmpty() || precioText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos del producto.");
            return;
        }

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (producto.equalsIgnoreCase(String.valueOf(modeloTabla.getValueAt(i, 0)))) {
                JOptionPane.showMessageDialog(this, "Ese producto ya está agregado a la factura.");
                return;
            }
        }

        try {
            int cantidad = Integer.parseInt(cantidadText);
            double precio = Double.parseDouble(precioText);

            if (cantidad <= 0 || precio <= 0) {
                JOptionPane.showMessageDialog(this, "Cantidad y precio deben ser mayores a 0.");
                return;
            }

            Producto productoCantidad = buscarProductoSeleccionado();
            if (productoCantidad != null && !productoCantidad.hayExistencia(cantidad)) {
                JOptionPane.showMessageDialog(this, "No hay existencia suficiente del producto (quedan "
                        + productoCantidad.getExistencia() + ").");
                return;
            }

            FacturaDetalle detalle = new FacturaDetalle(productoCantidad, cantidad, precio);
            facturaActual.agregarDetalle(detalle);
            Object[] fila = {producto, cantidad, String.format("%.2f", precio), String.format("%.2f", cantidad * precio)};
            modeloTabla.addRow(fila);

            limpiarCamposProducto();
            actualizarTotal();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad y precio deben ser números válidos.");
        }
    }

    private Producto buscarProductoSeleccionado() {
        String texto = campoBuscarProducto.getText().trim();
        for (Producto p : productoController.GetProductos()) {
            if ((p.getCodigo() + " - " + p.getNombre()).equalsIgnoreCase(texto)
                    || p.getCodigo().equalsIgnoreCase(texto)) {
                return p;
            }
        }
        Producto noRegistrado = new Producto();
        noRegistrado.setNombre(texto);
        return noRegistrado;
    }

    private void eliminarProducto() {
        int filaSeleccionada = jTableProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar.");
            return;
        }

        facturaActual.eliminarDetalle(filaSeleccionada);
        modeloTabla.removeRow(filaSeleccionada);
        actualizarTotal();
    }

    private void actualizarTotal() {
        lblTotal.setText(String.format("%.2f", facturaActual.getTotal()));
    }

    private void limpiarCamposProducto() {
        campoBuscarProducto.setText("");
        txtCantidad.setText("");
        txtPrecio.setText("");
    }

    private boolean validarNit(String nit) {
        String soloDigitos = nit.replace("-", "").replace(" ", "");
        if (soloDigitos.length() < 8 || soloDigitos.length() > 13) {
            return false;
        }
        for (char c : soloDigitos.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private void guardarFactura() {
        String cliente = txtCliente.getText().trim();
        String nit = txtNit.getText().trim();
        String numeroFactura = txtNumFactura.getText().trim();

        if (cliente.isEmpty() || nit.isEmpty() || numeroFactura.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete la información del cliente y el número de factura.");
            return;
        }

        if (!validarNit(nit)) {
            JOptionPane.showMessageDialog(this, "El NIT debe tener entre 8 y 13 dígitos (los guiones son opcionales).");
            return;
        }

        for (Factura f : controller.GetFacturas()) {
            if (f.getNumeroFactura() != null && f.getNumeroFactura().equalsIgnoreCase(numeroFactura)) {
                JOptionPane.showMessageDialog(this, "Ya existe una factura con ese número.");
                return;
            }
        }

        if (facturaActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un producto.");
            return;
        }

        Cliente clienteFactura = new Cliente();
        clienteFactura.setNombre(cliente);
        clienteFactura.setNit(nit);
        facturaActual.setCliente(clienteFactura);
        facturaActual.setNumeroFactura(numeroFactura);
        facturaActual.setFecha(LocalDate.now());

        controller.Guardar(facturaActual);
        JOptionPane.showMessageDialog(this, "Factura guardada con éxito.");

        limpiarFormulario();
    }

    public void limpiarFormulario() {
        txtCliente.setText("");
        txtNit.setText("");
        txtNumFactura.setText(controller.obtenerSiguienteNumeroFactura());
        fechaActual();
        facturaActual = new Factura();
        modeloTabla.setRowCount(0);
        lblTotal.setText("0.00");
    }

    private void imprimirFactura() {
        if (facturaActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en la factura para imprimir.");
            return;
        }
        new FrmVistaPreviaFactura(this, facturaActual).setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtCliente = new javax.swing.JTextField();
        txtNit = new javax.swing.JTextField();
        txtFecha = new javax.swing.JTextField();
        txtNumFactura = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        campoBuscarCliente = new CampoBusqueda();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        campoBuscarProducto = new CampoBusqueda();
        txtCantidad = new javax.swing.JTextField();
        txtPrecio = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableProductos = new javax.swing.JTable();
        btnEliminar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Formulario de Facturación");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del Cliente"));

        jLabel1.setText("Cliente:");

        jLabel2.setText("NIT:");

        jLabel3.setText("Fecha:");

        jLabel4.setText("No. Factura:");

        jLabel9.setText("Cliente del catálogo:");

        txtFecha.setEditable(false);

        txtNumFactura.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(txtCliente)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(txtNit, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(txtNumFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(campoBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtNumFactura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(campoBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Agregar Producto"));

        jLabel5.setText("Producto:");

        jLabel6.setText("Cantidad:");

        jLabel7.setText("Precio:");

        btnAgregar.setText("Agregar");
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(campoBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnAgregar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(campoBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTableProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Cantidad", "Precio", "Subtotal"
            }
        ));
        jScrollPane1.setViewportView(jTableProductos);

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnImprimir.setText("Imprimir");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });

        jLabel8.setText("Total:");

        lblTotal.setText("0.00");
        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 14));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnImprimir)
                        .addGap(18, 18, 18)
                        .addComponent(btnEliminar)
                        .addGap(18, 18, 18)
                        .addComponent(btnGuardar))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(lblTotal)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnImprimir)
                    .addComponent(btnEliminar)
                    .addComponent(btnGuardar))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(lblTotal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        agregarProducto();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminarProducto();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarFactura();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirFactura();
    }//GEN-LAST:event_btnImprimirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnImprimir;
    private CampoBusqueda campoBuscarCliente;
    private CampoBusqueda campoBuscarProducto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableProductos;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCliente;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtNit;
    private javax.swing.JTextField txtNumFactura;
    private javax.swing.JTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}