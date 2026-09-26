/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.CompraDAO;
import com.josue.ventas.dao.CompraDAOSQLite;
import com.josue.ventas.modelo.Compra;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class CompraController {

    CompraDAO dao;

    public CompraController() {
        dao = CompraDAOSQLite.getInstancia();
    }

    public boolean Guardar(Compra compra) {
        compra.setUsuario(Sesion.getInstancia().getUsuario());
        return dao.guardar(compra);
    }

    public List<Compra> GetCompras() {
        return dao.listar();
    }

    public boolean Anular(int id) {
        return dao.anular(id);
    }

    public String ObtenerSiguienteNumeroCompra() {
        return dao.obtenerSiguienteNumeroCompra();
    }
}
