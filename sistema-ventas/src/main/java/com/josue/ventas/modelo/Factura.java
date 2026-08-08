/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Factura del sistema de ventas. Modela la relacion del diagrama de clases:
 * una Factura tiene asociado un Cliente (asociación) y está compuesta por
 * una lista de DetalleFactura (composición): los detalles se crean y se
 * destruyen con la factura.
 *
 * @author josue zetino
 */
public class Factura {

    private int idFactura;
    private LocalDate fecha;
    private String numeroFactura;
    private Cliente cliente;
    private List<FacturaDetalle> detalles;
    private double total;

    public Factura() {
        this.detalles = new ArrayList<>();
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public int getId() {
        return idFactura;
    }

    public void setId(int id) {
        this.idFactura = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getNombreCliente() {
        return cliente != null ? cliente.getNombre() : null;
    }

    public void setNombreCliente(String nombreCliente) {
        if (cliente == null) {
            cliente = new Cliente();
        }
        cliente.setNombre(nombreCliente);
    }

    public String getNit() {
        return cliente != null ? cliente.getNit() : null;
    }

    public void setNit(String nit) {
        if (cliente == null) {
            cliente = new Cliente();
        }
        cliente.setNit(nit);
    }

    public List<FacturaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<FacturaDetalle> detalles) {
        this.detalles = detalles;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Object[][] getDetallesFilas() {
        if (detalles == null || detalles.isEmpty()) {
            return new Object[0][0];
        }
        Object[][] filas = new Object[detalles.size()][4];
        for (int i = 0; i < detalles.size(); i++) {
            FacturaDetalle d = detalles.get(i);
            filas[i][0] = d.getProducto() != null ? d.getProducto().getNombre() : "";
            filas[i][1] = d.getCantidad();
            filas[i][2] = d.getPrecioUnitario();
            filas[i][3] = d.calcularSubtotal();
        }
        return filas;
    }

    public void agregarDetalle(FacturaDetalle detalle) {
        this.detalles.add(detalle);
        calcularTotal();
    }

    public void agregarDetalle(String nombreProducto, int cantidad, double precio) {
        Producto producto = new Producto();
        producto.setNombre(nombreProducto);
        FacturaDetalle detalle = new FacturaDetalle(producto, cantidad, precio);
        this.detalles.add(detalle);
        calcularTotal();
    }

    public void eliminarDetalle(int index) {
        if (index >= 0 && index < this.detalles.size()) {
            this.detalles.remove(index);
            calcularTotal();
        }
    }

    public double calcularTotal() {
        double suma = 0;
        for (FacturaDetalle d : this.detalles) {
            suma += d.calcularSubtotal();
        }
        this.total = suma;
        return total;
    }
}