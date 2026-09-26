/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.Sesion;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 * Genera reportes en HTML (sin librerias externas) y los abre en el
 * navegador, desde donde se pueden imprimir o guardar como PDF.
 * Los archivos quedan en la carpeta "reportes" junto a la base de datos.
 *
 * Uso: new ReporteHtml("Titulo").columnas(...).fila(...).total(...).abrir(ventana)
 *
 * @author josue zetino
 */
public class ReporteHtml {

    private static final String CARPETA = "reportes";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final String titulo;
    private String subtitulo = "";
    private String[] columnas = new String[0];
    private final List<Object[]> filas = new ArrayList<>();
    private final List<String[]> resumen = new ArrayList<>();

    public ReporteHtml(String titulo) {
        this.titulo = titulo;
    }

    public ReporteHtml subtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
        return this;
    }

    public ReporteHtml columnas(String... columnas) {
        this.columnas = columnas;
        return this;
    }

    public ReporteHtml fila(Object... valores) {
        filas.add(valores);
        return this;
    }

    /** Agrega una linea al resumen del final (por ejemplo "Total", "Q 150.00"). */
    public ReporteHtml resumen(String etiqueta, String valor) {
        resumen.add(new String[]{etiqueta, valor});
        return this;
    }

    /** Toma las columnas y las filas visibles de una tabla (respeta la busqueda y el orden). */
    public static ReporteHtml desdeTabla(String titulo, JTable tabla) {
        ReporteHtml reporte = new ReporteHtml(titulo);
        String[] nombres = new String[tabla.getColumnCount()];
        for (int c = 0; c < nombres.length; c++) {
            nombres[c] = tabla.getColumnName(c);
        }
        reporte.columnas(nombres);
        for (int f = 0; f < tabla.getRowCount(); f++) {
            Object[] valores = new Object[nombres.length];
            for (int c = 0; c < nombres.length; c++) {
                valores[c] = tabla.getValueAt(f, c);
            }
            reporte.fila(valores);
        }
        reporte.resumen("Registros", String.valueOf(tabla.getRowCount()));
        return reporte;
    }

    public String generarHtml() {
        StringBuilder html = new StringBuilder();
        String usuario = Sesion.getInstancia().getUsuario() != null ? Sesion.getInstancia().getUsuario().getNombre() : "";
        html.append("<!DOCTYPE html>\n<html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<title>").append(escapar(titulo)).append("</title>")
                .append("<style>")
                .append("body{font-family:Segoe UI,Arial,sans-serif;margin:32px;color:#222}")
                .append("h1{margin:0 0 4px;font-size:22px}")
                .append(".meta{color:#666;font-size:13px;margin-bottom:18px}")
                .append("table{border-collapse:collapse;width:100%;font-size:13px}")
                .append("th{background:#2f4f6f;color:#fff;text-align:left;padding:6px 8px}")
                .append("td{padding:5px 8px;border-bottom:1px solid #ddd}")
                .append("tr:nth-child(even) td{background:#f5f7fa}")
                .append(".resumen{margin-top:16px;font-size:14px}.resumen b{display:inline-block;min-width:160px}")
                .append("@media print{body{margin:0}}")
                .append("</style></head><body>\n");
        html.append("<h1>Sistema de Ventas — ").append(escapar(titulo)).append("</h1>\n");
        html.append("<div class=\"meta\">");
        if (!subtitulo.isEmpty()) {
            html.append(escapar(subtitulo)).append("<br>");
        }
        html.append("Generado el ").append(LocalDateTime.now().format(FORMATO_FECHA));
        if (!usuario.isEmpty()) {
            html.append(" por ").append(escapar(usuario));
        }
        html.append("</div>\n<table><thead><tr>");
        for (String c : columnas) {
            html.append("<th>").append(escapar(c)).append("</th>");
        }
        html.append("</tr></thead><tbody>\n");
        for (Object[] fila : filas) {
            html.append("<tr>");
            for (Object valor : fila) {
                html.append("<td>").append(escapar(valor == null ? "" : String.valueOf(valor))).append("</td>");
            }
            html.append("</tr>\n");
        }
        if (filas.isEmpty()) {
            html.append("<tr><td colspan=\"").append(Math.max(1, columnas.length)).append("\">Sin registros</td></tr>\n");
        }
        html.append("</tbody></table>\n<div class=\"resumen\">");
        for (String[] r : resumen) {
            html.append("<div><b>").append(escapar(r[0])).append(":</b> ").append(escapar(r[1])).append("</div>");
        }
        html.append("</div>\n</body></html>\n");
        return html.toString();
    }

    /** Guarda el reporte y lo abre en el navegador. Devuelve el archivo, o null si fallo. */
    public File abrir(Component padre) {
        try {
            File carpeta = new File(CARPETA);
            carpeta.mkdirs();
            String nombre = titulo.toLowerCase().replaceAll("[^a-z0-9áéíóúñ]+", "-") + "-"
                    + LocalDateTime.now().format(FORMATO_ARCHIVO) + ".html";
            File archivo = new File(carpeta, nombre);
            Files.writeString(archivo.toPath(), generarHtml(), StandardCharsets.UTF_8);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(archivo.toURI());
            } else {
                JOptionPane.showMessageDialog(padre, "Reporte guardado en:\n" + archivo.getAbsolutePath());
            }
            return archivo;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(padre, "No se pudo generar el reporte: " + ex.getMessage(),
                    "Reporte", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Escapa los caracteres especiales para que ningun dato se interprete como HTML. */
    static String escapar(String texto) {
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
