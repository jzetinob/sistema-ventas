package com.josue.ventas.movil.vista;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Base de todas las pantallas: les aplica el tema claro u oscuro segun la
 * hora (TemaPorHora). Si la pantalla esta abierta cuando llegan las 06:00 o
 * las 18:00, se vuelve a crear con el tema nuevo.
 */
public abstract class ActividadTema extends Activity {

    private static final long REVISAR_CADA_MS = 60_000;

    private final Handler temporizador = new Handler(Looper.getMainLooper());
    private final Runnable revisarTema = new Runnable() {
        @Override
        public void run() {
            if (TemaPorHora.cambio(ActividadTema.this)) {
                recreate();
            } else {
                temporizador.postDelayed(this, REVISAR_CADA_MS);
            }
        }
    };

    @Override
    protected void attachBaseContext(Context base) {
        // se ejecuta antes de onCreate: el tema y los colores ya salen de values/ o values-night/
        super.attachBaseContext(TemaPorHora.aplicar(base));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TemaPorHora.cambio(this)) {
            recreate(); // p. ej. la app estaba en segundo plano y ya paso la hora de cambio
            return;
        }
        temporizador.postDelayed(revisarTema, REVISAR_CADA_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        temporizador.removeCallbacks(revisarTema);
    }
}
