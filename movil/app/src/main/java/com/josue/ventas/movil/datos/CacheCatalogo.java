package com.josue.ventas.movil.datos;

import android.content.Context;
import android.content.SharedPreferences;
import com.josue.ventas.movil.modelo.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Copia del catalogo de productos guardada en el telefono. Cada vez que se
 * consulta el catalogo completo con internet, se guarda; si luego no hay
 * conexion, la pantalla de productos muestra esta copia (con su fecha).
 */
public final class CacheCatalogo {

    private static final String ARCHIVO = "catalogo";

    private CacheCatalogo() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static void guardar(Context c, JSONArray productos) {
        prefs(c).edit().putString("productos", productos.toString())
                .putLong("guardado", System.currentTimeMillis()).apply();
    }

    public static boolean existe(Context c) {
        return prefs(c).contains("productos");
    }

    /** Momento en que se guardo la copia (milisegundos), o 0 si no hay copia. */
    public static long guardadoEn(Context c) {
        return prefs(c).getLong("guardado", 0);
    }

    /** Productos de la copia que contienen el texto buscado en el codigo o el nombre. */
    public static List<Producto> buscar(Context c, String texto) {
        List<Producto> resultado = new ArrayList<>();
        String json = prefs(c).getString("productos", null);
        if (json == null) {
            return resultado;
        }
        String filtro = texto.toLowerCase(Locale.ROOT);
        try {
            for (Producto p : ApiSupabase.productosDesdeJson(new JSONArray(json))) {
                if (filtro.isEmpty() || p.getCodigo().toLowerCase(Locale.ROOT).contains(filtro)
                        || p.getNombre().toLowerCase(Locale.ROOT).contains(filtro)) {
                    resultado.add(p);
                }
            }
        } catch (JSONException ex) {
            prefs(c).edit().clear().apply(); // copia danada: se descarta
        }
        return resultado;
    }

    public static void borrar(Context c) {
        prefs(c).edit().clear().apply();
    }
}
