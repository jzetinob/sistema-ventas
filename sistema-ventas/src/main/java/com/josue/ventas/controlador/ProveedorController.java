/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.ProveedorDAO;
import com.josue.ventas.dao.ProveedorDAOSQLite;
import com.josue.ventas.modelo.Proveedor;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class ProveedorController {

    ProveedorDAO dao;

    public ProveedorController() {
        dao = ProveedorDAOSQLite.getInstancia();
    }

    public void Guardar(Proveedor proveedor) {
        dao.guardar(proveedor);
    }

    public List<Proveedor> GetProveedores() {
        return dao.listar();
    }

    public void Actualizar(Proveedor proveedor) {
        dao.actualizar(proveedor);
    }

    public boolean Eliminar(int id) {
        return dao.eliminar(id);
    }

    public boolean ExisteNit(String nit) {
        return dao.existeNit(nit);
    }
}
