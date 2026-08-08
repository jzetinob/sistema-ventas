/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

/**
 * Clase abstracta base del diagrama de clases: modela los datos comunes
 * de toda persona (id, nombre, nit y telefono). De esta clase heredan
 * Cliente y Empleado.
 *
 * @author josue zetino
 */
public abstract class Persona {

    protected int id;
    protected String nombre;
    protected String nit;
    protected String telefono;

    public Persona() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String mostrarInformacion() {
        return "Persona [id=" + id + ", nombre=" + nombre + ", nit=" + nit
                + ", telefono=" + telefono + "]";
    }
}