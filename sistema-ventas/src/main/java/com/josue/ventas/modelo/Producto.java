/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

/**
 * Producto del catalogo del sistema de ventas. Ademas de su identificador,
 * nombre y precio, controla la existencia en el inventario y permite saber
 * si hay suficiente stock para una venta.
 *
 * @author josue zetino
 */
public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private double precio;
    private int existencia;

    public Producto() {
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getId() {
        return idProducto;
    }

    public void setId(int id) {
        this.idProducto = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public boolean hayExistencia(int cantidad) {
        return cantidad >= 0 && cantidad <= existencia;
    }
}