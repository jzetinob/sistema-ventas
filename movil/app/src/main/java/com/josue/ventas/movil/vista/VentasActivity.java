package com.josue.ventas.movil.vista;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.modelo.Venta;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Ventas (facturas) de un rango de fechas con su total. Por defecto, las de hoy. */
public class VentasActivity extends ActividadBase {

    private final Calendar desde = Calendar.getInstance();
    private final Calendar hasta = Calendar.getInstance();
    private Button btnDesde;
    private Button btnHasta;
    private TextView lblTotal;
    private TextView lblVacio;
    private ProgressBar progreso;
    private AdaptadorFilas<Venta> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas);
        btnDesde = findViewById(R.id.btnDesde);
        btnHasta = findViewById(R.id.btnHasta);
        lblTotal = findViewById(R.id.lblTotal);
        lblVacio = findViewById(R.id.lblVacio);
        progreso = findViewById(R.id.progreso);

        adaptador = new AdaptadorFilas<>(this, new AdaptadorFilas.Formato<Venta>() {
            @Override
            public String titulo(Venta v) {
                return String.format(Locale.US, "%s  ·  Q %.2f", v.getNumeroFactura(), v.getTotal());
            }

            @Override
            public String detalle(Venta v) {
                return v.getFecha() + "  ·  " + v.getCliente();
            }
        });
        ((ListView) findViewById(R.id.lista)).setAdapter(adaptador);

        btnDesde.setOnClickListener(v -> elegirFecha(desde));
        btnHasta.setOnClickListener(v -> elegirFecha(hasta));
        mostrarFechas();
        consultar();
    }

    private void elegirFecha(Calendar fecha) {
        new DatePickerDialog(this, (vista, anio, mes, dia) -> {
            fecha.set(anio, mes, dia);
            mostrarFechas();
            consultar();
        }, fecha.get(Calendar.YEAR), fecha.get(Calendar.MONTH), fecha.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarFechas() {
        btnDesde.setText(getString(R.string.fecha_boton, getString(R.string.desde), texto(desde)));
        btnHasta.setText(getString(R.string.fecha_boton, getString(R.string.hasta), texto(hasta)));
    }

    private static String texto(Calendar c) {
        return String.format(Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH));
    }

    private void consultar() {
        if (desde.after(hasta)) {
            mostrarError("La fecha inicial no puede ser posterior a la final.", false);
            return;
        }
        visible(progreso, true);
        ApiSupabase.getInstancia().ventas(token(), texto(desde), texto(hasta), new ApiSupabase.Respuesta<List<Venta>>() {
            @Override
            public void exito(List<Venta> ventas) {
                visible(progreso, false);
                visible(lblVacio, ventas.isEmpty());
                adaptador.setElementos(ventas);
                double total = 0;
                for (Venta v : ventas) {
                    total += v.getTotal();
                }
                lblTotal.setText(String.format(Locale.US, "%d facturas  ·  Total Q %.2f", ventas.size(), total));
            }

            @Override
            public void error(String mensaje, boolean sesionVencida) {
                visible(progreso, false);
                mostrarError(mensaje, sesionVencida);
            }
        });
    }
}
