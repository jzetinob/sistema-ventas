package com.josue.ventas.movil.modelo;

/**
 * Una linea de factura: sirve para el detalle de una venta ya guardada y para
 * el carrito de una venta nueva (en ese caso guarda tambien el producto).
 */
public class LineaVenta {

    private final String producto;
    private final String codigo;
    private final int cantidad;
    private final double precio;
    private final int existenciaDisponible;

    public LineaVenta(String producto, int cantidad, double precio) {
        this(producto, null, cantidad, precio, 0);
    }

    public LineaVenta(String producto, String codigo, int cantidad, double precio, int existenciaDisponible) {
        this.producto = producto;
        this.codigo = codigo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.existenciaDisponible = existenciaDisponible;
    }

    public String getProducto() {
        return producto;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public int getExistenciaDisponible() {
        return existenciaDisponible;
    }

    public double getSubtotal() {
        return cantidad * precio;
    }
}
