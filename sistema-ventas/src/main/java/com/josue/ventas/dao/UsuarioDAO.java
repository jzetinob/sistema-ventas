/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Usuario;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public interface UsuarioDAO {

    void guardar(Usuario usuario);

    List<Usuario> listar();

    /** Actualiza nombre, usuario, rol y estado (no la contrasena). */
    void actualizar(Usuario usuario);

    void cambiarClave(int id, String claveHash, String salt);

    void eliminar(int id);

    Usuario buscarPorUsuario(String usuario);

    boolean existeUsuario(String usuario);

    int contar();

    int contarAdministradoresActivos();
}
