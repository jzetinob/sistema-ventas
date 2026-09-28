/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.ResumenController;
import com.josue.ventas.modelo.Producto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Tablero "Resumen": indicadores del dia y del mes, grafico de barras de
 * las ventas de los ultimos 7 dias, dona con los productos mas vendidos del
 * mes y la lista de productos por agotarse.
 *
 * @author josue zetino
 */
public class FrmResumen extends javax.swing.JInternalFrame {

    private static final int EXISTENCIA_BAJA = 5;
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEE d", Locale.forLanguageTag("es-GT"));

    private final JLabel lblVentasHoy = valorIndicador();
    private final JLabel lblFacturasHoy = valorIndicador();
    private final JLabel lblVentasMes = valorIndicador();
    private final JLabel lblPorAgotarse = valorIndicador();
    private final GraficoBarrasPanel graficoBarras = new GraficoBarrasPanel();
    private final GraficoDonaPanel graficoDona = new GraficoDonaPanel();
    private final JPanel leyenda = new JPanel();
    private final DefaultTableModel modeloAgotandose = new DefaultTableModel(new String[]{"Código", "Producto", "Existencia"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public FrmResumen() {
        super("Resumen", true, true, true, true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JPanel indicadores = new JPanel(new GridLayout(1, 4, 10, 0));
        indicadores.add(tarjeta(lblVentasHoy, "Ventas de hoy"));
        indicadores.add(tarjeta(lblFacturasHoy, "Facturas de hoy"));
        indicadores.add(tarjeta(lblVentasMes, "Ventas del mes"));
        indicadores.add(tarjeta(lblPorAgotarse, "Productos por agotarse"));

        JPanel barras = seccion("Ventas de los últimos 7 días");
        barras.add(graficoBarras, BorderLayout.CENTER);

        leyenda.setLayout(new BoxLayout(leyenda, BoxLayout.Y_AXIS));
        leyenda.setOpaque(false);
        JPanel dona = seccion("Más vendidos del mes");
        dona.add(graficoDona, BorderLayout.WEST);
        dona.add(leyenda, BorderLayout.CENTER);

        JPanel graficos = new JPanel(new GridLayout(1, 2, 10, 0));
        graficos.add(barras);
        graficos.add(dona);

        JPanel agotandose = seccion("Productos por agotarse (existencia de " + EXISTENCIA_BAJA + " o menos)");
        JScrollPane scroll = new JScrollPane(new JTable(modeloAgotandose));
        scroll.setPreferredSize(new Dimension(600, 130));
        agotandose.add(scroll, BorderLayout.CENTER);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botones.add(btnActualizar);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.add(graficos, BorderLayout.CENTER);
        centro.add(agotandose, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenido.add(indicadores, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);

        cargar();
        pack();
    }

    private void cargar() {
        ResumenController resumen = new ResumenController(LocalDate.now());
        lblVentasHoy.setText(dinero(resumen.VentasDelDia()));
        lblFacturasHoy.setText(String.valueOf(resumen.FacturasDelDia()));
        lblVentasMes.setText(dinero(resumen.VentasDelMes()));

        List<String> dias = new ArrayList<>();
        List<Double> totales = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> e : resumen.VentasPorDia(7).entrySet()) {
            dias.add(e.getKey().format(DIA).replace(".", ""));
            totales.add(e.getValue());
        }
        graficoBarras.setDatos(dias, totales, dias.size() - 1);

        List<ResumenController.ProductoVendido> top = resumen.MasVendidosDelMes(5);
        List<Double> cantidades = new ArrayList<>();
        int unidades = 0;
        leyenda.removeAll();
        for (int i = 0; i < top.size(); i++) {
            ResumenController.ProductoVendido p = top.get(i);
            cantidades.add((double) p.cantidad);
            unidades += p.cantidad;
            JLabel renglon = new JLabel("<html><font color='" + hex(GraficoDonaPanel.colorDe(i)) + "'>&#9632;</font> "
                    + ReporteHtml.escapar(p.producto) + " — <b>" + p.cantidad + " u.</b> · " + dinero(p.total) + "</html>");
            renglon.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 0));
            leyenda.add(renglon);
        }
        if (top.isEmpty()) {
            leyenda.add(new JLabel("  Todavía no hay ventas este mes."));
        }
        graficoDona.setDatos(cantidades, String.valueOf(unidades), "unidades");
        leyenda.revalidate();
        leyenda.repaint();

        List<Producto> baja = resumen.ExistenciaBaja(EXISTENCIA_BAJA);
        lblPorAgotarse.setText(String.valueOf(baja.size()));
        lblPorAgotarse.setForeground(baja.isEmpty() ? new Color(0x1E8449) : new Color(0xC0392B));
        modeloAgotandose.setRowCount(0);
        for (Producto p : baja) {
            modeloAgotandose.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getExistencia() == 0 ? "Agotado" : p.getExistencia()});
        }
    }

    private static JLabel valorIndicador() {
        JLabel l = new JLabel("—");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 20f));
        l.setForeground(new Color(0x2F4F6F));
        return l;
    }

    private static JPanel tarjeta(JLabel valor, String etiqueta) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xDDE3EA)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel texto = new JLabel(etiqueta);
        texto.setForeground(GraficoBarrasPanel.TEXTO_SUAVE);
        p.add(valor, BorderLayout.CENTER);
        p.add(texto, BorderLayout.SOUTH);
        return p;
    }

    private static JPanel seccion(String titulo) {
        JPanel p = new JPanel(new BorderLayout(10, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        return p;
    }

    private static String dinero(double v) {
        return String.format(Locale.US, "Q %,.2f", v);
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
