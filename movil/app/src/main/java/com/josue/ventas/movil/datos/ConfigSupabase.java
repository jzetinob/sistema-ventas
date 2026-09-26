package com.josue.ventas.movil.datos;

/**
 * Datos del proyecto de Supabase (base de datos en la nube compartida con
 * la app de escritorio).
 *
 * La clave "publishable" esta hecha para ir dentro de apps publicas: por si
 * sola no permite leer ninguna tabla (RLS activo). La app solo puede llamar
 * las funciones app_* de la base, que exigen iniciar sesion.
 * NUNCA poner aqui la clave "secret" ni la contrasena de la base de datos.
 */
public final class ConfigSupabase {

    public static final String URL = "https://nlgvnwonkokdhbgpuoqe.supabase.co";
    public static final String CLAVE_PUBLICA = "sb_publishable_7FIt6ku03KIC_bOEPdkiHQ_8LxMsBtF";

    private ConfigSupabase() {
    }
}
