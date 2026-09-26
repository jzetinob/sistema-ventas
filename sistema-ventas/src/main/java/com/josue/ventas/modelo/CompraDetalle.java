/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

/**
 * Linea de una Compra: que producto se compro, cuantas unidades y a que
 * costo. Su ciclo de vida esta ligado al de la Compra (composicion) y se
 * asocia con un Producto.
 *
 * @author josue zetino
 */
public class CompraDetalle {

    private Producto producto;
    private int cantidad;
    private double costoUnitario;

    public CompraDetalle() {
    }

    public CompraDetalle(Producto producto, int cantidad, double costoUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
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

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public double calcularSubtotal() {
        return cantidad * costoUnitario;
    }
}
