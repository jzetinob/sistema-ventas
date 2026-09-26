package com.josue.ventas.movil.vista;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;

/**
 * Registro de un cliente desde el telefono. Queda en la misma base de
 * datos, asi que aparece de inmediato en la app de escritorio.
 * La base vuelve a validar todo (NIT, duplicados), aunque aqui ya se revise.
 */
public class NuevoClienteActivity extends ActividadBase {

    private EditText txtNit;
    private EditText txtNombre;
    private EditText txtDireccion;
    private EditText txtTelefono;
    private Button btnGuardar;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_cliente);
        txtNit = findViewById(R.id.txtNit);
        txtNombre = findViewById(R.id.txtNombre);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtTelefono = findViewById(R.id.txtTelefono);
        btnGuardar = findViewById(R.id.btnGuardar);
        progreso = findViewById(R.id.progreso);
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String nit = txtNit.getText().toString().trim();
        String nombre = txtNombre.getText().toString().trim();
        if (!nit.replace("-", "").replace(" ", "").matches("\\d{8,13}")) {
            txtNit.setError("El NIT debe tener entre 8 y 13 dígitos");
            return;
        }
        if (nombre.isEmpty()) {
            txtNombre.setError("Escriba el nombre");
            return;
        }
        btnGuardar.setEnabled(false);
        visible(progreso, true);
        ApiSupabase.getInstancia().registrarCliente(token(), nit, nombre,
                txtDireccion.getText().toString().trim(), txtTelefono.getText().toString().trim(),
                new ApiSupabase.Respuesta<Void>() {
                    @Override
                    public void exito(Void nada) {
                        Toast.makeText(NuevoClienteActivity.this, "Cliente registrado.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void error(String mensaje, boolean sesionVencida) {
                        btnGuardar.setEnabled(true);
                        visible(progreso, false);
                        mostrarError(mensaje, sesionVencida);
                    }
                });
    }
}
