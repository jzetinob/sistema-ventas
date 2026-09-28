package com.josue.ventas.movil.vista;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Calendar;

/**
 * Elige el tema segun la hora del telefono: claro de 06:00 a 17:59 y oscuro
 * de 18:00 a 05:59.
 *
 * Android elige los recursos de values/ o values-night/ segun el "modo
 * noche" de la configuracion. Aqui se crea una copia de la configuracion con
 * el modo noche activado o no segun la hora; asi los colores y el tema salen
 * de la carpeta que corresponde, sin importar el ajuste del sistema.
 */
final class TemaPorHora {

    static final int HORA_INICIO_DIA = 6;
    static final int HORA_INICIO_NOCHE = 18;

    private TemaPorHora() {
    }

    static boolean esDeNoche() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hora >= HORA_INICIO_NOCHE || hora < HORA_INICIO_DIA;
    }

    /** Devuelve un contexto igual al recibido pero con el modo noche que toca a esta hora. */
    static Context aplicar(Context base) {
        Configuration config = new Configuration(base.getResources().getConfiguration());
        int modo = esDeNoche() ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | modo;
        return base.createConfigurationContext(config);
    }

    /** true si la pantalla se creo con un tema distinto al que corresponde ahora. */
    static boolean cambio(Context pantalla) {
        boolean pantallaDeNoche = (pantalla.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        return pantallaDeNoche != esDeNoche();
    }
}
