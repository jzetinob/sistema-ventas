package com.josue.ventas.movil.modelo;

/** Factura registrada en el sistema de escritorio (resumen para consultar). */
public class Venta {

    private final String numeroFactura;
    private final String fecha;
    private final String cliente;
    private final double total;

    public Venta(String numeroFactura, String fecha, String cliente, double total) {
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.cliente = cliente;
        this.total = total;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public String getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public double getTotal() {
        return total;
    }
}
