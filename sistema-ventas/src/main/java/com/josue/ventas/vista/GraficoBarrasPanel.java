/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Grafico de barras dibujado con Graphics2D (sin librerias): se sobrescribe
 * paintComponent(), que Swing llama cada vez que hay que pintar el panel.
 * Es la version de escritorio del GraficoBarras de la app movil.
 *
 * @author josue zetino
 */
public class GraficoBarrasPanel extends JPanel {

    static final Color BARRA = new Color(0x3C7DBA);
    static final Color BARRA_DESTACADA = new Color(0xE08E2B);
    static final Color REJILLA = new Color(0xE6EAF0);
    static final Color TEXTO = new Color(0x1D2733);
    static final Color TEXTO_SUAVE = new Color(0x5F6B78);

    private final List<String> etiquetas = new ArrayList<>();
    private final List<Double> valores = new ArrayList<>();
    private int destacada = -1;

    public GraficoBarrasPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(420, 220));
    }

    public void setDatos(List<String> etiquetas, List<Double> valores, int destacada) {
        this.etiquetas.clear();
        this.etiquetas.addAll(etiquetas);
        this.valores.clear();
        this.valores.addAll(valores);
        this.destacada = destacada;
        repaint(); // pide a Swing que vuelva a llamar paintComponent()
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int n = valores.size();
        if (n == 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int ancho = getWidth();
        int alto = getHeight();
        int arriba = 22;
        int abajo = 24;
        int area = alto - arriba - abajo;
        double maximo = 0;
        for (double v : valores) {
            maximo = Math.max(maximo, v);
        }

        g2.setColor(REJILLA);
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 2; i++) {
            int y = arriba + area * i / 2;
            g2.drawLine(0, y, ancho, y);
        }
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        FontMetrics fm = g2.getFontMetrics();
        if (maximo <= 0) {
            g2.setColor(TEXTO_SUAVE);
            String texto = "Sin ventas en estos días";
            g2.drawString(texto, (ancho - fm.stringWidth(texto)) / 2, arriba + area / 2 - 6);
        }

        double espacio = (double) ancho / n;
        int anchoBarra = (int) (espacio * 0.56);
        for (int i = 0; i < n; i++) {
            int centro = (int) (espacio * i + espacio / 2);
            double v = valores.get(i);
            int altoBarra = maximo > 0 ? (int) Math.round(v / maximo * area) : 0;
            int base = arriba + area;
            if (altoBarra > 0) {
                g2.setColor(i == destacada ? BARRA_DESTACADA : BARRA);
                g2.fillRoundRect(centro - anchoBarra / 2, base - altoBarra, anchoBarra, altoBarra, 8, 8);
                String valor = compacto(v);
                g2.setColor(TEXTO);
                g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
                g2.drawString(valor, centro - g2.getFontMetrics().stringWidth(valor) / 2, base - altoBarra - 5);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            }
            String etiqueta = etiquetas.get(i);
            g2.setColor(TEXTO_SUAVE);
            g2.drawString(etiqueta, centro - fm.stringWidth(etiqueta) / 2, alto - 6);
        }
        g2.dispose();
    }

    /** 1250 -> "1.3k", para que el valor quepa encima de la barra. */
    static String compacto(double v) {
        return v >= 1000 ? String.format(java.util.Locale.US, "%.1fk", v / 1000) : String.format(java.util.Locale.US, "%.0f", v);
    }
}
