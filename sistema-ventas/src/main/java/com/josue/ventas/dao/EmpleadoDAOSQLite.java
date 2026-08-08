/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de EmpleadoDAO sobre SQLite usando JDBC.
 * Sigue el mismo patron que los demas DAOs del sistema: singleton
 * y operaciones CRUD contra la base de datos.
 *
 * @author josue zetino
 */
public class EmpleadoDAOSQLite implements EmpleadoDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EmpleadoDAOSQLite.class.getName());

    private static final EmpleadoDAOSQLite instancia = new EmpleadoDAOSQLite();

    private EmpleadoDAOSQLite() {
    }

    public static EmpleadoDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    @Override
    public void guardar(Empleado empleado) {
        String sql = "INSERT INTO empleados (codigo_empleado, nombre, nit, telefono, puesto) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empleado.getCodigoEmpleado());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getNit());
            ps.setString(4, empleado.getTelefono());
            ps.setString(5, empleado.getPuesto());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    empleado.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al guardar empleado", ex);
        }
    }

    @Override
    public List<Empleado> listar() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT id, codigo_empleado, nombre, nit, telefono, puesto FROM empleados ORDER BY id";
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                empleados.add(filaAEntidad(rs));
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar empleados", ex);
        }
        return empleados;
    }

    @Override
    public void actualizar(Empleado empleado) {
        String sql = "UPDATE empleados SET codigo_empleado = ?, nombre = ?, nit = ?, telefono = ?, puesto = ? WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, empleado.getCodigoEmpleado());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getNit());
            ps.setString(4, empleado.getTelefono());
            ps.setString(5, empleado.getPuesto());
            ps.setInt(6, empleado.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al actualizar empleado", ex);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM empleados WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al eliminar empleado", ex);
        }
    }

    @Override
    public boolean existeCodigoEmpleado(String codigoEmpleado) {
        String sql = "SELECT COUNT(*) FROM empleados WHERE LOWER(codigo_empleado) = LOWER(?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, codigoEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al consultar codigo de empleado", ex);
            return false;
        }
    }

    private Empleado filaAEntidad(ResultSet rs) throws SQLException {
        Empleado empleado = new Empleado();
        empleado.setId(rs.getInt("id"));
        empleado.setCodigoEmpleado(rs.getString("codigo_empleado"));
        empleado.setNombre(rs.getString("nombre"));
        empleado.setNit(rs.getString("nit"));
        empleado.setTelefono(rs.getString("telefono"));
        empleado.setPuesto(rs.getString("puesto"));
        return empleado;
    }
}