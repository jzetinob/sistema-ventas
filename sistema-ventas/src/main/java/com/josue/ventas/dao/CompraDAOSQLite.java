/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Compra;
import com.josue.ventas.modelo.CompraDetalle;
import com.josue.ventas.modelo.Producto;
import com.josue.ventas.modelo.Proveedor;
import com.josue.ventas.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de CompraDAO sobre SQLite usando JDBC (singleton).
 * La compra, sus detalles y el aumento de existencia de los productos se
 * guardan en una sola transaccion: o se aplica todo o no se aplica nada.
 *
 * @author josue zetino
 */
public class CompraDAOSQLite implements CompraDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CompraDAOSQLite.class.getName());

    private static final CompraDAOSQLite instancia = new CompraDAOSQLite();

    private CompraDAOSQLite() {
    }

    public static CompraDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    @Override
    public boolean guardar(Compra compra) {
        Connection conexion = conexion();
        try {
            conexion.setAutoCommit(false);
            if (compra.getNumeroCompra() == null || compra.getNumeroCompra().isBlank()) {
                compra.setNumeroCompra(obtenerSiguienteNumeroCompra());
            }
            String sqlCompra = "INSERT INTO compras (numero_compra, proveedor_id, usuario_id, fecha, total) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, compra.getNumeroCompra());
                ps.setInt(2, compra.getProveedor().getId());
                if (compra.getUsuario() != null) {
                    ps.setInt(3, compra.getUsuario().getId());
                } else {
                    ps.setNull(3, java.sql.Types.INTEGER);
                }
                ps.setString(4, compra.getFecha().toString());
                ps.setDouble(5, compra.calcularTotal());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        compra.setId(rs.getInt(1));
                    }
                }
            }
            String sqlDetalle = "INSERT INTO compra_detalles (compra_id, producto_id, cantidad, costo_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlExistencia = "UPDATE productos SET existencia = existencia + ? WHERE id = ?";
            try (PreparedStatement psDetalle = conexion.prepareStatement(sqlDetalle);
                    PreparedStatement psExistencia = conexion.prepareStatement(sqlExistencia)) {
                for (CompraDetalle d : compra.getDetalles()) {
                    psDetalle.setInt(1, compra.getId());
                    psDetalle.setInt(2, d.getProducto().getId());
                    psDetalle.setInt(3, d.getCantidad());
                    psDetalle.setDouble(4, d.getCostoUnitario());
                    psDetalle.setDouble(5, d.calcularSubtotal());
                    psDetalle.addBatch();

                    psExistencia.setInt(1, d.getCantidad());
                    psExistencia.setInt(2, d.getProducto().getId());
                    psExistencia.addBatch();
                }
                psDetalle.executeBatch();
                psExistencia.executeBatch();
            }
            conexion.commit();
            return true;
        } catch (SQLException ex) {
            revertir(conexion);
            logger.log(java.util.logging.Level.SEVERE, "Error al guardar compra", ex);
            return false;
        } finally {
            restaurarAutoCommit(conexion);
        }
    }

    @Override
    public List<Compra> listar() {
        List<Compra> compras = new ArrayList<>();
        String sql = """
                SELECT c.id, c.numero_compra, c.fecha, c.total,
                       p.id AS proveedor_id, p.nit, p.nombre AS proveedor,
                       u.id AS usuario_id, u.usuario
                FROM compras c
                JOIN proveedores p ON p.id = c.proveedor_id
                LEFT JOIN usuarios u ON u.id = c.usuario_id
                ORDER BY c.id
                """;
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Compra compra = new Compra();
                compra.setId(rs.getInt("id"));
                compra.setNumeroCompra(rs.getString("numero_compra"));
                compra.setFecha(LocalDate.parse(rs.getString("fecha")));

                Proveedor proveedor = new Proveedor();
                proveedor.setId(rs.getInt("proveedor_id"));
                proveedor.setNit(rs.getString("nit"));
                proveedor.setNombre(rs.getString("proveedor"));
                compra.setProveedor(proveedor);

                if (rs.getString("usuario") != null) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("usuario_id"));
                    usuario.setUsuario(rs.getString("usuario"));
                    compra.setUsuario(usuario);
                }
                cargarDetalles(compra);
                compra.setTotal(rs.getDouble("total"));
                compras.add(compra);
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar compras", ex);
        }
        return compras;
    }

    private void cargarDetalles(Compra compra) throws SQLException {
        String sql = """
                SELECT d.cantidad, d.costo_unitario, pr.id, pr.codigo, pr.nombre
                FROM compra_detalles d
                JOIN productos pr ON pr.id = d.producto_id
                WHERE d.compra_id = ?
                ORDER BY d.id
                """;
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, compra.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setId(rs.getInt("id"));
                    producto.setCodigo(rs.getString("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    compra.agregarDetalle(new CompraDetalle(producto, rs.getInt("cantidad"), rs.getDouble("costo_unitario")));
                }
            }
        }
    }

    @Override
    public boolean anular(int id) {
        Connection conexion = conexion();
        try {
            conexion.setAutoCommit(false);
            String sqlDetalles = "SELECT producto_id, cantidad FROM compra_detalles WHERE compra_id = ?";
            String sqlExistencia = "UPDATE productos SET existencia = existencia - ? WHERE id = ? AND existencia >= ?";
            try (PreparedStatement psDetalles = conexion.prepareStatement(sqlDetalles);
                    PreparedStatement psExistencia = conexion.prepareStatement(sqlExistencia)) {
                psDetalles.setInt(1, id);
                try (ResultSet rs = psDetalles.executeQuery()) {
                    while (rs.next()) {
                        int cantidad = rs.getInt("cantidad");
                        psExistencia.setInt(1, cantidad);
                        psExistencia.setInt(2, rs.getInt("producto_id"));
                        psExistencia.setInt(3, cantidad);
                        if (psExistencia.executeUpdate() == 0) {
                            conexion.rollback();
                            return false;
                        }
                    }
                }
            }
            try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM compras WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            conexion.commit();
            return true;
        } catch (SQLException ex) {
            revertir(conexion);
            logger.log(java.util.logging.Level.SEVERE, "Error al anular compra", ex);
            return false;
        } finally {
            restaurarAutoCommit(conexion);
        }
    }

    @Override
    public String obtenerSiguienteNumeroCompra() {
        String sql = "SELECT MAX(CAST(SUBSTR(numero_compra, 5) AS INTEGER)) FROM compras WHERE numero_compra LIKE 'COM-%'";
        int correlativo = 1;
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                correlativo = rs.getInt(1) + 1;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al calcular el siguiente numero de compra", ex);
        }
        return String.format("COM-%04d", correlativo);
    }

    private void revertir(Connection conexion) {
        try {
            conexion.rollback();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo revertir la transaccion", ex);
        }
    }

    private void restaurarAutoCommit(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo restaurar autocommit", ex);
        }
    }
}
