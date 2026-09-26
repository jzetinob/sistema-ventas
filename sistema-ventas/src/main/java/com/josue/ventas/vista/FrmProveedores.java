/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.ProveedorController;
import com.josue.ventas.modelo.Proveedor;
import javax.swing.JTextField;

/**
 * Catalogo de proveedores (altas, bajas, cambios y busqueda).
 *
 * @author josue zetino
 */
public class FrmProveedores extends FrmCatalogo {

    private final ProveedorController controller = new ProveedorController();
    private final JTextField txtNit = new JTextField(25);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtDireccion = new JTextField(25);
    private final JTextField txtTelefono = new JTextField(25);
    private final JTextField txtCorreo = new JTextField(25);

    public FrmProveedores() {
        super("Catálogo de Proveedores", "Datos del Proveedor",
                new String[]{"No.", "NIT", "Nombre", "Dirección", "Teléfono", "Correo"});
        agregarCampo("NIT:", txtNit);
        agregarCampo("Nombre:", txtNombre);
        agregarCampo("Dirección:", txtDireccion);
        agregarCampo("Teléfono:", txtTelefono);
        agregarCampo("Correo:", txtCorreo);
        terminarPantalla();
    }

    @Override
    protected void refrescarTabla() {
        modeloTabla.setRowCount(0);
        for (Proveedor p : controller.GetProveedores()) {
            modeloTabla.addRow(new Object[]{p.getId(), p.getNit(), p.getNombre(), p.getDireccion(), p.getTelefono(), p.getCorreo()});
        }
    }

    @Override
    protected void cargarFila(int fila) {
        txtNit.setText((String) modeloTabla.getValueAt(fila, 1));
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 2));
        txtDireccion.setText((String) modeloTabla.getValueAt(fila, 3));
        txtTelefono.setText((String) modeloTabla.getValueAt(fila, 4));
        txtCorreo.setText((String) modeloTabla.getValueAt(fila, 5));
    }

    /** Arma el proveedor con los datos del formulario, o devuelve null si algo no es valido. */
    private Proveedor leerFormulario() {
        Proveedor p = new Proveedor();
        p.setNit(txtNit.getText().trim());
        p.setNombre(txtNombre.getText().trim());
        p.setDireccion(txtDireccion.getText().trim());
        p.setTelefono(txtTelefono.getText().trim());
        p.setCorreo(txtCorreo.getText().trim());
        if (p.getNit().isEmpty() || p.getNombre().isEmpty()) {
            avisar("Complete al menos el NIT y el nombre del proveedor.");
            return null;
        }
        if (!Validaciones.nitValido(p.getNit())) {
            avisar("El NIT debe tener entre 8 y 13 dígitos (los guiones son opcionales).");
            return null;
        }
        if (!Validaciones.telefonoValido(p.getTelefono())) {
            avisar("El teléfono debe tener entre 8 y 15 dígitos.");
            return null;
        }
        if (!Validaciones.correoValido(p.getCorreo())) {
            avisar("El correo no tiene un formato válido (ejemplo: ventas@proveedor.com).");
            return null;
        }
        return p;
    }

    @Override
    protected void guardar() {
        Proveedor p = leerFormulario();
        if (p == null) {
            return;
        }
        if (controller.ExisteNit(p.getNit())) {
            avisar("Ya existe un proveedor con ese NIT.");
            return;
        }
        controller.Guardar(p);
        limpiarCampos();
        refrescarTabla();
    }

    @Override
    protected void actualizar() {
        int id = idSeleccionado("actualizar");
        if (id == -1) {
            return;
        }
        Proveedor p = leerFormulario();
        if (p == null) {
            return;
        }
        String nitActual = (String) modeloTabla.getValueAt(FiltroTabla.filaDelModelo(tabla), 1);
        if (!p.getNit().equals(nitActual) && controller.ExisteNit(p.getNit())) {
            avisar("Ya existe un proveedor con ese NIT.");
            return;
        }
        p.setId(id);
        controller.Actualizar(p);
        limpiarCampos();
        refrescarTabla();
    }

    @Override
    protected void eliminar() {
        int id = idSeleccionado("eliminar");
        if (id == -1 || !confirmar("¿Desea eliminar el proveedor seleccionado?")) {
            return;
        }
        if (!controller.Eliminar(id)) {
            avisar("No se puede eliminar: el proveedor tiene compras registradas.");
            return;
        }
        limpiarCampos();
        refrescarTabla();
    }

    @Override
    protected void limpiarCampos() {
        txtNit.setText("");
        txtNombre.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
    }
}
