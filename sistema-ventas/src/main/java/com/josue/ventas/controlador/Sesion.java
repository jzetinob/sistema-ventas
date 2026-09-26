/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.modelo.Usuario;

/**
 * Sesion del usuario que inicio sesion (patron singleton): hay una sola
 * sesion activa por ejecucion del programa.
 *
 * @author josue zetino
 */
public class Sesion {

    private static final Sesion instancia = new Sesion();

    private Usuario usuario;

    private Sesion() {
    }

    public static Sesion getInstancia() {
        return instancia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    void iniciar(Usuario usuario) {
        this.usuario = usuario;
    }

    public void cerrar() {
        usuario = null;
    }

    public boolean esAdministrador() {
        return usuario != null && usuario.esAdministrador();
    }
}
