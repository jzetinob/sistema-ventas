package com.josue.ventas.movil.vista;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.modelo.Cliente;
import java.util.List;

/** Consulta de clientes por NIT o nombre. */
public class ClientesActivity extends ActividadBase {

    private AdaptadorFilas<Cliente> adaptador;
    private ProgressBar progreso;
    private TextView lblVacio;
    private EditText txtBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        progreso = findViewById(R.id.progreso);
        lblVacio = findViewById(R.id.lblVacio);
        txtBuscar = findViewById(R.id.txtBuscar);
        txtBuscar.setHint(R.string.buscar_cliente);

        adaptador = new AdaptadorFilas<>(this, new AdaptadorFilas.Formato<Cliente>() {
            @Override
            public String titulo(Cliente c) {
                return c.getNombre();
            }

            @Override
            public String detalle(Cliente c) {
                StringBuilder sb = new StringBuilder("NIT ").append(c.getNit());
                if (!c.getTelefono().isEmpty()) {
                    sb.append("  ·  Tel. ").append(c.getTelefono());
                }
                if (!c.getDireccion().isEmpty()) {
                    sb.append("\n").append(c.getDireccion());
                }
                return sb.toString();
            }
        });
        ((ListView) findViewById(R.id.lista)).setAdapter(adaptador);

        txtBuscar.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                buscar();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        buscar(); // al volver de "Nuevo cliente" se ve el cliente registrado
    }

    private void buscar() {
        visible(progreso, true);
        ApiSupabase.getInstancia().clientes(token(), txtBuscar.getText().toString().trim(),
                new ApiSupabase.Respuesta<List<Cliente>>() {
                    @Override
                    public void exito(List<Cliente> clientes) {
                        visible(progreso, false);
                        visible(lblVacio, clientes.isEmpty());
                        adaptador.setElementos(clientes);
                    }

                    @Override
                    public void error(String mensaje, boolean sesionVencida) {
                        visible(progreso, false);
                        mostrarError(mensaje, sesionVencida);
                    }
                });
    }
}
