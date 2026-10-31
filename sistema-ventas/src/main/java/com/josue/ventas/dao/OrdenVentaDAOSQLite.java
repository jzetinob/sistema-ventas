package com.josue.ventas.dao;

import com.josue.ventas.modelo.Cliente;
import com.josue.ventas.modelo.OrdenVenta;
import com.josue.ventas.modelo.OrdenVentaDetalle;
import com.josue.ventas.modelo.Producto;
import com.josue.ventas.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC de OrdenVentaDAO. Usa SQL estandar, asi que funciona
 * igual con SQLite y con PostgreSQL (Supabase).
 *
 * @author josue zetino
 */
public class OrdenVentaDAOSQLite implements OrdenVentaDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OrdenVentaDAOSQLite.class.getName());
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final OrdenVentaDAOSQLite instancia = new OrdenVentaDAOSQLite();

    private OrdenVentaDAOSQLite() {
    }

    public static OrdenVentaDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    @Override
    public List<OrdenVenta> listar() {
        List<OrdenVenta> ordenes = new ArrayList<>();
        String sql = """
                SELECT o.id, o.numero_orden, o.fecha, o.estado, o.observaciones, o.total,
                       c.id AS cliente_id, c.nit, c.nombre AS cliente, u.usuario
                FROM ordenes_venta o
                JOIN clientes c ON c.id = o.cliente_id
                LEFT JOIN usuarios u ON u.id = o.usuario_id
                ORDER BY o.id DESC
                """;
        try (PreparedStatement ps = conexion().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrdenVenta orden = new OrdenVenta();
                orden.setId(rs.getInt("id"));
                orden.setNumeroOrden(rs.getString("numero_orden"));
                orden.setFecha(LocalDateTime.parse(rs.getString("fecha"), FORMATO_FECHA));
                orden.setEstado(OrdenVenta.Estado.valueOf(rs.getString("estado")));
                orden.setObservaciones(rs.getString("observaciones"));
                orden.setTotal(rs.getDouble("total"));
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("cliente_id"));
                cliente.setNit(rs.getString("nit"));
                cliente.setNombre(rs.getString("cliente"));
                orden.setCliente(cliente);
                if (rs.getString("usuario") != null) {
                    Usuario usuario = new Usuario();
                    usuario.setUsuario(rs.getString("usuario"));
                    orden.setUsuario(usuario);
                }
                ordenes.add(orden);
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar ordenes de venta", ex);
        }
        return ordenes;
    }

    @Override
    public List<OrdenVentaDetalle> listarDetalles(int ordenId) {
        List<OrdenVentaDetalle> detalles = new ArrayList<>();
        String sql = """
                SELECT d.cantidad, d.precio_unitario, p.id, p.codigo, p.nombre
                FROM orden_venta_detalles d
                JOIN productos p ON p.id = d.producto_id
                WHERE d.orden_venta_id = ?
                ORDER BY d.id
                """;
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, ordenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdProducto(rs.getInt("id"));
                    producto.setCodigo(rs.getString("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    detalles.add(new OrdenVentaDetalle(producto, rs.getInt("cantidad"), rs.getDouble("precio_unitario")));
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar el detalle de la orden", ex);
        }
        return detalles;
    }

    /** Solo cambia una orden que sigue PENDIENTE; devuelve false si ya la habian cambiado. */
    @Override
    public boolean cambiarEstado(int id, OrdenVenta.Estado nuevo) {
        String sql = "UPDATE ordenes_venta SET estado = ? WHERE id = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, nuevo.name());
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al cambiar el estado de la orden", ex);
            return false;
        }
    }
}
