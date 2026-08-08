/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexion a la base de datos SQLite mediante JDBC.
 * Es un singleton: la conexion se abre una sola vez y se reutiliza.
 * Al arrancar crea la base (si no existe) y las tablas necesarias.
 *
 * @author josue zetino
 */
public class ConexionBD {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConexionBD.class.getName());

    private static final String DIRECTORIO = "datos";
    private static final String ARCHIVO_BD = DIRECTORIO + File.separator + "sistema_ventas.db";
    private static final String URL = "jdbc:sqlite:" + ARCHIVO_BD;

    private static final ConexionBD instancia = new ConexionBD();

    private Connection conexion;

    private ConexionBD() {
        conectar();
        crearTablas();
        MigradorDatos.instancia().migrarSiVacio(conexion);
    }

    public static ConexionBD getInstancia() {
        return instancia;
    }

    public static String getRutaBaseDatos() {
        return ARCHIVO_BD;
    }

    private void conectar() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dir = new File(DIRECTORIO);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            conexion = DriverManager.getConnection(URL);
            logger.info("Conexion a la base de datos establecida: " + ARCHIVO_BD);
        } catch (ClassNotFoundException | SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo conectar a la base de datos", ex);
        }
    }

    public Connection getConnection() {
        return conexion;
    }

    private void crearTablas() {
        String sql = """
                CREATE TABLE IF NOT EXISTS clientes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nit TEXT NOT NULL UNIQUE,
                    nombre TEXT NOT NULL,
                    direccion TEXT,
                    telefono TEXT
                );
                CREATE TABLE IF NOT EXISTS productos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo TEXT NOT NULL UNIQUE,
                    nombre TEXT NOT NULL,
                    precio REAL NOT NULL
                );
                CREATE TABLE IF NOT EXISTS facturas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero_factura TEXT NOT NULL UNIQUE,
                    nit TEXT NOT NULL,
                    cliente TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    total REAL NOT NULL
                );
                CREATE TABLE IF NOT EXISTS factura_detalles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    factura_id INTEGER NOT NULL,
                    producto TEXT NOT NULL,
                    cantidad INTEGER NOT NULL,
                    precio REAL NOT NULL,
                    subtotal REAL NOT NULL,
                    FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE
                );
                CREATE TABLE IF NOT EXISTS empleados (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo_empleado TEXT NOT NULL UNIQUE,
                    nombre TEXT NOT NULL,
                    nit TEXT,
                    telefono TEXT,
                    puesto TEXT
                );
                """;
        try (Statement stmt = conexion.createStatement()) {
            stmt.executeUpdate(sql);
            logger.info("Tablas de la base de datos verificadas/creadas");
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudieron crear las tablas", ex);
        }
        verificarColumnaExistencia();
    }

    private void verificarColumnaExistencia() {
        String sql = "SELECT COUNT(*) FROM pragma_table_info('productos') WHERE name = 'existencia'";
        try (Statement stmt = conexion.createStatement(); java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement alter = conexion.createStatement()) {
                    alter.executeUpdate("ALTER TABLE productos ADD COLUMN existencia INTEGER NOT NULL DEFAULT 0");
                    logger.info("Columna existencia agregada a la tabla productos");
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo verificar la columna existencia", ex);
        }
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                logger.info("Conexion a la base de datos cerrada");
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.WARNING, "Error al cerrar la conexion", ex);
        }
    }
}
