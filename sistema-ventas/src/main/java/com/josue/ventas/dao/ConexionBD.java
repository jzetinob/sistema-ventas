/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Administra la conexion a la base de datos mediante JDBC (singleton: la
 * conexion se abre una sola vez y se reutiliza). Soporta dos motores:
 *
 * - SQLite (por defecto): archivo local datos/sistema_ventas.db.
 * - PostgreSQL (Supabase, en la nube): si existe config/bd.properties con
 *   bd.motor=postgresql. Asi la app de escritorio y la app movil comparten
 *   los mismos datos.
 *
 * Al arrancar crea las tablas que falten (y migra bases de versiones
 * anteriores). Los DAO usan SQL estandar que funciona en los dos motores.
 *
 * @author josue zetino
 */
public class ConexionBD {

    public enum Motor {
        SQLITE, POSTGRESQL
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConexionBD.class.getName());

    private static final String DIRECTORIO = "datos";
    private static final String ARCHIVO_BD = DIRECTORIO + File.separator + "sistema_ventas.db";
    private static final String URL = "jdbc:sqlite:" + ARCHIVO_BD;
    public static final String ARCHIVO_CONFIG = "config" + File.separator + "bd.properties";

    private static final ConexionBD instancia = new ConexionBD();

    private Motor motor = Motor.SQLITE;
    private String descripcion = ARCHIVO_BD;
    private Connection conexion;
    private String errorConexion;

    private ConexionBD() {
        Properties config = leerConfiguracion();
        if (config != null && "postgresql".equalsIgnoreCase(config.getProperty("bd.motor", "").trim())) {
            motor = Motor.POSTGRESQL;
            conectarPostgres(config);
            if (conexion != null) {
                crearEsquemaPostgres();
                CopiadorBD.importarSqliteSiVacia(conexion, new File(ARCHIVO_BD));
            }
        } else {
            conectar();
            if (conexion != null) {
                crearTablas(conexion);
                MigradorDatos.instancia().migrarSiVacio(conexion);
            }
        }
    }

    public static ConexionBD getInstancia() {
        return instancia;
    }

    public static String getRutaBaseDatos() {
        return ARCHIVO_BD;
    }

    public Motor getMotor() {
        return motor;
    }

    /** Texto para mostrar donde estan los datos (archivo local o servidor). */
    public String getDescripcion() {
        return descripcion;
    }

    /** Mensaje de error si no se pudo conectar; null si la conexion esta bien. */
    public String getErrorConexion() {
        return errorConexion;
    }

    private Properties leerConfiguracion() {
        File archivo = new File(ARCHIVO_CONFIG);
        if (!archivo.isFile()) {
            return null;
        }
        Properties config = new Properties();
        try (InputStream in = new FileInputStream(archivo)) {
            config.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            return config;
        } catch (IOException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo leer " + ARCHIVO_CONFIG, ex);
            errorConexion = "No se pudo leer el archivo " + ARCHIVO_CONFIG + ": " + ex.getMessage();
            return null;
        }
    }

    private void conectarPostgres(Properties config) {
        String url = config.getProperty("bd.url", "").trim();
        String usuario = config.getProperty("bd.usuario", "").trim();
        String clave = config.getProperty("bd.clave", "");
        if (url.isEmpty() || usuario.isEmpty() || clave.isEmpty() || clave.startsWith("ESCRIBA")) {
            errorConexion = "Complete bd.url, bd.usuario y bd.clave en " + ARCHIVO_CONFIG + ".";
            return;
        }
        Properties props = new Properties();
        props.setProperty("user", usuario);
        props.setProperty("password", clave);
        props.setProperty("sslmode", "require");
        props.setProperty("connectTimeout", "15");
        props.setProperty("ApplicationName", "sistema-ventas-escritorio");
        try {
            conexion = DriverManager.getConnection(url, props);
            descripcion = "PostgreSQL en la nube (" + url.replaceFirst("^jdbc:postgresql://", "").replaceFirst("[/?].*$", "") + ")";
            logger.info("Conexion a PostgreSQL establecida: " + descripcion);
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo conectar a PostgreSQL", ex);
            errorConexion = "No se pudo conectar a la base de datos en la nube.\n"
                    + "Revise su conexión a internet y los datos de " + ARCHIVO_CONFIG + ".\n\nDetalle: " + ex.getMessage();
        }
    }

