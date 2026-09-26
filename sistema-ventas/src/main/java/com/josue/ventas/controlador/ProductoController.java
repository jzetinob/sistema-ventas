/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.ProductoDAO;
import com.josue.ventas.dao.ProductoDAOSQLite;
import com.josue.ventas.modelo.Producto;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class ProductoController {

    ProductoDAO dao;

    public ProductoController() {
        dao = ProductoDAOSQLite.getInstancia();
    }

    public void Guardar(Producto producto) {
        dao.guardar(producto);
    }

    public List<Producto> GetProductos() {
        return dao.listar();
    }

    public void Actualizar(Producto producto) {
        dao.actualizar(producto);
    }

    public boolean Eliminar(int id) {
        return dao.eliminar(id);
    }

    public boolean ExisteCodigo(String codigo) {
        return dao.existeCodigo(codigo);
    }
}
