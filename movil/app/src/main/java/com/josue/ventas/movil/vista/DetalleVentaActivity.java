package com.josue.ventas.movil.vista;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.modelo.LineaVenta;
import java.util.List;
import java.util.Locale;

/** Productos de una factura: se abre al tocar una venta en "Ventas por fecha". */
public class DetalleVentaActivity extends ActividadBase {

    static final String EXTRA_NUMERO = "numero";
    static final String EXTRA_FECHA = "fecha";
    static final String EXTRA_CLIENTE = "cliente";
    static final String EXTRA_TOTAL = "total";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_venta);
        setTitle(R.string.titulo_detalle_venta);

        String numero = getIntent().getStringExtra(EXTRA_NUMERO);
        ((TextView) findViewById(R.id.lblNumero)).setText(numero);
        ((TextView) findViewById(R.id.lblCliente)).setText(getIntent().getStringExtra(EXTRA_CLIENTE));
        ((TextView) findViewById(R.id.lblFecha)).setText(Formatos.fechaLarga(getIntent().getStringExtra(EXTRA_FECHA)));
        ((TextView) findViewById(R.id.lblTotal)).setText(getString(R.string.total_con_valor, Formatos.dinero(getIntent().getDoubleExtra(EXTRA_TOTAL, 0))));

        AdaptadorFilas<LineaVenta> adaptador = new AdaptadorFilas<>(this, new AdaptadorFilas.Formato<LineaVenta>() {
            @Override
            public String titulo(LineaVenta l) {
                return l.getProducto();
            }

            @Override
            public String detalle(LineaVenta l) {
                return String.format(Locale.US, "%d × %s", l.getCantidad(), Formatos.dinero(l.getPrecio()));
            }

            @Override
            public String valor(LineaVenta l) {
                return Formatos.dinero(l.getSubtotal());
            }
        });
        ((ListView) findViewById(R.id.lista)).setAdapter(adaptador);

        ProgressBar progreso = findViewById(R.id.progreso);
        visible(progreso, true);
        ApiSupabase.getInstancia().detalleVenta(token(), numero, new ApiSupabase.Respuesta<List<LineaVenta>>() {
            @Override
            public void exito(List<LineaVenta> lineas) {
                visible(progreso, false);
                adaptador.setElementos(lineas);
            }

            @Override
            public void error(String mensaje, boolean sesionVencida) {
                visible(progreso, false);
                mostrarError(mensaje, sesionVencida);
            }
        });
    }
}
