/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Formulario base (clase abstracta) para los catalogos de altas, bajas y
 * cambios. Arma la misma pantalla para todos: datos arriba, botones a la
 * derecha, buscador y tabla abajo. Cada catalogo concreto solo agrega sus
 * campos e implementa las operaciones (patron Template Method).
 *
 * @author josue zetino
 */
public abstract class FrmCatalogo extends javax.swing.JInternalFrame {

    protected final DefaultTableModel modeloTabla;
    protected final JTable tabla = new JTable();
    protected final JTextField txtBuscar = new JTextField(20);

    private final JPanel panelCampos = new JPanel(new GridBagLayout());
    private final JPanel panelBotones = new JPanel(new GridBagLayout());
    private int filaCampos = 0;
    private int filaBotones = 0;

    protected FrmCatalogo(String titulo, String tituloDatos, String[] columnas) {
        super(titulo, true, true, true, true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setModel(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        FiltroTabla.instalar(txtBuscar, tabla);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = FiltroTabla.filaDelModelo(tabla);
            if (!e.getValueIsAdjusting() && fila != -1) {
                cargarFila(fila);
            }
        });

        agregarBoton("Guardar", this::guardar);
        agregarBoton("Actualizar", this::actualizar);
        agregarBoton("Eliminar", this::eliminar);
        agregarBoton("Limpiar", this::limpiarTodo);

        JPanel datos = new JPanel(new BorderLayout(18, 0));
        datos.setBorder(BorderFactory.createTitledBorder(tituloDatos));
        datos.add(panelCampos, BorderLayout.CENTER);
        datos.add(panelBotones, BorderLayout.EAST);

        JPanel buscador = new JPanel(new BorderLayout(8, 0));
        buscador.add(new JLabel("Buscar:"), BorderLayout.WEST);
        buscador.add(txtBuscar, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.add(buscador, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new java.awt.Dimension(600, 220));
        abajo.add(scroll, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.add(datos, BorderLayout.NORTH);
        contenido.add(abajo, BorderLayout.CENTER);
        setContentPane(contenido);
    }

    /** Agrega una fila "Etiqueta: campo" al panel de datos. */
    protected void agregarCampo(String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = filaCampos++;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.LINE_START;
        panelCampos.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panelCampos.add(campo, c);
    }

    /** Agrega un boton a la columna de botones de la derecha. */
    protected JButton agregarBoton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> accion.run());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = filaBotones++;
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        panelBotones.add(boton, c);
        return boton;
    }

    /** Llamar al final del constructor de la subclase. */
    protected void terminarPantalla() {
        refrescarTabla();
        pack();
    }

    /** Id (columna 0) de la fila seleccionada, o -1 mostrando un aviso. */
    protected int idSeleccionado(String accion) {
        int fila = FiltroTabla.filaDelModelo(tabla);
        if (fila == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un registro para " + accion + ".");
            return -1;
        }
        return (int) modeloTabla.getValueAt(fila, 0);
    }

    protected boolean confirmar(String pregunta) {
        return javax.swing.JOptionPane.showConfirmDialog(this, pregunta, getTitle(),
                javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE)
                == javax.swing.JOptionPane.YES_OPTION;
    }

    protected void avisar(String mensaje) {
        javax.swing.JOptionPane.showMessageDialog(this, mensaje);
    }

    private void limpiarTodo() {
        tabla.clearSelection();
        limpiarCampos();
    }

    protected abstract void refrescarTabla();

    protected abstract void cargarFila(int filaModelo);

    protected abstract void guardar();

    protected abstract void actualizar();

    protected abstract void eliminar();

    protected abstract void limpiarCampos();
}
