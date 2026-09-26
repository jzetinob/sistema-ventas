package com.josue.ventas.movil.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.datos.Sesion;

/** Menu principal de la app movil. */
public class MenuActivity extends ActividadBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setTitle(R.string.app_name);

        ((TextView) findViewById(R.id.lblBienvenida)).setText("Hola, " + Sesion.getNombre(this));
        ((TextView) findViewById(R.id.lblRol)).setText("Rol: " + Sesion.getRol(this));

        findViewById(R.id.btnProductos).setOnClickListener(v -> abrir(ProductosActivity.class));
        findViewById(R.id.btnClientes).setOnClickListener(v -> abrir(ClientesActivity.class));
        findViewById(R.id.btnNuevoCliente).setOnClickListener(v -> abrir(NuevoClienteActivity.class));
        findViewById(R.id.btnVentas).setOnClickListener(v -> abrir(VentasActivity.class));
        findViewById(R.id.btnSalir).setOnClickListener(v -> cerrarSesion());
    }

    private void abrir(Class<?> pantalla) {
        startActivity(new Intent(this, pantalla));
    }

    private void cerrarSesion() {
        ApiSupabase.getInstancia().logout(token());
        Sesion.cerrar(this);
        irAlLogin();
    }
}
