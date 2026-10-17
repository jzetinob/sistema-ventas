package com.josue.ventas.vista;

import com.josue.ventas.modelo.Telefono;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Componente para la lista de telefonos de un cliente: agregar, modificar y
 * quitar telefonos, sin limite de cantidad. Los cambios quedan en memoria
 * hasta que el formulario guarda o actualiza el cliente.
 *
 * @author josue zetino
 */
public class PanelTelefonos extends JPanel {

    private final List<Telefono> telefonos = new ArrayList<>();
    private final JTextField txtNumero = new JTextField(10);
    private final JComboBox<Telefono.Tipo> cmbTipo = new JComboBox<>(Telefono.Tipo.values());
    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnQuitar = new JButton("Quitar");
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Número", "Tipo"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public PanelTelefonos() {
        super(new BorderLayout(0, 4));
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        fila.add(txtNumero);
        fila.add(cmbTipo);
        fila.add(btnAgregar);
        fila.add(btnModificar);
        fila.add(btnQuitar);
        add(fila, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(380, 90));
        add(scroll, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregar());
        btnModificar.addActionListener(e -> modificar());
        btnQuitar.addActionListener(e -> quitar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionado();
            }
        });
    }

    /** Muestra los telefonos de un cliente (al seleccionarlo en la tabla). */
    public void setTelefonos(List<Telefono> lista) {
        telefonos.clear();
        telefonos.addAll(lista);
        refrescar();
    }

    /** Devuelve la lista para guardarla junto con el cliente. */
    public List<Telefono> getTelefonos() {
        return new ArrayList<>(telefonos);
    }

    public void limpiar() {
        telefonos.clear();
        refrescar();
    }

    private void agregar() {
        String numero = txtNumero.getText().trim();
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba el número de teléfono.");
            return;
        }
        telefonos.add(new Telefono(numero, (Telefono.Tipo) cmbTipo.getSelectedItem()));
        refrescar();
    }

    private void modificar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un teléfono de la lista.");
            return;
        }
        String numero = txtNumero.getText().trim();
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba el número de teléfono.");
            return;
        }
        Telefono telefono = telefonos.get(fila);
        telefono.setNumero(numero);
        telefono.setTipo((Telefono.Tipo) cmbTipo.getSelectedItem());
        refrescar();
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un teléfono de la lista.");
            return;
        }
        telefonos.remove(fila);
        refrescar();
    }

    private void cargarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        Telefono telefono = telefonos.get(fila);
        txtNumero.setText(telefono.getNumero());
        cmbTipo.setSelectedItem(telefono.getTipo());
    }

    private void refrescar() {
        modelo.setRowCount(0);
        for (Telefono t : telefonos) {
            modelo.addRow(new Object[]{t.getNumero(), t.getTipo()});
        }
        txtNumero.setText("");
        cmbTipo.setSelectedIndex(0);
    }
}
