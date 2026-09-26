/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.CategoriaDAO;
import com.josue.ventas.dao.CategoriaDAOSQLite;
import com.josue.ventas.modelo.Categoria;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class CategoriaController {

    CategoriaDAO dao;

    public CategoriaController() {
        dao = CategoriaDAOSQLite.getInstancia();
    }

    public void Guardar(Categoria categoria) {
        dao.guardar(categoria);
    }

    public List<Categoria> GetCategorias() {
        return dao.listar();
    }

    public void Actualizar(Categoria categoria) {
        dao.actualizar(categoria);
    }

    public boolean Eliminar(int id) {
        return dao.eliminar(id);
    }

    public boolean ExisteNombre(String nombre) {
        return dao.existeNombre(nombre);
    }
}
