/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Compra;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public interface CompraDAO {

    /** Guarda la compra con sus detalles y suma la existencia de cada producto. */
    boolean guardar(Compra compra);

    List<Compra> listar();

    /**
     * Anula la compra: la borra con sus detalles y resta la existencia que
     * habia sumado. Devuelve false si algun producto ya no tiene existencia
     * suficiente (se vendio parte de lo comprado).
     */
    boolean anular(int id);

    String obtenerSiguienteNumeroCompra();

    /** Costo unitario de la compra mas reciente de ese producto, o null si nunca se ha comprado. */
    Double ultimoCosto(int productoId);
}
