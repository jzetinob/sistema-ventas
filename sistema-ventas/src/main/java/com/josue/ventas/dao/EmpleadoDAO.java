/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Empleado;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public interface EmpleadoDAO {

    void guardar(Empleado empleado);

    List<Empleado> listar();

    void actualizar(Empleado empleado);

    void eliminar(int id);

    boolean existeCodigoEmpleado(String codigoEmpleado);
}