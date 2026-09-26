/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

/**
 * Proveedor hereda de Persona (id, nombre, nit, telefono) y agrega la
 * direccion y el correo. Sobreescribe mostrarInformacion() (polimorfismo).
 * Un Proveedor se asocia a las Compras que se le hacen.
 *
 * @author josue zetino
 */
public class Proveedor extends Persona {

    private String direccion;
    private String correo;

    public Proveedor() {
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public String mostrarInformacion() {
        return "Proveedor [id=" + id + ", nombre=" + nombre + ", nit=" + nit
                + ", telefono=" + telefono + ", direccion=" + direccion
                + ", correo=" + correo + "]";
    }
}
