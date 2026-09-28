package com.josue.ventas.movil.vista;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import java.util.List;

/**
 * Dialogo generico para buscar y elegir un elemento (cliente, producto...).
 * Recibe como buscar los datos y que hacer con el elegido; asi la misma
 * clase sirve para cualquier tipo T.
 */
final class DialogoBusqueda<T> {

    /** Como se buscan los datos (normalmente una llamada a ApiSupabase). */
    interface Buscador<T> {
        void buscar(String texto, ApiSupabase.Respuesta<List<T>> respuesta);
    }

    /** Que hacer con el elemento elegido. */
    interface AlElegir<T> {
        void elegido(T elemento);
    }

    private static final long ESPERA_MS = 350;

    private DialogoBusqueda() {
    }

    static <T> void mostrar(ActividadBase pantalla, String titulo, String pista, AdaptadorFilas.Formato<T> formato,
            Buscador<T> buscador, AlElegir<T> alElegir) {
        int margen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, pantalla.getResources().getDisplayMetrics());
        LinearLayout contenido = new LinearLayout(pantalla);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(margen, margen / 2, margen, 0);

        EditText txtBuscar = new EditText(pantalla);
        txtBuscar.setHint(pista);
        txtBuscar.setSingleLine(true);
        contenido.addView(txtBuscar);

        TextView lblEstado = new TextView(pantalla);
        lblEstado.setPadding(0, margen / 2, 0, margen / 2);
        lblEstado.setTextColor(pantalla.getColor(R.color.texto_suave));
        contenido.addView(lblEstado);

        ListView lista = new ListView(pantalla);
        lista.setDivider(null);
        lista.setDividerHeight(margen / 2);
        AdaptadorFilas<T> adaptador = new AdaptadorFilas<>(pantalla, formato);
        lista.setAdapter(adaptador);
        contenido.addView(lista, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (pantalla.getResources().getDisplayMetrics().heightPixels * 0.5)));

        AlertDialog dialogo = new AlertDialog.Builder(pantalla)
                .setTitle(titulo)
                .setView(contenido)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        lista.setOnItemClickListener((padre, vista, posicion, id) -> {
            dialogo.dismiss();
            alElegir.elegido(adaptador.getItem(posicion));
        });

        Runnable[] pendiente = new Runnable[1];
        Handler temporizador = new Handler(Looper.getMainLooper());
        Runnable cargar = () -> {
            lblEstado.setText(R.string.buscando);
            buscador.buscar(txtBuscar.getText().toString().trim(), new ApiSupabase.Respuesta<List<T>>() {
                @Override
                public void exito(List<T> elementos) {
                    lblEstado.setText(elementos.isEmpty() ? pantalla.getString(R.string.sin_resultados) : pantalla.getResources().getQuantityString(R.plurals.n_resultados, elementos.size(), elementos.size()));
                    adaptador.setElementos(elementos);
                }

                @Override
                public void error(String mensaje, boolean sesionVencida) {
                    lblEstado.setText(mensaje);
                    if (sesionVencida) {
                        dialogo.dismiss();
                        pantalla.mostrarError(mensaje, true);
                    }
                }
            });
        };
        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                temporizador.removeCallbacks(pendiente[0]);
                pendiente[0] = cargar;
                temporizador.postDelayed(cargar, ESPERA_MS);
            }
        });
        dialogo.show();
        cargar.run();
    }
}
