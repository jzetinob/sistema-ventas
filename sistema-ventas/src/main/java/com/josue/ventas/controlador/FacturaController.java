/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.FacturaDAO;
import com.josue.ventas.dao.FacturaDAOSQLite;
import com.josue.ventas.modelo.Factura;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class FacturaController {

    FacturaDAO dao;

    public FacturaController() {
        dao = FacturaDAOSQLite.getInstancia();
    }

    public boolean Guardar(Factura factura) {
        return dao.guardar(factura);
    }

    public List<Factura> GetFacturas() {
        return dao.listar();
    }

    public void Eliminar(int id) {
        dao.eliminar(id);
    }

    public void EliminarConDetalles(int id) {
        dao.eliminarConDetalles(id);
    }

    public String obtenerSiguienteNumeroFactura() {
        return dao.obtenerSiguienteNumeroFactura();
    }
}