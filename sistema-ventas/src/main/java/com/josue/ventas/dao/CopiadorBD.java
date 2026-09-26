/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Copia los datos de una base a otra (SQLite y PostgreSQL, en cualquier
 * direccion), conservando los id para que las llaves foraneas sigan
 * apuntando al registro correcto. Se usa para:
 * - subir a la nube (Supabase) los datos de la base local la primera vez;
 * - respaldar la base de la nube en un archivo .db local.
 *
 * @author josue zetino
 */
public final class CopiadorBD {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CopiadorBD.class.getName());

    /** Orden de copia: primero las tablas padre, luego las que tienen llaves foraneas. */
    static final String[] TABLAS = {
        "categorias", "productos", "clientes", "empleados", "proveedores", "usuarios",
        "facturas", "factura_detalles", "compras", "compra_detalles"
    };

    private CopiadorBD() {
    }

    /** Copia todas las tablas en una transaccion: o se copia todo o nada. */
    public static void copiar(Connection origen, Connection destino) throws SQLException {
        boolean destinoPostgres = esPostgres(destino);
        boolean autoCommit = destino.getAutoCommit();
        destino.setAutoCommit(false);
        try {
            for (String tabla : TABLAS) {
                int filas = copiarTabla(origen, destino, tabla);
                logger.info("Tabla " + tabla + ": " + filas + " registros copiados");
            }
            if (destinoPostgres) {
                reiniciarSecuencias(destino);
            }
            destino.commit();
        } catch (SQLException ex) {
            destino.rollback();
            throw ex;
        } finally {
            destino.setAutoCommit(autoCommit);
        }
    }

    /**
     * Si la base de la nube esta vacia y existe la base local de SQLite con
     * datos, los sube una sola vez (la primera vez que se usa la nube).
     */
    static void importarSqliteSiVacia(Connection postgres, File archivoSqlite) {
        if (!archivoSqlite.isFile()) {
            return;
        }
        try {
            if (!estaVacia(postgres)) {
                return;
            }
            try (Connection local = ConexionBD.abrirSqlite(archivoSqlite)) {
                ConexionBD.crearTablas(local);
                if (estaVacia(local)) {
                    return;
                }
                copiar(local, postgres);
                logger.info("Datos de " + archivoSqlite + " importados a la base de la nube");
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudieron importar los datos locales a la nube", ex);
        }
    }

    private static boolean estaVacia(Connection c) throws SQLException {
        for (String tabla : TABLAS) {
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
                if (rs.next() && rs.getLong(1) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int copiarTabla(Connection origen, Connection destino, String tabla) throws SQLException {
        // solo las columnas que existen en ambas bases (una base antigua puede no tener las nuevas)
        List<String> columnas = new ArrayList<>(columnas(origen, tabla));
        columnas.retainAll(columnas(destino, tabla));
        if (columnas.isEmpty()) {
            return 0;
        }
        String lista = String.join(", ", columnas);
        String marcadores = String.join(", ", java.util.Collections.nCopies(columnas.size(), "?"));
        int filas = 0;
        try (Statement st = origen.createStatement();
                ResultSet rs = st.executeQuery("SELECT " + lista + " FROM " + tabla + " ORDER BY id");
                PreparedStatement ps = destino.prepareStatement("INSERT INTO " + tabla + " (" + lista + ") VALUES (" + marcadores + ")")) {
            while (rs.next()) {
                for (int i = 1; i <= columnas.size(); i++) {
                    Object valor = rs.getObject(i);
                    if (valor instanceof BigDecimal decimal) {
                        valor = decimal.doubleValue();
                    }
                    ps.setObject(i, valor);
                }
                ps.addBatch();
                filas++;
            }
            ps.executeBatch();
        }
        return filas;
    }

    private static Set<String> columnas(Connection c, String tabla) throws SQLException {
        Set<String> nombres = new LinkedHashSet<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + tabla + " WHERE 1 = 0")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                nombres.add(md.getColumnName(i).toLowerCase());
            }
        }
        return nombres;
    }

    /** En PostgreSQL los id se generan con secuencias: se adelantan al ultimo id copiado. */
    private static void reiniciarSecuencias(Connection postgres) throws SQLException {
        try (Statement st = postgres.createStatement()) {
            for (String tabla : TABLAS) {
                st.execute("SELECT setval(pg_get_serial_sequence('" + tabla + "', 'id'), "
                        + "COALESCE((SELECT MAX(id) FROM " + tabla + "), 1), "
                        + "(SELECT MAX(id) IS NOT NULL FROM " + tabla + "))");
            }
        }
    }

    private static boolean esPostgres(Connection c) throws SQLException {
        return c.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");
    }
}
