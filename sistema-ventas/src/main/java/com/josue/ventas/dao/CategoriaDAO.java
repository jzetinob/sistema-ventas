/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Categoria;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public interface CategoriaDAO {

    void guardar(Categoria categoria);

    List<Categoria> listar();

    void actualizar(Categoria categoria);

    boolean eliminar(int id);

    boolean existeNombre(String nombre);
}
