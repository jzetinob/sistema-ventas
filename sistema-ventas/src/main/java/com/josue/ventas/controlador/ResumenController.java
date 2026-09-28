/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.modelo.Factura;
import com.josue.ventas.modelo.FacturaDetalle;
import com.josue.ventas.modelo.Producto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula los datos del tablero "Resumen" (indicadores y graficos) a partir
 * de las facturas y los productos. Usa los otros controladores, asi que
 * funciona igual con la base local (SQLite) y con la nube (PostgreSQL).
 *
 * @author josue zetino
 */
public class ResumenController {

    /** Un producto con las unidades y el monto vendidos en el periodo. */
    public static class ProductoVendido {
        public final String producto;
        public final int cantidad;
        public final double total;

        ProductoVendido(String producto, int cantidad, double total) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.total = total;
        }
    }

    private final List<Factura> facturas;
    private final List<Producto> productos;
    private final LocalDate hoy;

    /** Toma una "foto" de los datos al crearse: todos los calculos usan esa misma foto. */
    public ResumenController(LocalDate hoy) {
        this.hoy = hoy;
        this.facturas = new FacturaController().GetFacturas();
        this.productos = new ProductoController().GetProductos();
    }

    public double VentasDelDia() {
        return totalEntre(hoy, hoy);
    }

    public int FacturasDelDia() {
        int n = 0;
        for (Factura f : facturas) {
            if (hoy.equals(f.getFecha())) {
                n++;
            }
        }
        return n;
    }

    public double VentasDelMes() {
        return totalEntre(hoy.withDayOfMonth(1), hoy);
    }

    /** Total vendido en cada uno de los ultimos n dias (incluye los dias sin ventas, en cero). */
    public Map<LocalDate, Double> VentasPorDia(int dias) {
        Map<LocalDate, Double> porDia = new LinkedHashMap<>();
        for (int i = dias - 1; i >= 0; i--) {
            porDia.put(hoy.minusDays(i), 0.0);
        }
        for (Factura f : facturas) {
            if (f.getFecha() != null && porDia.containsKey(f.getFecha())) {
                porDia.merge(f.getFecha(), f.getTotal(), Double::sum);
            }
        }
        return porDia;
    }

    /** Los productos con mas unidades vendidas en el mes, de mayor a menor. */
    public List<ProductoVendido> MasVendidosDelMes(int limite) {
        Map<String, int[]> unidades = new HashMap<>();
        Map<String, Double> montos = new HashMap<>();
        LocalDate inicio = hoy.withDayOfMonth(1);
        for (Factura f : facturas) {
            if (f.getFecha() == null || f.getFecha().isBefore(inicio) || f.getFecha().isAfter(hoy)) {
                continue;
            }
            for (FacturaDetalle d : f.getDetalles()) {
                String nombre = d.getProducto() != null ? d.getProducto().getNombre() : "";
                unidades.computeIfAbsent(nombre, k -> new int[1])[0] += d.getCantidad();
                montos.merge(nombre, d.calcularSubtotal(), Double::sum);
            }
        }
        List<ProductoVendido> lista = new ArrayList<>();
        for (Map.Entry<String, int[]> e : unidades.entrySet()) {
            lista.add(new ProductoVendido(e.getKey(), e.getValue()[0], montos.get(e.getKey())));
        }
        lista.sort(Comparator.comparingInt((ProductoVendido p) -> p.cantidad).reversed()
                .thenComparing(p -> p.producto));
        return lista.size() > limite ? lista.subList(0, limite) : lista;
    }

    /** Productos con existencia menor o igual al limite, de menor a mayor existencia. */
    public List<Producto> ExistenciaBaja(int limite) {
        List<Producto> lista = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getExistencia() <= limite) {
                lista.add(p);
            }
        }
        lista.sort(Comparator.comparingInt(Producto::getExistencia).thenComparing(Producto::getNombre));
        return lista;
    }

    private double totalEntre(LocalDate desde, LocalDate hasta) {
        double total = 0;
        for (Factura f : facturas) {
            if (f.getFecha() != null && !f.getFecha().isBefore(desde) && !f.getFecha().isAfter(hasta)) {
                total += f.getTotal();
            }
        }
        return total;
    }
}
