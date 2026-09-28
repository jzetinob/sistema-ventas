package com.josue.ventas.movil.vista;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.datos.CacheCatalogo;
import com.josue.ventas.movil.modelo.Producto;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Consulta de productos: precio y existencia al momento, con busqueda.
 * Si no hay internet, muestra la ultima copia del catalogo guardada en el telefono.
 */
public class ProductosActivity extends ActividadBase {

    private static final long ESPERA_BUSQUEDA_MS = 400;

    private AdaptadorFilas<Producto> adaptador;
    private SwipeRefreshLayout deslizar;
    private TextView lblVacio;
    private TextView lblAviso;
    private EditText txtBuscar;
    private final Handler temporizador = new Handler(Looper.getMainLooper());
    private Runnable busquedaPendiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        lblVacio = findViewById(R.id.lblVacio);
        lblAviso = findViewById(R.id.lblAviso);
        txtBuscar = findViewById(R.id.txtBuscar);
        txtBuscar.setHint(R.string.buscar_producto);
        deslizar = findViewById(R.id.deslizar);
        deslizar.setColorSchemeColors(getColor(R.color.acento));
        deslizar.setOnRefreshListener(() -> buscar(txtBuscar.getText().toString().trim()));

        int colorAlerta = getColor(R.color.alerta);
        adaptador = new AdaptadorFilas<>(this, new AdaptadorFilas.Formato<Producto>() {
            @Override
            public String titulo(Producto p) {
                return p.getNombre();
            }

            @Override
            public String detalle(Producto p) {
                String categoria = p.getCategoria().isEmpty() ? "" : "  ·  " + p.getCategoria();
                String existencia = p.getExistencia() == 0 ? "Agotado" : "Existencia: " + p.getExistencia();
                return String.format(Locale.US, "%s%s\n%s%s", p.getCodigo(), categoria, existencia,
                        p.tieneExistenciaBaja() && p.getExistencia() > 0 ? " (baja)" : "");
            }

            @Override
            public String valor(Producto p) {
                return Formatos.dinero(p.getPrecio());
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
        deslizar.setRefreshing(true);
        buscar("");
    }

    private void buscar(String texto) {
        ApiSupabase.getInstancia().productos(token(), texto, new ApiSupabase.Respuesta<JSONArray>() {
            @Override
            public void exito(JSONArray json) {
                deslizar.setRefreshing(false);
                visible(lblAviso, false);
                if (texto.isEmpty()) {
                    CacheCatalogo.guardar(ProductosActivity.this, json); // copia para usar sin internet
                }
                try {
                    mostrar(ApiSupabase.productosDesdeJson(json));
                } catch (JSONException ex) {
                    mostrarError("Respuesta inesperada del servidor.", false);
                }
            }

            @Override
            public void error(String mensaje, boolean sesionVencida) {
                deslizar.setRefreshing(false);
                if (ApiSupabase.SIN_CONEXION.equals(mensaje) && CacheCatalogo.existe(ProductosActivity.this)) {
                    lblAviso.setText(getString(R.string.aviso_sin_conexion,
                            Formatos.momento(CacheCatalogo.guardadoEn(ProductosActivity.this))));
                    visible(lblAviso, true);
                    mostrar(CacheCatalogo.buscar(ProductosActivity.this, texto));
                } else {
                    mostrarError(mensaje, sesionVencida);
                }
            }
        });
    }

    private void mostrar(List<Producto> productos) {
        visible(lblVacio, productos.isEmpty());
        adaptador.setElementos(productos);
    }
}
