package com.josue.ventas.modelo;

/**
 * Linea de una orden de venta: producto, cantidad y precio del catalogo
 * en el momento en que se registro la orden.
 *
 * @author josue zetino
 */
public class OrdenVentaDetalle {

    private Producto producto;
    private int cantidad;
    private double precioUnitario;

    public OrdenVentaDetalle() {
    }

    public OrdenVentaDetalle(Producto producto, int cantidad, double precioUnitario) {
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

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }
}
