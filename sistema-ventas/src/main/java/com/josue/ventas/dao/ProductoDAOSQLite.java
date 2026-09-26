/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Categoria;
import com.josue.ventas.modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de ProductoDAO sobre SQLite usando JDBC.
 * Sustituye al almacenamiento en CSV: cada operacion se ejecuta
 * directamente contra la base de datos.
 *
 * @author josue zetino
 */
public class ProductoDAOSQLite implements ProductoDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProductoDAOSQLite.class.getName());

    private static final ProductoDAOSQLite instancia = new ProductoDAOSQLite();

    private ProductoDAOSQLite() {
    }

    public static ProductoDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    @Override
    public void guardar(Producto producto) {
        String sql = "INSERT INTO productos (codigo, nombre, precio, existencia, categoria_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getExistencia());
            asignarCategoria(ps, 5, producto);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al guardar producto", ex);
        }
    }

    @Override
    public List<Producto> listar() {
        List<Producto> productos = new ArrayList<>();
        String sql = """
                SELECT p.id, p.codigo, p.nombre, p.precio, p.existencia, c.id AS categoria_id, c.nombre AS categoria
                FROM productos p
                LEFT JOIN categorias c ON c.id = p.categoria_id
                ORDER BY p.id
                """;
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getInt("id"));
                producto.setCodigo(rs.getString("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setPrecio(rs.getDouble("precio"));
                producto.setExistencia(rs.getInt("existencia"));
                if (rs.getString("categoria") != null) {
                    producto.setCategoria(new Categoria(rs.getInt("categoria_id"), rs.getString("categoria")));
                }
                productos.add(producto);
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar productos", ex);
        }
        return productos;
    }

    @Override
    public void actualizar(Producto producto) {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, precio = ?, existencia = ?, categoria_id = ? WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getExistencia());
            asignarCategoria(ps, 5, producto);
            ps.setInt(6, producto.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al actualizar producto", ex);
        }
    }

    private void asignarCategoria(PreparedStatement ps, int indice, Producto producto) throws SQLException {
        if (producto.getCategoria() != null) {
            ps.setInt(indice, producto.getCategoria().getId());
        } else {
            ps.setNull(indice, java.sql.Types.INTEGER);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo eliminar el producto " + id, ex);
            return false;
        }
    }

    @Override
    public boolean existeCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM productos WHERE LOWER(codigo) = LOWER(?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al consultar codigo", ex);
            return false;
        }
    }
}
