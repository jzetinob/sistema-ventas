/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de ProveedorDAO sobre SQLite usando JDBC (singleton).
 * Un proveedor con compras registradas no se puede eliminar: la llave
 * foranea de compras lo impide.
 *
 * @author josue zetino
 */
public class ProveedorDAOSQLite implements ProveedorDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProveedorDAOSQLite.class.getName());

    private static final ProveedorDAOSQLite instancia = new ProveedorDAOSQLite();

    private ProveedorDAOSQLite() {
    }

    public static ProveedorDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    @Override
    public void guardar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedores (nit, nombre, direccion, telefono, correo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proveedor.getNit());
            ps.setString(2, proveedor.getNombre());
            ps.setString(3, proveedor.getDireccion());
            ps.setString(4, proveedor.getTelefono());
            ps.setString(5, proveedor.getCorreo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    proveedor.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al guardar proveedor", ex);
        }
    }

    @Override
    public List<Proveedor> listar() {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT id, nit, nombre, direccion, telefono, correo FROM proveedores ORDER BY id";
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setId(rs.getInt("id"));
                proveedor.setNit(rs.getString("nit"));
                proveedor.setNombre(rs.getString("nombre"));
                proveedor.setDireccion(rs.getString("direccion"));
                proveedor.setTelefono(rs.getString("telefono"));
                proveedor.setCorreo(rs.getString("correo"));
                proveedores.add(proveedor);
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar proveedores", ex);
        }
        return proveedores;
    }

    @Override
    public void actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedores SET nit = ?, nombre = ?, direccion = ?, telefono = ?, correo = ? WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, proveedor.getNit());
            ps.setString(2, proveedor.getNombre());
            ps.setString(3, proveedor.getDireccion());
            ps.setString(4, proveedor.getTelefono());
            ps.setString(5, proveedor.getCorreo());
            ps.setInt(6, proveedor.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al actualizar proveedor", ex);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedores WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo eliminar el proveedor " + id, ex);
            return false;
        }
    }

    @Override
    public boolean existeNit(String nit) {
        String sql = "SELECT COUNT(*) FROM proveedores WHERE nit = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, nit);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al consultar NIT de proveedor", ex);
            return false;
        }
    }
}
