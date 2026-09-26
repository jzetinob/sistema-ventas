/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de UsuarioDAO sobre SQLite usando JDBC (singleton).
 * Todas las consultas usan PreparedStatement: el texto que el usuario
 * escribe en el login nunca se concatena al SQL (evita inyeccion SQL).
 *
 * @author josue zetino
 */
public class UsuarioDAOSQLite implements UsuarioDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UsuarioDAOSQLite.class.getName());

    private static final UsuarioDAOSQLite instancia = new UsuarioDAOSQLite();

    private UsuarioDAOSQLite() {
    }

    public static UsuarioDAOSQLite getInstancia() {
        return instancia;
    }

    private Connection conexion() {
        return ConexionBD.getInstancia().getConnection();
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (usuario, nombre, clave_hash, salt, rol, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getUsuario());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getClaveHash());
            ps.setString(4, usuario.getSalt());
            ps.setString(5, usuario.getRol().name());
            ps.setInt(6, usuario.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al guardar usuario", ex);
        }
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, usuario, nombre, clave_hash, salt, rol, activo FROM usuarios ORDER BY id";
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(filaAEntidad(rs));
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al listar usuarios", ex);
        }
        return usuarios;
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET usuario = ?, nombre = ?, rol = ?, activo = ? WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, usuario.getUsuario());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getRol().name());
            ps.setInt(4, usuario.isActivo() ? 1 : 0);
            ps.setInt(5, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al actualizar usuario", ex);
        }
    }

    @Override
    public void cambiarClave(int id, String claveHash, String salt) {
        String sql = "UPDATE usuarios SET clave_hash = ?, salt = ? WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, claveHash);
            ps.setString(2, salt);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al cambiar la contrasena", ex);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al eliminar usuario", ex);
        }
    }

    @Override
    public Usuario buscarPorUsuario(String usuario) {
        String sql = "SELECT id, usuario, nombre, clave_hash, salt, rol, activo FROM usuarios WHERE LOWER(usuario) = LOWER(?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? filaAEntidad(rs) : null;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al buscar usuario", ex);
            return null;
        }
    }

    @Override
    public boolean existeUsuario(String usuario) {
        return buscarPorUsuario(usuario) != null;
    }

    @Override
    public int contar() {
        return contar("SELECT COUNT(*) FROM usuarios");
    }

    @Override
    public int contarAdministradoresActivos() {
        return contar("SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMINISTRADOR' AND activo = 1");
    }

    private int contar(String sql) {
        try (Statement stmt = conexion().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error al contar usuarios", ex);
            return 0;
        }
    }

    private Usuario filaAEntidad(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setClaveHash(rs.getString("clave_hash"));
        usuario.setSalt(rs.getString("salt"));
        usuario.setRol(Usuario.Rol.valueOf(rs.getString("rol")));
        usuario.setActivo(rs.getInt("activo") == 1);
        return usuario;
    }
}
