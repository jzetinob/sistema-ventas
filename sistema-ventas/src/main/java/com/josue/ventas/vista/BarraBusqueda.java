/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import java.awt.BorderLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Agrega sobre la tabla de un formulario una barra con "Buscar" y un boton
 * "Reporte HTML" que exporta las filas visibles. Sirve para los formularios
 * hechos con el editor de NetBeans (GroupLayout) sin rehacer su diseno:
 * se reemplaza el JScrollPane de la tabla por un panel que lo contiene.
 *
 * @author josue zetino
 */
public final class BarraBusqueda {

    private BarraBusqueda() {
    }

    /** Crea el panel "Buscar: [____] [Reporte HTML]" + tabla. */
    public static JPanel crear(JTable tabla, JComponent scrollTabla, String tituloReporte) {
        JTextField txtBuscar = new JTextField(20);
        FiltroTabla.instalar(txtBuscar, tabla);
        JButton btnReporte = new JButton("Reporte HTML");
        btnReporte.addActionListener(e -> ReporteHtml.desdeTabla(tituloReporte, tabla).abrir(tabla));

        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.add(new JLabel("Buscar:"), BorderLayout.WEST);
        barra.add(txtBuscar, BorderLayout.CENTER);
        barra.add(btnReporte, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(barra, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        return panel;
    }

    /** Version para formularios de NetBeans: reemplaza el scroll de la tabla dentro del GroupLayout. */
    public static void instalar(javax.swing.JInternalFrame frame, JTable tabla, JScrollPane scrollTabla, String tituloReporte) {
        GroupLayout layout = (GroupLayout) frame.getContentPane().getLayout();
        JPanel panel = new JPanel(new BorderLayout());
        layout.replace(scrollTabla, panel);
        panel.add(crear(tabla, scrollTabla, tituloReporte), BorderLayout.CENTER);
        frame.pack();
    }
}
