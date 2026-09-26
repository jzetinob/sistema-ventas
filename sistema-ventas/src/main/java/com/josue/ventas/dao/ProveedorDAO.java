/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Proveedor;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public interface ProveedorDAO {

    void guardar(Proveedor proveedor);

    List<Proveedor> listar();

    void actualizar(Proveedor proveedor);

    /**
     * @return false si no se pudo eliminar (por ejemplo, tiene compras registradas)
     */
    boolean eliminar(int id);

    boolean existeNit(String nit);
}
