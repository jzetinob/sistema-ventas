package com.josue.ventas.movil.vista;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.modelo.Venta;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Ventas (facturas) de un rango de fechas con su total. Por defecto, las de hoy.
 * Al tocar una venta se abre su detalle.
 */
public class VentasActivity extends ActividadBase {

    private final Calendar desde = Calendar.getInstance();
    private final Calendar hasta = Calendar.getInstance();
    private Button btnDesde;
    private Button btnHasta;
    private TextView lblTotal;
    private TextView lblVacio;
    private SwipeRefreshLayout deslizar;
    private AdaptadorFilas<Venta> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas);
        btnDesde = findViewById(R.id.btnDesde);
        btnHasta = findViewById(R.id.btnHasta);
        lblTotal = findViewById(R.id.lblTotal);
        lblVacio = findViewById(R.id.lblVacio);
        deslizar = findViewById(R.id.deslizar);
        deslizar.setColorSchemeColors(getColor(R.color.acento));
        deslizar.setOnRefreshListener(this::consultar);

        adaptador = new AdaptadorFilas<>(this, new AdaptadorFilas.Formato<Venta>() {
            @Override
            public String titulo(Venta v) {
                return v.getNumeroFactura();
            }

            @Override
            public String detalle(Venta v) {
                return Formatos.fechaLarga(v.getFecha()) + "  ·  " + v.getCliente();
            }

            @Override
            public String valor(Venta v) {
                return Formatos.dinero(v.getTotal());
            }
        });
        ListView lista = findViewById(R.id.lista);
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((padre, vista, posicion, id) -> abrirDetalle(adaptador.getItem(posicion)));

        btnDesde.setOnClickListener(v -> elegirFecha(desde));
        btnHasta.setOnClickListener(v -> elegirFecha(hasta));
        mostrarFechas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        deslizar.setRefreshing(true);
        consultar(); // al volver de una venta nueva, aparece en la lista
    }

    private void abrirDetalle(Venta v) {
        Intent intent = new Intent(this, DetalleVentaActivity.class);
        intent.putExtra(DetalleVentaActivity.EXTRA_NUMERO, v.getNumeroFactura());
        intent.putExtra(DetalleVentaActivity.EXTRA_FECHA, v.getFecha());
        intent.putExtra(DetalleVentaActivity.EXTRA_CLIENTE, v.getCliente());
        intent.putExtra(DetalleVentaActivity.EXTRA_TOTAL, v.getTotal());
        startActivity(intent);
    }

    private void elegirFecha(Calendar fecha) {
        new DatePickerDialog(this, (vista, anio, mes, dia) -> {
            fecha.set(anio, mes, dia);
            mostrarFechas();
            deslizar.setRefreshing(true);
            consultar();
        }, fecha.get(Calendar.YEAR), fecha.get(Calendar.MONTH), fecha.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarFechas() {
        btnDesde.setText(getString(R.string.fecha_boton, getString(R.string.desde), Formatos.fechaLarga(Formatos.aTexto(desde))));
        btnHasta.setText(getString(R.string.fecha_boton, getString(R.string.hasta), Formatos.fechaLarga(Formatos.aTexto(hasta))));
    }

    private void consultar() {
        if (Formatos.aTexto(desde).compareTo(Formatos.aTexto(hasta)) > 0) {
            deslizar.setRefreshing(false);
            mostrarError("La fecha inicial no puede ser posterior a la final.", false);
            return;
        }
        ApiSupabase.getInstancia().ventas(token(), Formatos.aTexto(desde), Formatos.aTexto(hasta),
                new ApiSupabase.Respuesta<List<Venta>>() {
                    @Override
                    public void exito(List<Venta> ventas) {
                        deslizar.setRefreshing(false);
                        visible(lblVacio, ventas.isEmpty());
                        adaptador.setElementos(ventas);
                        double total = 0;
                        for (Venta v : ventas) {
                            total += v.getTotal();
                        }
                        lblTotal.setText(String.format(Locale.US, "%d facturas  ·  Total %s", ventas.size(), Formatos.dinero(total)));
                    }

                    @Override
                    public void error(String mensaje, boolean sesionVencida) {
                        deslizar.setRefreshing(false);
                        mostrarError(mensaje, sesionVencida);
                    }
                });
    }
}
