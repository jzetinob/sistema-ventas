/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Factura;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public interface FacturaDAO {

    /** @return false si no se pudo guardar (por ejemplo, sin existencia suficiente) */
    boolean guardar(Factura factura);

    List<Factura> listar();

    void actualizar(Factura factura);

    void eliminar(int id);

    void eliminarConDetalles(int id);

    String obtenerSiguienteNumeroFactura();
}
