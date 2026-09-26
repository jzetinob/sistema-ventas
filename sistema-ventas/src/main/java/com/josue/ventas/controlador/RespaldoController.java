/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.ConexionBD;
import java.io.File;
import java.sql.SQLException;

/**
 * Copias de seguridad (backup) de la base de datos.
 *
 * @author josue zetino
 */
public class RespaldoController {

    /** Devuelve el mensaje de error, o null si el respaldo se creo. */
    public String Respaldar(File destino) {
        if (!destino.getName().toLowerCase().endsWith(".db")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".db");
        }
        if (destino.exists()) {
            return "Ya existe un archivo con ese nombre. Elija otro nombre.";
        }
        try {
            ConexionBD.getInstancia().respaldar(destino);
            return null;
        } catch (SQLException ex) {
            return "No se pudo crear el respaldo: " + ex.getMessage();
        }
    }
}
