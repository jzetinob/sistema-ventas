/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Factura;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class FacturaDAOCsv implements FacturaDAO {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FacturaDAOCsv.class.getName());

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String DIRECTORIO = "datos";
    private static final String ARCHIVO_FACTURAS = DIRECTORIO + File.separator + "facturas.csv";
    private static final String ARCHIVO_DETALLES = DIRECTORIO + File.separator + "detalles.csv";
    private static final String ARCHIVO_CONTADOR = DIRECTORIO + File.separator + "contador.txt";

    private static final FacturaDAOCsv instancia = new FacturaDAOCsv();

    private final List<Factura> facturas = new ArrayList<>();
    private int siguienteId = 1;
    private int siguienteCorrelativo = 1;

    private FacturaDAOCsv() {
        cargar();
    }

    public static FacturaDAOCsv getInstancia() {
        return instancia;
    }

    private void cargar() {
        File archivoFacturas = new File(ARCHIVO_FACTURAS);
        if (archivoFacturas.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivoFacturas))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (linea.isBlank()) {
                        continue;
                    }
                    String[] campos = CsvUtil.dividirLinea(linea);
                    Factura f = new Factura();
                    f.setId(Integer.parseInt(campos[0]));
                    f.setNumeroFactura(campos[1]);
                    f.setNit(campos[2]);
                    f.setNombreCliente(campos[3]);
                    try {
                        f.setFecha(parsearFecha(campos[4]));
                    } catch (Exception ex) {
                        f.setFecha(LocalDate.now());
                    }
                    f.setTotal(Double.parseDouble(campos[5]));
                    facturas.add(f);
                    if (f.getId() >= siguienteId) {
                        siguienteId = f.getId() + 1;
                    }
                }
            } catch (IOException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo cargar el archivo de facturas: " + ARCHIVO_FACTURAS, ex);
            }
        }
        File archivoDetalles = new File(ARCHIVO_DETALLES);
        if (archivoDetalles.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivoDetalles))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (linea.isBlank()) {
                        continue;
                    }
                    String[] campos = CsvUtil.dividirLinea(linea);
                    Factura f = buscarPorId(Integer.parseInt(campos[0]));
                    if (f != null) {
                        f.agregarDetalle(campos[1], Integer.parseInt(campos[2]), Double.parseDouble(campos[3]));
                    }
                }
            } catch (IOException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo cargar el archivo de detalles: " + ARCHIVO_DETALLES, ex);
            }
        }
        File archivoContador = new File(ARCHIVO_CONTADOR);
        if (archivoContador.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivoContador))) {
                String linea = br.readLine();
                if (linea != null && !linea.isBlank()) {
                    siguienteCorrelativo = Integer.parseInt(linea.trim());
                }
            } catch (IOException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo cargar el contador de facturas: " + ARCHIVO_CONTADOR, ex);
            }
        }
    }

    private void escribir() {
        File dir = new File(DIRECTORIO);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_FACTURAS))) {
            for (Factura f : facturas) {
                String linea = String.join(";",
                        String.valueOf(f.getId()),
                        CsvUtil.escape(f.getNumeroFactura()),
                        CsvUtil.escape(f.getNit() != null ? f.getNit() : ""),
                        CsvUtil.escape(f.getNombreCliente() != null ? f.getNombreCliente() : ""),
                        f.getFecha() != null ? FORMATO_FECHA.format(f.getFecha().atStartOfDay()) : "",
                        String.valueOf(f.getTotal()));
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo escribir el archivo de facturas: " + ARCHIVO_FACTURAS, ex);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_DETALLES))) {
            for (Factura f : facturas) {
                for (Object[] fila : f.getDetallesFilas()) {
                    String linea = String.join(";",
                            String.valueOf(f.getId()),
                            CsvUtil.escape(String.valueOf(fila[0])),
                            String.valueOf(fila[1]),
                            String.valueOf(fila[2]),
                            String.valueOf(fila[3]));
                    bw.write(linea);
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo escribir el archivo de detalles: " + ARCHIVO_DETALLES, ex);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_CONTADOR))) {
            bw.write(String.valueOf(siguienteCorrelativo));
        } catch (IOException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo escribir el contador de facturas: " + ARCHIVO_CONTADOR, ex);
        }
    }

    private Factura buscarPorId(int id) {
        for (Factura f : facturas) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    @Override
    public void guardar(Factura factura) {
        if (factura.getId() == 0) {
            factura.setId(siguienteId++);
        }
        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isBlank()) {
            factura.setNumeroFactura(String.format("FAC-%04d", siguienteCorrelativo++));
        } else {
            adelantarCorrelativo(factura.getNumeroFactura());
        }
        facturas.add(factura);
        escribir();
    }

    private void adelantarCorrelativo(String numeroFactura) {
        int ultimoGuion = numeroFactura.lastIndexOf('-');
        if (ultimoGuion == -1) {
            return;
        }
        try {
            int numero = Integer.parseInt(numeroFactura.substring(ultimoGuion + 1));
            if (numero >= siguienteCorrelativo) {
                siguienteCorrelativo = numero + 1;
            }
        } catch (NumberFormatException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo interpretar el número de factura: {0}", numeroFactura);
        }
    }

    @Override
    public List<Factura> listar() {
        return facturas;
    }

    @Override
    public void actualizar(Factura factura) {
        for (int i = 0; i < facturas.size(); i++) {
            if (facturas.get(i).getId() == factura.getId()) {
                facturas.set(i, factura);
                break;
            }
        }
        escribir();
    }

    @Override
    public void eliminar(int id) {
        facturas.removeIf(factura -> factura.getId() == id);
        escribir();
    }

    @Override
    public void eliminarConDetalles(int id) {
        eliminar(id);
    }

    @Override
    public String obtenerSiguienteNumeroFactura() {
        return String.format("FAC-%04d", siguienteCorrelativo);
    }

    private LocalDate parsearFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Fecha vacia");
        }
        if (texto.length() == 10) {
            return LocalDate.parse(texto);
        }
        return LocalDateTime.parse(texto, FORMATO_FECHA).toLocalDate();
    }
}
