/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

/**
 * Empleado hereda de Persona los atributos comunes y agrega el codigo de
 * empleado y el puesto. Sobreescribe mostrarInformacion() para incluir
 * la informacion propia del empleado.
 *
 * @author josue zetino
 */
public class Empleado extends Persona {

    private String codigoEmpleado;
    private String puesto;

    public Empleado() {
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    @Override
    public String mostrarInformacion() {
        return "Empleado [id=" + id + ", nombre=" + nombre + ", nit=" + nit
                + ", telefono=" + telefono + ", codigoEmpleado=" + codigoEmpleado
                + ", puesto=" + puesto + "]";
    }
}