package com.josue.ventas.movil.vista;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import com.josue.ventas.movil.datos.Sesion;

/**
 * Base de las pantallas que requieren sesion (herencia de ActividadTema,
 * que ademas aplica el tema segun la hora): si el usuario no
 * ha iniciado sesion, o su sesion vencio, lo regresa al inicio de sesion.
 */
public abstract class ActividadBase extends ActividadTema {

    @Override
    protected void onResume() {
        super.onResume();
        if (!Sesion.activa(this)) {
            irAlLogin();
        }
    }

    protected String token() {
        return Sesion.getToken(this);
    }

    /** Muestra el error; si la sesion vencio, la cierra y vuelve al login. */
    protected void mostrarError(String mensaje, boolean sesionVencida) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        if (sesionVencida) {
            Sesion.cerrar(this);
            irAlLogin();
        }
    }

    protected void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    protected static void visible(View vista, boolean mostrar) {
        vista.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }
}
