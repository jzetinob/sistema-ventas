package com.josue.ventas.movil.modelo;

/** Producto del catalogo, tal como lo devuelve la funcion app_productos. */
public class Producto {

    public static final int EXISTENCIA_BAJA = 5;

    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final double precio;
    private final int existencia;

    public Producto(String codigo, String nombre, String categoria, double precio, int existencia) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.existencia = existencia;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public boolean tieneExistenciaBaja() {
        return existencia <= EXISTENCIA_BAJA;
    }
}
