package com.josue.ventas.movil.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.datos.CacheCatalogo;
import com.josue.ventas.movil.datos.Sesion;
import com.josue.ventas.movil.modelo.Resumen;

/** Pantalla de inicio: indicadores del dia y menu en mosaico con iconos. */
public class MenuActivity extends ActividadBase {

    /** Un boton del menu: texto, icono, color de fondo y pantalla que abre. */
    private static final class Opcion {
        final int texto;
        final int icono;
        final int color;
        final Class<?> pantalla;

        Opcion(int texto, int icono, int color, Class<?> pantalla) {
            this.texto = texto;
            this.icono = icono;
            this.color = color;
            this.pantalla = pantalla;
        }
    }

    private static final Opcion[] OPCIONES = {
        new Opcion(R.string.titulo_resumen, R.drawable.ic_resumen, R.color.mosaico_1, ResumenActivity.class),
        new Opcion(R.string.titulo_nueva_venta, R.drawable.ic_nueva_venta, R.color.mosaico_2, NuevaVentaActivity.class),
        new Opcion(R.string.titulo_productos, R.drawable.ic_productos, R.color.mosaico_3, ProductosActivity.class),
        new Opcion(R.string.titulo_clientes, R.drawable.ic_clientes, R.color.mosaico_4, ClientesActivity.class),
        new Opcion(R.string.titulo_nuevo_cliente, R.drawable.ic_cliente_nuevo, R.color.mosaico_5, NuevoClienteActivity.class),
        new Opcion(R.string.titulo_ventas, R.drawable.ic_ventas, R.color.mosaico_6, VentasActivity.class),
    };

    private SwipeRefreshLayout deslizar;
    private TextView lblVentasHoy;
    private TextView lblFacturasHoy;
    private TextView lblExistenciaBaja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setTitle(R.string.app_name);

        ((TextView) findViewById(R.id.lblBienvenida)).setText(getString(R.string.saludo, Sesion.getNombre(this)));
        ((TextView) findViewById(R.id.lblRol)).setText(getString(R.string.rol, Sesion.getRol(this)));
        lblVentasHoy = findViewById(R.id.lblVentasHoy);
        lblFacturasHoy = findViewById(R.id.lblFacturasHoy);
        lblExistenciaBaja = findViewById(R.id.lblExistenciaBaja);
        findViewById(R.id.filaIndicadores).setOnClickListener(v -> abrir(ResumenActivity.class));
        findViewById(R.id.btnSalir).setOnClickListener(v -> cerrarSesion());

        deslizar = findViewById(R.id.deslizar);
        deslizar.setColorSchemeColors(getColor(R.color.acento));
        deslizar.setOnRefreshListener(this::cargarIndicadores);

        armarMosaico();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (token() != null) {
            cargarIndicadores(); // al volver de una venta, los numeros se actualizan
        }
    }

    private void armarMosaico() {
        GridLayout mosaico = findViewById(R.id.mosaico);
        LayoutInflater inflador = LayoutInflater.from(this);
        for (Opcion opcion : OPCIONES) {
            View boton = inflador.inflate(R.layout.item_mosaico, mosaico, false);
            ((ImageView) boton.findViewById(R.id.imgIcono)).setImageResource(opcion.icono);
            ((TextView) boton.findViewById(R.id.txtNombre)).setText(opcion.texto);
            boton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(opcion.color)));
            boton.setOnClickListener(v -> abrir(opcion.pantalla));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(boton.getLayoutParams());
            lp.width = 0;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // columnas del mismo ancho
            mosaico.addView(boton, lp);
        }
    }

    private void cargarIndicadores() {
        ApiSupabase.getInstancia().resumen(token(), Formatos.hoy(), new ApiSupabase.Respuesta<Resumen>() {
            @Override
            public void exito(Resumen r) {
                deslizar.setRefreshing(false);
                lblVentasHoy.setText(Formatos.dinero(r.getVentasHoy()));
                lblFacturasHoy.setText(String.valueOf(r.getFacturasHoy()));
                lblExistenciaBaja.setText(String.valueOf(r.getExistenciaBaja().size()));
                lblExistenciaBaja.setTextColor(getColor(r.getExistenciaBaja().isEmpty() ? R.color.exito : R.color.alerta));
            }

            @Override
            public void error(String mensaje, boolean sesionVencida) {
                deslizar.setRefreshing(false);
                if (sesionVencida || !ApiSupabase.SIN_CONEXION.equals(mensaje)) {
                    mostrarError(mensaje, sesionVencida);
                }
            }
        });
    }

    private void abrir(Class<?> pantalla) {
        startActivity(new Intent(this, pantalla));
    }

    private void cerrarSesion() {
        ApiSupabase.getInstancia().logout(token());
        Sesion.cerrar(this);
        CacheCatalogo.borrar(this); // los datos del negocio no quedan en el telefono al salir
        irAlLogin();
    }
}