    /** Ejecuta el script bd/esquema-postgresql.sql (idempotente: se puede correr siempre). */
    private void crearEsquemaPostgres() {
        try (InputStream in = ConexionBD.class.getResourceAsStream("/bd/esquema-postgresql.sql");
                Statement stmt = conexion.createStatement()) {
            if (in == null) {
                throw new IOException("No se encontro el recurso bd/esquema-postgresql.sql");
            }
            stmt.execute(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            logger.info("Esquema de PostgreSQL verificado/creado");
        } catch (IOException | SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo crear el esquema en PostgreSQL", ex);
            errorConexion = "Se conectó a la nube pero no se pudieron crear las tablas.\n\nDetalle: " + ex.getMessage();
        }
    }

    private void conectar() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dir = new File(DIRECTORIO);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            conexion = abrirSqlite(new File(ARCHIVO_BD));
            logger.info("Conexion a la base de datos establecida: " + ARCHIVO_BD);
        } catch (ClassNotFoundException | SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo conectar a la base de datos", ex);
            errorConexion = "No se pudo abrir la base de datos local " + ARCHIVO_BD + ".\n\nDetalle: " + ex.getMessage();
        }
    }

    static Connection abrirSqlite(File archivo) throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + archivo.getPath());
        // SQLite no valida las llaves foraneas si no se activan en cada conexion
        try (Statement stmt = c.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    public Connection getConnection() {
        return conexion;
    }

    /** Crea (o completa) las tablas de SQLite en la conexion indicada. */
    static void crearTablas(Connection conexion) {
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
                CREATE TABLE IF NOT EXISTS categorias (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE,
                    descripcion TEXT
                );
                CREATE TABLE IF NOT EXISTS proveedores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nit TEXT NOT NULL UNIQUE,
                    nombre TEXT NOT NULL,
                    direccion TEXT,
                    telefono TEXT,
                    correo TEXT
                );
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    usuario TEXT NOT NULL UNIQUE,
                    nombre TEXT NOT NULL,
                    clave_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    rol TEXT NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'VENDEDOR')),
                    activo INTEGER NOT NULL DEFAULT 1
                );
                CREATE TABLE IF NOT EXISTS compras (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero_compra TEXT NOT NULL UNIQUE,
                    proveedor_id INTEGER NOT NULL,
                    usuario_id INTEGER,
                    fecha TEXT NOT NULL,
                    total REAL NOT NULL,
                    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
                );
                CREATE TABLE IF NOT EXISTS compra_detalles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    compra_id INTEGER NOT NULL,
                    producto_id INTEGER NOT NULL,
                    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
                    costo_unitario REAL NOT NULL,
                    subtotal REAL NOT NULL,
                    FOREIGN KEY (compra_id) REFERENCES compras(id) ON DELETE CASCADE,
                    FOREIGN KEY (producto_id) REFERENCES productos(id)
                );
                """;
        try (Statement stmt = conexion.createStatement()) {
            stmt.executeUpdate(sql);
            logger.info("Tablas de la base de datos verificadas/creadas");
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudieron crear las tablas", ex);
        }
        agregarColumnaSiFalta(conexion, "productos", "existencia", "INTEGER NOT NULL DEFAULT 0");
        agregarColumnaSiFalta(conexion, "productos", "categoria_id", "INTEGER REFERENCES categorias(id) ON DELETE SET NULL");
        agregarColumnaSiFalta(conexion, "factura_detalles", "producto_id", "INTEGER REFERENCES productos(id) ON DELETE SET NULL");
    }

    /**
     * Migracion para bases creadas con versiones anteriores: agrega la
     * columna solo si la tabla todavia no la tiene. Los nombres vienen de
     * constantes del codigo, nunca del usuario.
     */
    private static void agregarColumnaSiFalta(Connection conexion, String tabla, String columna, String definicion) {
        String sql = "SELECT COUNT(*) FROM pragma_table_info('" + tabla + "') WHERE name = '" + columna + "'";
        try (Statement stmt = conexion.createStatement(); java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement alter = conexion.createStatement()) {
                    alter.executeUpdate("ALTER TABLE " + tabla + " ADD COLUMN " + columna + " " + definicion);
                    logger.info("Columna " + columna + " agregada a la tabla " + tabla);
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo verificar la columna " + columna, ex);
        }
    }

    /**
     * Copia de seguridad completa en un archivo SQLite (.db) que se puede
     * abrir con DB Browser for SQLite o usar como base local. El archivo no
     * debe existir.
     * - SQLite: VACUUM INTO (copia consistente del archivo).
     * - PostgreSQL: se copian todas las tablas de la nube al archivo.
     */
    public void respaldar(File destino) throws SQLException {
        if (motor == Motor.SQLITE) {
            try (java.sql.PreparedStatement ps = conexion.prepareStatement("VACUUM INTO ?")) {
                ps.setString(1, destino.getAbsolutePath());
                ps.executeUpdate();
            }
        } else {
            try (Connection copia = abrirSqlite(destino)) {
                crearTablas(copia);
                CopiadorBD.copiar(conexion, copia);
            }
        }
        logger.info("Respaldo de la base de datos creado en " + destino.getAbsolutePath());
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
