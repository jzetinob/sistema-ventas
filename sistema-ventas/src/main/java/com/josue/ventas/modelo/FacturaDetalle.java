/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

/**
 * DetalleFactura representa una linea del ticket de una factura.
 * Tiene una asociacion con Producto y su ciclo de vida esta ligado al de
 * la Factura (composicion): los detalles se crean y destruyen dentro de la
 * Factura.
 *
 * @author josue zetino
 */
public class FacturaDetalle {

    private Producto producto;
    private int cantidad;
    private double precioUnitario;

    public FacturaDetalle() {
    }

    public FacturaDetalle(Producto producto, int cantidad, double precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getPrecio() {
        return precioUnitario;
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    public double getSubtotal() {
        return calcularSubtotal();
    }
}