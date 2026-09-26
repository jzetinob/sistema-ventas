/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.CategoriaController;
import com.josue.ventas.modelo.Categoria;
import javax.swing.JTextField;

/**
 * Catalogo de categorias de productos (altas, bajas, cambios y busqueda).
 *
 * @author josue zetino
 */
public class FrmCategorias extends FrmCatalogo {

    private final CategoriaController controller = new CategoriaController();
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtDescripcion = new JTextField(25);

    public FrmCategorias() {
        super("Catálogo de Categorías", "Datos de la Categoría", new String[]{"No.", "Nombre", "Descripción"});
        agregarCampo("Nombre:", txtNombre);
        agregarCampo("Descripción:", txtDescripcion);
        terminarPantalla();
    }

    @Override
    protected void refrescarTabla() {
        modeloTabla.setRowCount(0);
        for (Categoria c : controller.GetCategorias()) {
            modeloTabla.addRow(new Object[]{c.getId(), c.getNombre(), c.getDescripcion()});
        }
    }

    @Override
    protected void cargarFila(int fila) {
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 1));
        txtDescripcion.setText((String) modeloTabla.getValueAt(fila, 2));
    }

    @Override
    protected void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            avisar("Escriba el nombre de la categoría.");
            return;
        }
        if (controller.ExisteNombre(nombre)) {
            avisar("Ya existe una categoría con ese nombre.");
            return;
        }
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion(txtDescripcion.getText().trim());
        controller.Guardar(c);
        limpiarCampos();
        refrescarTabla();
    }

    @Override
    protected void actualizar() {
        int id = idSeleccionado("actualizar");
        if (id == -1) {
            return;
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            avisar("Escriba el nombre de la categoría.");
            return;
        }
        String nombreActual = (String) modeloTabla.getValueAt(FiltroTabla.filaDelModelo(tabla), 1);
        if (!nombre.equalsIgnoreCase(nombreActual) && controller.ExisteNombre(nombre)) {
            avisar("Ya existe una categoría con ese nombre.");
            return;
        }
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        c.setDescripcion(txtDescripcion.getText().trim());
        controller.Actualizar(c);
        limpiarCampos();
        refrescarTabla();
    }

    @Override
    protected void eliminar() {
        int id = idSeleccionado("eliminar");
        if (id == -1 || !confirmar("¿Desea eliminar la categoría? Sus productos quedarán sin categoría.")) {
            return;
        }
        if (!controller.Eliminar(id)) {
            avisar("No se pudo eliminar la categoría.");
        }
        limpiarCampos();
        refrescarTabla();
    }

    @Override
    protected void limpiarCampos() {
        txtNombre.setText("");
        txtDescripcion.setText("");
    }
}
