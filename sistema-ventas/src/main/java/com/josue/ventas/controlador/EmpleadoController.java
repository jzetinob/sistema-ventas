/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.EmpleadoDAO;
import com.josue.ventas.dao.EmpleadoDAOSQLite;
import com.josue.ventas.modelo.Empleado;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class EmpleadoController {

    EmpleadoDAO dao;

    public EmpleadoController() {
        dao = EmpleadoDAOSQLite.getInstancia();
    }

    public void Guardar(Empleado empleado) {
        dao.guardar(empleado);
    }

    public List<Empleado> GetEmpleados() {
        return dao.listar();
    }

    public void Actualizar(Empleado empleado) {
        dao.actualizar(empleado);
    }

    public void Eliminar(int id) {
        dao.eliminar(id);
    }

    public boolean ExisteCodigoEmpleado(String codigoEmpleado) {
        return dao.existeCodigoEmpleado(codigoEmpleado);
    }
}