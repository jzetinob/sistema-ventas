/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

/**
 * Validaciones de formato que comparten los formularios.
 *
 * @author josue zetino
 */
public final class Validaciones {

    private Validaciones() {
    }

    /** NIT guatemalteco: de 8 a 13 digitos; guiones y espacios son opcionales. */
    public static boolean nitValido(String nit) {
        return nit != null && nit.replace("-", "").replace(" ", "").matches("\\d{8,13}");
    }

    /** Correo opcional: vacio o con formato nombre@dominio.ext */
    public static boolean correoValido(String correo) {
        return correo == null || correo.isEmpty() || correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    /** Telefono opcional: vacio o de 8 a 15 digitos (se permiten +, guiones y espacios). */
    public static boolean telefonoValido(String telefono) {
        return telefono == null || telefono.isEmpty()
                || telefono.replaceAll("[+\\- ]", "").matches("\\d{8,15}");
    }
}
