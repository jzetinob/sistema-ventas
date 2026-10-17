/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Cliente hereda de Persona los datos personales (id, nombre, nit, telefono)
 * y agrega la direccion. Una persona con rol de cliente puede tener muchas
 * facturas asociadas (asociacion 1..* con Factura).
 *
 * @author josue zetino
 */
public class Cliente extends Persona {

    private String direccion;
    private List<Telefono> telefonos = new ArrayList<>();

    public Cliente() {
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<Telefono> getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(List<Telefono> telefonos) {
        this.telefonos = telefonos;
    }
}