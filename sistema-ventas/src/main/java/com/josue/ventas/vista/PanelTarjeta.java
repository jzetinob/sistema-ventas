/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel con forma de tarjeta: fondo redondeado con borde, pintado con los
 * colores del tema actual (claro u oscuro). Opcionalmente lleva un titulo.
 * Es el equivalente de escritorio de las tarjetas de la app movil.
 *
 * @author josue zetino
 */
public class PanelTarjeta extends JPanel {

    private static final int RADIO = 14;
    private final JLabel titulo;

    public PanelTarjeta(String textoTitulo) {
        super(new BorderLayout(10, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        if (textoTitulo != null) {
            titulo = new JLabel(textoTitulo);
            titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, titulo.getFont().getSize2D() + 1f));
            add(titulo, BorderLayout.NORTH);
        } else {
            titulo = null;
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (titulo != null) {
            titulo.setForeground(Tema.texto()); // al cambiar de tema
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.fondoTarjeta());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
        g2.setColor(Tema.borde());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
        g2.dispose();
        super.paintComponent(g);
    }
}
