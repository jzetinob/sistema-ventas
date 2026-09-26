/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.CompraController;
import com.josue.ventas.controlador.FacturaController;
import com.josue.ventas.controlador.ProductoController;
import com.josue.ventas.modelo.Compra;
import com.josue.ventas.modelo.Factura;
import com.josue.ventas.modelo.Producto;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JOptionPane;

/**
 * Reportes de resumen del menu "Reportes". Cada uno arma un ReporteHtml
 * con los datos de los controladores y lo abre en el navegador.
 *
 * @author josue zetino
 */
public final class Reportes {

    private Reportes() {
    }

    public static void inventario(Component padre) {
        ReporteHtml r = new ReporteHtml("Inventario de productos")
                .columnas("Código", "Producto", "Categoría", "Precio", "Existencia", "Valor en inventario");
        double valorTotal = 0;
        int unidades = 0;
        for (Producto p : new ProductoController().GetProductos()) {
            double valor = p.getPrecio() * p.getExistencia();
            valorTotal += valor;
            unidades += p.getExistencia();
            r.fila(p.getCodigo(), p.getNombre(), nombreCategoria(p), dinero(p.getPrecio()), p.getExistencia(), dinero(valor));
        }
        r.resumen("Unidades en inventario", String.valueOf(unidades))
                .resumen("Valor del inventario", "Q " + dinero(valorTotal))
                .abrir(padre);
    }

    public static void existenciaBaja(Component padre) {
        String texto = JOptionPane.showInputDialog(padre, "Mostrar productos con existencia menor o igual a:", "5");
        if (texto == null) {
            return;
        }
        int limite;
        try {
            limite = Integer.parseInt(texto.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(padre, "Escriba un número entero.");
            return;
        }
        ReporteHtml r = new ReporteHtml("Productos con existencia baja")
                .subtitulo("Existencia menor o igual a " + limite)
                .columnas("Código", "Producto", "Categoría", "Existencia");
        int cantidad = 0;
        for (Producto p : new ProductoController().GetProductos()) {
            if (p.getExistencia() <= limite) {
                r.fila(p.getCodigo(), p.getNombre(), nombreCategoria(p), p.getExistencia());
                cantidad++;
            }
        }
        r.resumen("Productos por reabastecer", String.valueOf(cantidad)).abrir(padre);
    }

    public static void ventasPorPeriodo(Component padre) {
        LocalDate[] periodo = pedirPeriodo(padre);
        if (periodo == null) {
            return;
        }
        ReporteHtml r = new ReporteHtml("Ventas por período")
                .subtitulo("Del " + periodo[0] + " al " + periodo[1])
                .columnas("Factura", "Fecha", "NIT", "Cliente", "Total");
        double total = 0;
        int facturas = 0;
        for (Factura f : new FacturaController().GetFacturas()) {
            if (f.getFecha() != null && !f.getFecha().isBefore(periodo[0]) && !f.getFecha().isAfter(periodo[1])) {
                r.fila(f.getNumeroFactura(), f.getFecha(), f.getNit(), f.getNombreCliente(), dinero(f.getTotal()));
                total += f.getTotal();
                facturas++;
            }
        }
        r.resumen("Facturas", String.valueOf(facturas))
                .resumen("Total vendido", "Q " + dinero(total))
                .abrir(padre);
    }

    public static void comprasPorProveedor(Component padre) {
        LocalDate[] periodo = pedirPeriodo(padre);
        if (periodo == null) {
            return;
        }
        Map<String, double[]> porProveedor = new TreeMap<>();
        for (Compra c : new CompraController().GetCompras()) {
            if (!c.getFecha().isBefore(periodo[0]) && !c.getFecha().isAfter(periodo[1])) {
                String clave = c.getProveedor().getNit() + "\t" + c.getProveedor().getNombre();
                double[] acumulado = porProveedor.computeIfAbsent(clave, k -> new double[2]);
                acumulado[0]++;
                acumulado[1] += c.getTotal();
            }
        }
        ReporteHtml r = new ReporteHtml("Compras por proveedor")
                .subtitulo("Del " + periodo[0] + " al " + periodo[1])
                .columnas("NIT", "Proveedor", "Compras", "Total comprado");
        double total = 0;
        for (Map.Entry<String, double[]> e : porProveedor.entrySet()) {
            String[] proveedor = e.getKey().split("\t", 2);
            r.fila(proveedor[0], proveedor[1], (int) e.getValue()[0], dinero(e.getValue()[1]));
            total += e.getValue()[1];
        }
        r.resumen("Total comprado", "Q " + dinero(total)).abrir(padre);
    }

    /** Pide fecha inicial y final (yyyy-mm-dd). Por defecto, el mes actual. */
    private static LocalDate[] pedirPeriodo(Component padre) {
        LocalDate hoy = LocalDate.now();
        javax.swing.JTextField desde = new javax.swing.JTextField(hoy.withDayOfMonth(1).toString(), 10);
        javax.swing.JTextField hasta = new javax.swing.JTextField(hoy.toString(), 10);
        Object[] campos = {"Desde (aaaa-mm-dd):", desde, "Hasta (aaaa-mm-dd):", hasta};
        if (JOptionPane.showConfirmDialog(padre, campos, "Período del reporte",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            LocalDate inicio = LocalDate.parse(desde.getText().trim());
            LocalDate fin = LocalDate.parse(hasta.getText().trim());
            if (fin.isBefore(inicio)) {
                JOptionPane.showMessageDialog(padre, "La fecha final no puede ser anterior a la inicial.");
                return null;
            }
            return new LocalDate[]{inicio, fin};
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(padre, "Escriba las fechas con el formato aaaa-mm-dd (ejemplo: 2026-09-01).");
            return null;
        }
    }

    private static String nombreCategoria(Producto p) {
        return p.getCategoria() != null ? p.getCategoria().getNombre() : "";
    }

    private static String dinero(double valor) {
        return String.format("%.2f", valor);
    }
}
