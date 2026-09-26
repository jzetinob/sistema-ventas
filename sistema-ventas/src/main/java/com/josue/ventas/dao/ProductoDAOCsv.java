/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.dao;

import com.josue.ventas.modelo.Producto;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josue zetino
 */
public class ProductoDAOCsv implements ProductoDAO {

    private static final String DIRECTORIO = "datos";
    private static final String ARCHIVO_PRODUCTOS = DIRECTORIO + File.separator + "productos.csv";

    private static final ProductoDAOCsv instancia = new ProductoDAOCsv();

    private final List<Producto> productos = new ArrayList<>();
    private int siguienteId = 1;

    private ProductoDAOCsv() {
        cargar();
    }

    public static ProductoDAOCsv getInstancia() {
        return instancia;
    }

    private void cargar() {
        File archivo = new File(ARCHIVO_PRODUCTOS);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = CsvUtil.dividirLinea(linea);
                Producto p = new Producto();
                p.setId(Integer.parseInt(campos[0]));
                p.setCodigo(campos[1]);
                p.setNombre(campos[2]);
                p.setPrecio(Double.parseDouble(campos[3]));
                productos.add(p);
                if (p.getId() >= siguienteId) {
                    siguienteId = p.getId() + 1;
                }
            }
        } catch (IOException ex) {
        }
    }

    private void escribir() {
        File dir = new File(DIRECTORIO);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_PRODUCTOS))) {
            for (Producto p : productos) {
                String linea = String.join(";",
                        String.valueOf(p.getId()),
                        CsvUtil.escape(p.getCodigo()),
                        CsvUtil.escape(p.getNombre()),
                        String.valueOf(p.getPrecio()));
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException ex) {
        }
    }

    @Override
    public void guardar(Producto producto) {
        if (producto.getId() == 0) {
            producto.setId(siguienteId++);
        }
        productos.add(producto);
        escribir();
    }

    @Override
    public List<Producto> listar() {
        return productos;
    }

    @Override
    public void actualizar(Producto producto) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == producto.getId()) {
                productos.set(i, producto);
                break;
            }
        }
        escribir();
    }

    @Override
    public boolean eliminar(int id) {
        productos.removeIf(producto -> producto.getId() == id);
        escribir();
        return true;
    }

    @Override
    public boolean existeCodigo(String codigo) {
        for (Producto p : productos) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return true;
            }
        }
        return false;
    }
}
