package com.josue.ventas.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de venta (pedido) que se crea desde la app movil por la API REST.
 * Se compone de sus detalles. No descuenta existencia: queda PENDIENTE
 * hasta que en el escritorio se confirma o se anula.
 *
 * @author josue zetino
 */
public class OrdenVenta {

    public enum Estado {
        PENDIENTE, CONFIRMADA, ANULADA
    }

    private int id;
    private String numeroOrden;
    private Cliente cliente;
    private Usuario usuario;
    private LocalDateTime fecha;
    private Estado estado = Estado.PENDIENTE;
    private String observaciones;
    private double total;
    private List<OrdenVentaDetalle> detalles = new ArrayList<>();

    public OrdenVenta() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(String numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<OrdenVentaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<OrdenVentaDetalle> detalles) {
        this.detalles = detalles;
    }

    public boolean estaPendiente() {
        return estado == Estado.PENDIENTE;
    }
}
