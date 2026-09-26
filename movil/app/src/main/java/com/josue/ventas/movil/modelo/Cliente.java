package com.josue.ventas.movil.modelo;

/** Cliente del sistema (la misma tabla que usa la app de escritorio). */
public class Cliente {

    private final String nit;
    private final String nombre;
    private final String direccion;
    private final String telefono;

    public Cliente(String nit, String nombre, String direccion, String telefono) {
        this.nit = nit;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public String getNit() {
        return nit;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }
}
