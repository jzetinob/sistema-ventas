/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Busqueda en cualquier tabla de catalogo: mientras se escribe en el campo,
 * la tabla muestra solo las filas que contienen el texto en alguna columna.
 * Tambien permite ordenar haciendo clic en los encabezados.
 *
 * Como la tabla puede estar filtrada u ordenada, para leer la fila
 * seleccionada hay que usar filaDelModelo().
 *
 * @author josue zetino
 */
public final class FiltroTabla {

    private FiltroTabla() {
    }

    public static void instalar(JTextField campo, JTable tabla) {
        TableRowSorter<TableModel> ordenador = new TableRowSorter<>(tabla.getModel());
        tabla.setRowSorter(ordenador);
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicar();
            }

            private void aplicar() {
                String texto = campo.getText().trim();
                // Pattern.quote: el texto se busca literal, sin interpretarse como expresion regular
                ordenador.setRowFilter(texto.isEmpty() ? null
                        : RowFilter.regexFilter("(?iu)" + Pattern.quote(texto)));
            }
        });
    }

    /** Indice en el modelo de la fila seleccionada, o -1 si no hay seleccion. */
    public static int filaDelModelo(JTable tabla) {
        int fila = tabla.getSelectedRow();
        return fila == -1 ? -1 : tabla.convertRowIndexToModel(fila);
    }
}
