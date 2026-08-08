/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.modelo.Factura;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author josue zetino
 */
public class TicketFactura implements Printable {

    private final Factura factura;

    public TicketFactura(Factura factura) {
        this.factura = factura;
    }

    public void pintar(Graphics2D g2, float ancho) {
        Font fuenteTitulo = new Font("Monospaced", Font.BOLD, 14);
        Font fuenteNormal = new Font("Monospaced", Font.PLAIN, 11);
        Font fuenteTotal = new Font("Monospaced", Font.BOLD, 12);

        float xProducto = 10;
        float xCantidad = ancho * 0.45f;
        float xPrecio = ancho * 0.62f;
        float xSubtotal = ancho * 0.78f;
        float y = 30;

        g2.setFont(fuenteTitulo);
        String titulo = "SISTEMA DE VENTAS";
        g2.drawString(titulo, centrar(g2, titulo, ancho), y);
        y += 25;

        g2.setFont(fuenteNormal);
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String numero = "No. Factura: " + (factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "");
        String fecha = "Fecha: " + (factura.getFecha() != null ? factura.getFecha().format(sdf) : "");
        g2.drawString(numero, xProducto, y);
        g2.drawString(fecha, xProducto, y + 14);
        y += 35;

        g2.drawString("Cliente: " + (factura.getNombreCliente() != null ? factura.getNombreCliente() : ""), xProducto, y);
        g2.drawString("NIT: " + (factura.getNit() != null ? factura.getNit() : ""), xProducto, y + 14);
        y += 40;

        g2.drawLine((int) xProducto, (int) y, (int) (ancho - xProducto), (int) y);
        y += 18;

        g2.setFont(fuenteTotal);
        g2.drawString("PRODUCTO", xProducto, y);
        g2.drawString("CANT", xCantidad, y);
        g2.drawString("PRECIO", xPrecio, y);
        g2.drawString("SUBTOTAL", xSubtotal, y);
        y += 16;

        g2.setFont(fuenteNormal);
        for (Object[] fila : factura.getDetallesFilas()) {
            String producto = String.valueOf(fila[0]);
            if (producto.length() > 28) {
                producto = producto.substring(0, 27) + "...";
            }
            g2.drawString(producto, xProducto, y);
            g2.drawString(String.valueOf(fila[1]), xCantidad, y);
            g2.drawString(String.format("%.2f", (Double) fila[2]), xPrecio, y);
            g2.drawString(String.format("%.2f", (Double) fila[3]), xSubtotal, y);
            y += 14;
        }

        y += 6;
        g2.drawLine((int) xProducto, (int) y, (int) (ancho - xProducto), (int) y);
        y += 20;

        g2.setFont(fuenteTotal);
        String total = "TOTAL: " + String.format("%.2f", factura.getTotal());
        g2.drawString(total, ancho - xProducto - g2.getFontMetrics().stringWidth(total), y);
        y += 30;

        g2.setFont(fuenteNormal);
        String gracias = "Gracias por su compra";
        g2.drawString(gracias, centrar(g2, gracias, ancho), y);
    }

    private float centrar(Graphics2D g2, String texto, float ancho) {
        return (ancho - g2.getFontMetrics().stringWidth(texto)) / 2f;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        pintar(g2, (float) pageFormat.getImageableWidth());
        return PAGE_EXISTS;
    }
}
