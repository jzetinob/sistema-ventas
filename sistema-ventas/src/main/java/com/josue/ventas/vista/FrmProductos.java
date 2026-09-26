/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.CategoriaController;
import com.josue.ventas.controlador.ProductoController;
import com.josue.ventas.modelo.Categoria;
import com.josue.ventas.modelo.Producto;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author josue zetino
 */
public class FrmProductos extends javax.swing.JInternalFrame {

    private static final Categoria SIN_CATEGORIA = new Categoria(0, "(Sin categoría)");

    ProductoController controller;
    DefaultTableModel modeloTabla;

    public FrmProductos() {
        initComponents();
        controller = new ProductoController();
        cargarCategorias();
        // si se agregan categorias con esta ventana abierta, se recargan al volver a ella
        addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) {
                cargarCategorias();
            }
        });
        configurarTabla();
        BarraBusqueda.instalar(this, jTableProductos, jScrollPane1, "Catálogo de productos");
        refrescarTabla();
        jTableProductos.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    cargarSeleccionado();
                }
            }
        });
    }

    private void configurarTabla() {
        String[] columnas = {"No.", "Código", "Nombre", "Precio", "Existencia", "Categoría"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTableProductos.setModel(modeloTabla);
    }

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = controller.GetProductos();
        for (Producto p : productos) {
            Object[] fila = {p.getId(), p.getCodigo(), p.getNombre(), String.format("%.2f", p.getPrecio()), p.getExistencia(),
                p.getCategoria() != null ? p.getCategoria().getNombre() : ""};
            modeloTabla.addRow(fila);
        }
    }

    private void cargarCategorias() {
        Object elegida = cmbCategoria.getSelectedItem();
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem(SIN_CATEGORIA);
        for (Categoria c : new CategoriaController().GetCategorias()) {
            cmbCategoria.addItem(c);
        }
        seleccionarCategoria(elegida != null ? elegida.toString() : "");
    }

    private void seleccionarCategoria(String nombre) {
        cmbCategoria.setSelectedItem(SIN_CATEGORIA);
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getNombre().equals(nombre)) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    private Categoria categoriaElegida() {
        Categoria c = (Categoria) cmbCategoria.getSelectedItem();
        return c == null || c == SIN_CATEGORIA ? null : c;
    }

    private void guardarProducto() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String precioText = txtPrecio.getText().trim();
        String existenciaText = txtExistencia.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || precioText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos del producto.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioText);
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser mayor a 0.");
                return;
            }
            int existencia = existenciaText.isEmpty() ? 0 : Integer.parseInt(existenciaText);
            if (existencia < 0) {
                JOptionPane.showMessageDialog(this, "La existencia no puede ser negativa.");
                return;
            }
            if (controller.ExisteCodigo(codigo)) {
                JOptionPane.showMessageDialog(this, "Ya existe un producto con ese código.");
                return;
            }
            Producto p = new Producto();
            p.setCodigo(codigo);
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setExistencia(existencia);
            p.setCategoria(categoriaElegida());
            controller.Guardar(p);
            limpiarCampos();
            refrescarTabla();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio y la existencia deben ser números válidos.");
        }
    }

    private void actualizarProducto() {
        int fila = FiltroTabla.filaDelModelo(jTableProductos);
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para actualizar.");
            return;
        }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String precioText = txtPrecio.getText().trim();
        String existenciaText = txtExistencia.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || precioText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos del producto.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioText);
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser mayor a 0.");
                return;
            }
            int existencia = existenciaText.isEmpty() ? 0 : Integer.parseInt(existenciaText);
            if (existencia < 0) {
                JOptionPane.showMessageDialog(this, "La existencia no puede ser negativa.");
                return;
            }
            String codigoActual = (String) modeloTabla.getValueAt(fila, 1);
            if (!codigo.equalsIgnoreCase(codigoActual) && controller.ExisteCodigo(codigo)) {
                JOptionPane.showMessageDialog(this, "Ya existe un producto con ese código.");
                return;
            }
            Producto p = new Producto();
            p.setId(id);
            p.setCodigo(codigo);
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setExistencia(existencia);
            p.setCategoria(categoriaElegida());
            controller.Actualizar(p);
            limpiarCampos();
            refrescarTabla();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio y la existencia deben ser números válidos.");
        }
    }

    private void eliminarProducto() {
        int fila = FiltroTabla.filaDelModelo(jTableProductos);
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.");
            return;
        }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el producto seleccionado?", "Eliminar Producto",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            if (!controller.Eliminar(id)) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar: el producto aparece en compras registradas.");
                return;
            }
            limpiarCampos();
            refrescarTabla();
        }
    }

    private void cargarSeleccionado() {
        int fila = FiltroTabla.filaDelModelo(jTableProductos);
        if (fila == -1) {
            return;
        }
        txtCodigo.setText((String) modeloTabla.getValueAt(fila, 1));
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 2));
        txtPrecio.setText((String) modeloTabla.getValueAt(fila, 3));
        txtExistencia.setText(String.valueOf(modeloTabla.getValueAt(fila, 4)));
        seleccionarCategoria((String) modeloTabla.getValueAt(fila, 5));
    }

    private void limpiarCampos() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtExistencia.setText("");
        cmbCategoria.setSelectedItem(SIN_CATEGORIA);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtPrecio = new javax.swing.JTextField();
        txtExistencia = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        btnGuardar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableProductos = new javax.swing.JTable();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Catálogo de Productos");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del Producto"));

        jLabel1.setText("Código:");

        jLabel2.setText("Nombre:");

        jLabel3.setText("Precio:");

        jLabel4.setText("Existencia:");

        jLabel5.setText("Categoría:");

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtCodigo)
                    .addComponent(txtNombre)
                    .addComponent(txtPrecio)
                    .addComponent(txtExistencia, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                    .addComponent(cmbCategoria, 0, 180, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGuardar)
                    .addComponent(btnActualizar)
                    .addComponent(btnEliminar)
                    .addComponent(btnLimpiar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGuardar))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnActualizar))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtExistencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTableProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No.", "Código", "Nombre", "Precio", "Existencia", "Categoría"
            }
        ));
        jScrollPane1.setViewportView(jTableProductos);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarProducto();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        actualizarProducto();
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminarProducto();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarCampos();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox<Categoria> cmbCategoria;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableProductos;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtExistencia;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}
