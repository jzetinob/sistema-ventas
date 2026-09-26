package com.josue.ventas.movil.datos;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda la sesion en el telefono (token temporal, nombre y rol) para no
 * pedir usuario y contrasena cada vez que se abre la app. La contrasena
 * nunca se guarda. El token vence a las 8 horas (lo controla la base).
 */
public final class Sesion {

    private static final String ARCHIVO = "sesion";

    private Sesion() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static void guardar(Context c, String token, String nombre, String rol) {
        prefs(c).edit().putString("token", token).putString("nombre", nombre).putString("rol", rol).apply();
    }

    public static String getToken(Context c) {
        return prefs(c).getString("token", null);
    }

    public static String getNombre(Context c) {
        return prefs(c).getString("nombre", "");
    }

    public static String getRol(Context c) {
        return prefs(c).getString("rol", "");
    }

    public static boolean activa(Context c) {
        return getToken(c) != null;
    }

    public static void cerrar(Context c) {
        prefs(c).edit().clear().apply();
    }
}
