/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.UsuarioController;
import com.josue.ventas.modelo.Usuario;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Administracion de usuarios del sistema (solo para administradores).
 * La contrasena se escribe al crear el usuario o con "Cambiar contraseña";
 * "Actualizar" no la modifica.
 *
 * @author josue zetino
 */
public class FrmUsuarios extends FrmCatalogo {

    private final UsuarioController controller = new UsuarioController();
    private final JTextField txtUsuario = new JTextField(25);
    private final JTextField txtNombre = new JTextField(25);
    private final JPasswordField txtClave = new JPasswordField(25);
    private final JComboBox<Usuario.Rol> cmbRol = new JComboBox<>(Usuario.Rol.values());
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    public FrmUsuarios() {
        super("Usuarios del Sistema", "Datos del Usuario", new String[]{"No.", "Usuario", "Nombre", "Rol", "Activo"});
        agregarCampo("Usuario:", txtUsuario);
        agregarCampo("Nombre:", txtNombre);
        agregarCampo("Contraseña:", txtClave);
        agregarCampo("Rol:", cmbRol);
        agregarCampo("", chkActivo);
        agregarBoton("Cambiar contraseña", this::cambiarClave);
        cmbRol.setSelectedItem(Usuario.Rol.VENDEDOR);
        terminarPantalla();
    }

    @Override
    protected void refrescarTabla() {
        modeloTabla.setRowCount(0);
        for (Usuario u : controller.GetUsuarios()) {
            modeloTabla.addRow(new Object[]{u.getId(), u.getUsuario(), u.getNombre(), u.getRol(), u.isActivo() ? "Sí" : "No"});
        }
    }

    @Override
    protected void cargarFila(int fila) {
        txtUsuario.setText((String) modeloTabla.getValueAt(fila, 1));
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 2));
        cmbRol.setSelectedItem(modeloTabla.getValueAt(fila, 3));
        chkActivo.setSelected("Sí".equals(modeloTabla.getValueAt(fila, 4)));
        txtClave.setText("");
    }

    @Override
    protected void guardar() {
        String error = controller.Crear(txtUsuario.getText(), txtNombre.getText(),
                txtClave.getPassword(), (Usuario.Rol) cmbRol.getSelectedItem());
        terminar(error);
    }

    @Override
    protected void actualizar() {
        int id = idSeleccionado("actualizar");
        if (id == -1) {
            return;
        }
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsuario(txtUsuario.getText());
        u.setNombre(txtNombre.getText());
        u.setRol((Usuario.Rol) cmbRol.getSelectedItem());
        u.setActivo(chkActivo.isSelected());
        terminar(controller.Actualizar(u));
    }

    private void cambiarClave() {
        int id = idSeleccionado("cambiarle la contraseña");
        if (id == -1) {
            return;
        }
        terminar(controller.CambiarClave(id, txtClave.getPassword()));
    }

    @Override
    protected void eliminar() {
        int id = idSeleccionado("eliminar");
        if (id == -1 || !confirmar("¿Desea eliminar el usuario seleccionado?")) {
            return;
        }
        terminar(controller.Eliminar(id));
    }

    private void terminar(String error) {
        txtClave.setText("");
        if (error != null) {
            avisar(error);
            return;
        }
        limpiarCampos();
        refrescarTabla();
    }

    @Override
    protected void limpiarCampos() {
        txtUsuario.setText("");
        txtNombre.setText("");
        txtClave.setText("");
        cmbRol.setSelectedItem(Usuario.Rol.VENDEDOR);
        chkActivo.setSelected(true);
    }
}
