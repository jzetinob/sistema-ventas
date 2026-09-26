/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Factura;
import com.josue.ventas.modelo.FacturaDetalle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de FacturaDAO sobre SQLite usando JDBC.
 * La factura y sus detalles se guardan en una transaccion para que
 * el registro quede completo o no se guarde nada (integridad).
 *
 * @author josue zetino
 */
public class FacturaDAOSQLite implements FacturaDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FacturaDAOSQLite.class.getName());

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final FacturaDAOSQLite instancia = new FacturaDAOSQLite();

    private FacturaDAOSQLite() {
    }

    public static FacturaDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    /**
     * Guarda la factura con sus detalles y descuenta la existencia de los
     * productos del catalogo, todo en una transaccion. Si algun producto ya
     * no tiene existencia suficiente, no se guarda nada y devuelve false.
     */
    @Override
    public boolean guardar(Factura factura) {
        Connection conexion = conexion();
        try {
            conexion.setAutoCommit(false);
            if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isBlank()) {
                factura.setNumeroFactura(obtenerSiguienteNumeroFactura());
            }
            String sqlFactura = "INSERT INTO facturas (numero_factura, nit, cliente, fecha, total) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, factura.getNumeroFactura());
                ps.setString(2, factura.getNit() != null ? factura.getNit() : "");
                ps.setString(3, factura.getNombreCliente() != null ? factura.getNombreCliente() : "");
                ps.setString(4, factura.getFecha() != null ? FORMATO_FECHA.format(factura.getFecha().atTime(0, 0)) : "");
                ps.setDouble(5, factura.getTotal());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        factura.setId(rs.getInt(1));
                    }
                }
            }
            guardarDetalles(conexion, factura);
            if (!descontarExistencia(conexion, factura)) {
                conexion.rollback();
                return false;
            }
            conexion.commit();
            return true;
        } catch (SQLException ex) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo revertir la factura", rollbackEx);
            }
            logger.log(java.util.logging.Level.SEVERE, "Error al guardar factura", ex);
            return false;
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo restaurar autocommit", ex);
            }
        }
    }

    private void guardarDetalles(Connection conexion, Factura factura) throws SQLException {
        String sql = "INSERT INTO factura_detalles (factura_id, producto, cantidad, precio, subtotal, producto_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (FacturaDetalle d : factura.getDetalles()) {
                ps.setInt(1, factura.getId());
                ps.setString(2, d.getProducto() != null ? d.getProducto().getNombre() : "");
                ps.setInt(3, d.getCantidad());
                ps.setDouble(4, d.getPrecioUnitario());
                ps.setDouble(5, d.calcularSubtotal());
                if (esProductoDelCatalogo(d)) {
                    ps.setInt(6, d.getProducto().getId());
                } else {
                    ps.setNull(6, java.sql.Types.INTEGER);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Los productos escritos a mano (sin id) no llevan control de existencia. */
    private boolean esProductoDelCatalogo(FacturaDetalle d) {
        return d.getProducto() != null && d.getProducto().getId() > 0;
    }

    private boolean descontarExistencia(Connection conexion, Factura factura) throws SQLException {
        String sql = "UPDATE productos SET existencia = existencia - ? WHERE id = ? AND existencia >= ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (FacturaDetalle d : factura.getDetalles()) {
                if (!esProductoDelCatalogo(d)) {
                    continue;
                }
                ps.setInt(1, d.getCantidad());
                ps.setInt(2, d.getProducto().getId());
                ps.setInt(3, d.getCantidad());
                if (ps.executeUpdate() == 0) {
                    logger.log(java.util.logging.Level.WARNING, "Existencia insuficiente de {0}", d.getProducto().getNombre());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<Factura> listar() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT id, numero_factura, nit, cliente, fecha, total FROM facturas ORDER BY id";
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Factura factura = new Factura();
                factura.setId(rs.getInt("id"));
                factura.setNumeroFactura(rs.getString("numero_factura"));
                factura.setNit(rs.getString("nit"));
                factura.setNombreCliente(rs.getString("cliente"));
                factura.setFecha(parsearFecha(rs.getString("fecha")));
                factura.setTotal(rs.getDouble("total"));
                cargarDetalles(factura);
                facturas.add(factura);
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar facturas", ex);
        }
        return facturas;
    }

    private void cargarDetalles(Factura factura) throws SQLException {
        String sql = "SELECT producto, cantidad, precio, subtotal FROM factura_detalles WHERE factura_id = ? ORDER BY id";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, factura.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    factura.agregarDetalle(rs.getString("producto"), rs.getInt("cantidad"), rs.getDouble("precio"));
                }
            }
        }
    }

    @Override
    public void actualizar(Factura factura) {
        Connection conexion = conexion();
        try {
            conexion.setAutoCommit(false);
            String sql = "UPDATE facturas SET nit = ?, cliente = ?, fecha = ?, total = ? WHERE id = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, factura.getNit() != null ? factura.getNit() : "");
                ps.setString(2, factura.getNombreCliente() != null ? factura.getNombreCliente() : "");
                ps.setString(3, factura.getFecha() != null ? FORMATO_FECHA.format(factura.getFecha().atTime(0, 0)) : "");
                ps.setDouble(4, factura.getTotal());
                ps.setInt(5, factura.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM factura_detalles WHERE factura_id = ?")) {
                ps.setInt(1, factura.getId());
                ps.executeUpdate();
            }
            guardarDetalles(conexion, factura);
            conexion.commit();
        } catch (SQLException ex) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo revertir la actualizacion", rollbackEx);
            }
            logger.log(java.util.logging.Level.SEVERE, "Error al actualizar factura", ex);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo restaurar autocommit", ex);
            }
        }
    }

    /**
     * Elimina la factura (sus detalles se borran en cascada) y devuelve a la
     * existencia lo que se habia vendido de los productos del catalogo.
     */
    @Override
    public void eliminar(int id) {
        Connection conexion = conexion();
        try {
            conexion.setAutoCommit(false);
            String sqlDevolver = """
                    UPDATE productos SET existencia = existencia + (
                        SELECT SUM(d.cantidad) FROM factura_detalles d
                        WHERE d.factura_id = ? AND d.producto_id = productos.id)
                    WHERE id IN (SELECT producto_id FROM factura_detalles WHERE factura_id = ? AND producto_id IS NOT NULL)
                    """;
            try (PreparedStatement ps = conexion.prepareStatement(sqlDevolver)) {
                ps.setInt(1, id);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM facturas WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            conexion.commit();
        } catch (SQLException ex) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo revertir la eliminacion", rollbackEx);
            }
            logger.log(java.util.logging.Level.SEVERE, "Error al eliminar factura", ex);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo restaurar autocommit", ex);
            }
        }
    }

    @Override
    public void eliminarConDetalles(int id) {
        eliminar(id);
    }

    @Override
    public String obtenerSiguienteNumeroFactura() {
        int correlativo = 1;
        String sql = "SELECT numero_factura FROM facturas";
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String numero = rs.getString("numero_factura");
                int guion = numero.lastIndexOf('-');
                if (guion == -1) {
                    continue;
                }
                try {
                    int valor = Integer.parseInt(numero.substring(guion + 1));
                    if (valor >= correlativo) {
                        correlativo = valor + 1;
                    }
                } catch (NumberFormatException ex) {
                    logger.log(java.util.logging.Level.WARNING, "No se pudo interpretar el numero de factura: {0}", numero);
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al calcular el siguiente numero de factura", ex);
        }
        return String.format("FAC-%04d", correlativo);
    }

    private LocalDate parsearFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return LocalDate.now();
        }
        try {
            if (texto.length() == 10) {
                return LocalDate.parse(texto);
            }
            return java.time.LocalDateTime.parse(texto, FORMATO_FECHA).toLocalDate();
        } catch (java.time.format.DateTimeParseException ex) {
            return LocalDate.now();
        }
    }
}
