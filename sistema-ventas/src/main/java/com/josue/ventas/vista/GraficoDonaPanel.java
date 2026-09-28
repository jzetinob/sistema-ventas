/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Grafico de dona dibujado con Graphics2D: cada porcion es un arco
 * proporcional a su valor, y el total va en el centro. Version de
 * escritorio del GraficoDona de la app movil.
 *
 * @author josue zetino
 */
public class GraficoDonaPanel extends JPanel {

    static final Color[] COLORES = {
        new Color(0x3C7DBA), new Color(0xE08E2B), new Color(0x2E9E6B), new Color(0x9B59B6), new Color(0xD4526E)
    };

    private final List<Double> valores = new ArrayList<>();
    private String textoCentro = "";
    private String etiquetaCentro = "";

    public GraficoDonaPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 200));
    }

    static Color colorDe(int i) {
        return COLORES[i % COLORES.length];
    }

    public void setDatos(List<Double> valores, String textoCentro, String etiquetaCentro) {
        this.valores.clear();
        this.valores.addAll(valores);
        this.textoCentro = textoCentro;
        this.etiquetaCentro = etiquetaCentro;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int lado = Math.min(getWidth(), getHeight()) - 8;
        float grosor = lado * 0.16f;
        double radio = lado / 2.0 - grosor / 2;
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        double total = 0;
        for (double v : valores) {
            total += v;
        }
        Arc2D.Double arco = new Arc2D.Double(cx - radio, cy - radio, radio * 2, radio * 2, 0, 0, Arc2D.OPEN);
        if (total <= 0) {
            g2.setColor(GraficoBarrasPanel.REJILLA);
            arco.setAngleStart(0);
            arco.setAngleExtent(360);
            g2.draw(arco);
        } else {
            double inicio = 90; // en Java 2D los angulos van contra reloj desde las 3 en punto
            double separacion = valores.size() > 1 ? 1.5 : 0;
            for (int i = 0; i < valores.size(); i++) {
                double barrido = valores.get(i) / total * 360;
                g2.setColor(colorDe(i));
                arco.setAngleStart(inicio);
                arco.setAngleExtent(-Math.max(barrido - separacion, 0.5)); // negativo = sentido del reloj
                g2.draw(arco);
                inicio -= barrido;
            }
        }
        g2.setColor(GraficoBarrasPanel.TEXTO);
        g2.setFont(getFont().deriveFont(Font.BOLD, 20f));
        int w = g2.getFontMetrics().stringWidth(textoCentro);
        g2.drawString(textoCentro, (int) (cx - w / 2.0), (int) cy + 4);
        g2.setColor(GraficoBarrasPanel.TEXTO_SUAVE);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        w = g2.getFontMetrics().stringWidth(etiquetaCentro);
        g2.drawString(etiquetaCentro, (int) (cx - w / 2.0), (int) cy + 20);
        g2.dispose();
    }
}
