package com.josue.ventas.movil.vista;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Formatos que comparten las pantallas (dinero y fechas).
 * Se usa SimpleDateFormat/Calendar porque java.time necesita Android 8 y la
 * app funciona desde Android 7.
 */
final class Formatos {

    private static final Locale ESPANOL = Locale.forLanguageTag("es-GT");

    private Formatos() {
    }

    /** 1234.5 -> "Q 1,234.50" */
    static String dinero(double valor) {
        return String.format(Locale.US, "Q %,.2f", valor);
    }

    /** Fecha de hoy como la espera la base: aaaa-mm-dd. */
    static String hoy() {
        return aTexto(Calendar.getInstance());
    }

    static String aTexto(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    /** "2026-09-21" -> "lun 21" (etiqueta corta para el grafico de barras). */
    static String diaCorto(String fecha) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha);
            String texto = new SimpleDateFormat("EEE d", ESPANOL).format(d);
            return texto.replace(".", "");
        } catch (ParseException ex) {
            return fecha;
        }
    }

    /** "2026-09-21" -> "21/09/2026" */
    static String fechaLarga(String fecha) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(d);
        } catch (ParseException ex) {
            return fecha;
        }
    }

    /** Milisegundos -> "21/09 14:05" */
    static String momento(long milisegundos) {
        return new SimpleDateFormat("dd/MM HH:mm", Locale.US).format(new Date(milisegundos));
    }
}
