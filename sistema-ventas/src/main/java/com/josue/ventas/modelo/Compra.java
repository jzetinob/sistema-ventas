/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Compra de mercaderia a un Proveedor (asociacion). Esta compuesta por una
 * lista de CompraDetalle (composicion). Al guardarse, la existencia de cada
 * producto comprado aumenta en la cantidad indicada.
 *
 * @author josue zetino
 */
public class Compra {

    private int id;
    private String numeroCompra;
    private LocalDate fecha;
    private Proveedor proveedor;
    private Usuario usuario;
    private final List<CompraDetalle> detalles = new ArrayList<>();
    private double total;

    public Compra() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroCompra() {
        return numeroCompra;
    }

    public void setNumeroCompra(String numeroCompra) {
        this.numeroCompra = numeroCompra;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<CompraDetalle> getDetalles() {
        return detalles;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void agregarDetalle(CompraDetalle detalle) {
        detalles.add(detalle);
        calcularTotal();
    }

    public void eliminarDetalle(int index) {
        if (index >= 0 && index < detalles.size()) {
            detalles.remove(index);
            calcularTotal();
        }
    }

    public double calcularTotal() {
        double suma = 0;
        for (CompraDetalle d : detalles) {
            suma += d.calcularSubtotal();
        }
        total = suma;
        return total;
    }
}
