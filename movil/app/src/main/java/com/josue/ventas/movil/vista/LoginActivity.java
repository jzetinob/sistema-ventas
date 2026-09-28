package com.josue.ventas.movil.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.datos.Sesion;
import org.json.JSONObject;

/** Inicio de sesion con los mismos usuarios de la app de escritorio. */
public class LoginActivity extends ActividadTema {

    private EditText txtUsuario;
    private EditText txtClave;
    private Button btnEntrar;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Sesion.activa(this)) {
            abrirMenu();
            return;
        }
        setContentView(R.layout.activity_login);
        txtUsuario = findViewById(R.id.txtUsuario);
        txtClave = findViewById(R.id.txtClave);
        btnEntrar = findViewById(R.id.btnEntrar);
        progreso = findViewById(R.id.progreso);

        btnEntrar.setOnClickListener(v -> entrar());
        txtClave.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });
    }

    private void entrar() {
        String usuario = txtUsuario.getText().toString().trim();
        String clave = txtClave.getText().toString();
        if (usuario.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Escriba su usuario y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }
        cargando(true);
        ApiSupabase.getInstancia().login(usuario, clave, new ApiSupabase.Respuesta<JSONObject>() {
            @Override
            public void exito(JSONObject datos) {
                Sesion.guardar(LoginActivity.this, datos.optString("token"), datos.optString("nombre"), datos.optString("rol"));
                abrirMenu();
            }

            @Override
            public void error(String mensaje, boolean sesionVencida) {
                cargando(false);
                txtClave.setText("");
                Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargando(boolean activo) {
        btnEntrar.setEnabled(!activo);
        progreso.setVisibility(activo ? ProgressBar.VISIBLE : ProgressBar.GONE);
    }

    private void abrirMenu() {
        startActivity(new Intent(this, MenuActivity.class));
        finish();
    }
}
