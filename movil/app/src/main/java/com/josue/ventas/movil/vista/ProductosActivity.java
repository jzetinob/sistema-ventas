package com.josue.ventas.movil.vista;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.modelo.Producto;
import java.util.List;
import java.util.Locale;

/** Consulta de productos: precio y existencia al momento, con busqueda. */
public class ProductosActivity extends ActividadBase {

    private static final long ESPERA_BUSQUEDA_MS = 400;

    private AdaptadorFilas<Producto> adaptador;
    private ProgressBar progreso;
    private TextView lblVacio;
    private final Handler temporizador = new Handler(Looper.getMainLooper());
    private Runnable busquedaPendiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        progreso = findViewById(R.id.progreso);
        lblVacio = findViewById(R.id.lblVacio);
        EditText txtBuscar = findViewById(R.id.txtBuscar);
        txtBuscar.setHint(R.string.buscar_producto);

        int colorAlerta = getColor(R.color.alerta);
        adaptador = new AdaptadorFilas<>(this, new AdaptadorFilas.Formato<Producto>() {
            @Override
            public String titulo(Producto p) {
                return p.getCodigo() + " - " + p.getNombre();
            }

            @Override
            public String detalle(Producto p) {
                String categoria = p.getCategoria().isEmpty() ? "" : p.getCategoria() + "  ·  ";
                return String.format(Locale.US, "%sQ %.2f  ·  Existencia: %d%s", categoria, p.getPrecio(),
                        p.getExistencia(), p.tieneExistenciaBaja() ? " (baja)" : "");
            }

            @Override
            public int colorDetalle(Producto p) {
                return p.tieneExistenciaBaja() ? colorAlerta : 0;
            }
        });
        ((ListView) findViewById(R.id.lista)).setAdapter(adaptador);

        // busca mientras se escribe, esperando a que se deje de teclear
        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                temporizador.removeCallbacks(busquedaPendiente);
                busquedaPendiente = () -> buscar(s.toString().trim());
                temporizador.postDelayed(busquedaPendiente, ESPERA_BUSQUEDA_MS);
            }
        });
        buscar("");
    }

    private void buscar(String texto) {
        visible(progreso, true);
        ApiSupabase.getInstancia().productos(token(), texto, new ApiSupabase.Respuesta<List<Producto>>() {
            @Override
            public void exito(List<Producto> productos) {
                visible(progreso, false);
                visible(lblVacio, productos.isEmpty());
                adaptador.setElementos(productos);
            }

            @Override
            public void error(String mensaje, boolean sesionVencida) {
                visible(progreso, false);
                mostrarError(mensaje, sesionVencida);
            }
        });
    }
}
