package com.josue.ventas.dao;

import com.josue.ventas.modelo.Telefono;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de TelefonoDAO con JDBC. Usa SQL estandar, asi que funciona
 * igual con SQLite y con PostgreSQL (Supabase).
 *
 * @author josue zetino
 */
public class TelefonoDAOSQLite implements TelefonoDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelefonoDAOSQLite.class.getName());

    private static final TelefonoDAOSQLite instancia = new TelefonoDAOSQLite();

    private TelefonoDAOSQLite() {
    }

    public static TelefonoDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    @Override
    public List<Telefono> listarPorCliente(int clienteId) {
        List<Telefono> telefonos = new ArrayList<>();
        String sql = "SELECT id, cliente_id, numero, tipo FROM telefonos WHERE cliente_id = ? ORDER BY id";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Telefono telefono = new Telefono();
                    telefono.setId(rs.getInt("id"));
                    telefono.setClienteId(rs.getInt("cliente_id"));
                    telefono.setNumero(rs.getString("numero"));
                    telefono.setTipo(Telefono.Tipo.valueOf(rs.getString("tipo")));
                    telefonos.add(telefono);
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar telefonos", ex);
        }
        return telefonos;
    }

    /**
     * Guarda la lista actual de telefonos del cliente: borra los que tenia y
     * vuelve a insertar los de la lista, en una transaccion (o todo o nada).
     */
    @Override
    public boolean reemplazar(int clienteId, List<Telefono> telefonos) {
        Connection conexion = conexion();
        try {
            conexion.setAutoCommit(false);
            try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM telefonos WHERE cliente_id = ?")) {
                ps.setInt(1, clienteId);
                ps.executeUpdate();
            }
            String sql = "INSERT INTO telefonos (cliente_id, numero, tipo) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                for (Telefono telefono : telefonos) {
                    ps.setInt(1, clienteId);
                    ps.setString(2, telefono.getNumero());
                    ps.setString(3, telefono.getTipo().name());
                    ps.executeUpdate();
                }
            }
            conexion.commit();
            return true;
        } catch (SQLException ex) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo revertir los telefonos", rollbackEx);
            }
            logger.log(java.util.logging.Level.SEVERE, "Error al guardar telefonos", ex);
            return false;
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo restaurar autocommit", ex);
            }
        }
    }
}
