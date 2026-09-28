package com.josue.ventas.movil.vista;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.modelo.Producto;
import com.josue.ventas.movil.modelo.Resumen;
import java.util.ArrayList;
import java.util.List;

/**
 * Tablero con indicadores y graficos: ventas de los ultimos 7 dias (barras),
 * productos mas vendidos del mes (dona) y productos por agotarse.
 * Todo llega en una sola llamada a la base (app_resumen).
 */
public class ResumenActivity extends ActividadBase {

    private SwipeRefreshLayout deslizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);
        setTitle(R.string.titulo_resumen);
        deslizar = findViewById(R.id.deslizar);
        deslizar.setColorSchemeColors(getColor(R.color.acento));
        deslizar.setOnRefreshListener(this::cargar);
        deslizar.setRefreshing(true);
        cargar();
    }

    private void cargar() {
        ApiSupabase.getInstancia().resumen(token(), Formatos.hoy(), new ApiSupabase.Respuesta<Resumen>() {
            @Override
            public void exito(Resumen r) {
                deslizar.setRefreshing(false);
                mostrar(r);
            }

            @Override
            public void error(String mensaje, boolean sesionVencida) {
                deslizar.setRefreshing(false);
                mostrarError(mensaje, sesionVencida);
            }
        });
    }

    private void mostrar(Resumen r) {
        ((TextView) findViewById(R.id.lblVentasHoy)).setText(Formatos.dinero(r.getVentasHoy()));
        ((TextView) findViewById(R.id.lblFacturasHoy)).setText(String.valueOf(r.getFacturasHoy()));
        ((TextView) findViewById(R.id.lblVentasMes)).setText(Formatos.dinero(r.getVentasMes()));

        // barras: un dia por barra; la ultima (hoy) se destaca con otro color
        List<String> dias = new ArrayList<>();
        List<Double> totales = new ArrayList<>();
        for (Resumen.VentaDia d : r.getVentasSemana()) {
            dias.add(Formatos.diaCorto(d.getFecha()));
            totales.add(d.getTotal());
        }
        ((GraficoBarras) findViewById(R.id.graficoBarras)).setDatos(dias, totales, dias.size() - 1);

        // dona: unidades vendidas por producto, con su leyenda
        List<Double> cantidades = new ArrayList<>();
        int unidades = 0;
        for (Resumen.ProductoVendido p : r.getMasVendidos()) {
            cantidades.add((double) p.getCantidad());
            unidades += p.getCantidad();
        }
        ((GraficoDona) findViewById(R.id.graficoDona)).setDatos(cantidades, String.valueOf(unidades), "unidades");
        LinearLayout leyenda = findViewById(R.id.leyenda);
        leyenda.removeAllViews();
        if (r.getMasVendidos().isEmpty()) {
            agregarTexto(leyenda, getString(R.string.sin_ventas_mes));
        }
        for (int i = 0; i < r.getMasVendidos().size(); i++) {
            Resumen.ProductoVendido p = r.getMasVendidos().get(i);
            agregarRenglon(leyenda, GraficoDona.colorDe(this, i), p.getProducto(),
                    p.getCantidad() + " u. · " + Formatos.dinero(p.getTotal()));
        }

        // productos por agotarse
        LinearLayout agotandose = findViewById(R.id.listaAgotandose);
        agotandose.removeAllViews();
        if (r.getExistenciaBaja().isEmpty()) {
            agregarTexto(agotandose, getString(R.string.sin_agotandose));
        }
        for (Producto p : r.getExistenciaBaja()) {
            agregarRenglon(agotandose, getColor(p.getExistencia() == 0 ? R.color.alerta : R.color.serie_2),
                    p.getCodigo() + " - " + p.getNombre(),
                    p.getExistencia() == 0 ? "Agotado" : "Quedan " + p.getExistencia());
        }
    }

    private void agregarRenglon(LinearLayout contenedor, int color, String texto, String valor) {
        View renglon = LayoutInflater.from(this).inflate(R.layout.item_leyenda, contenedor, false);
        renglon.findViewById(R.id.marca).setBackgroundTintList(ColorStateList.valueOf(color));
        ((TextView) renglon.findViewById(R.id.txtTexto)).setText(texto);
        ((TextView) renglon.findViewById(R.id.txtValor)).setText(valor);
        contenedor.addView(renglon);
    }

    private void agregarTexto(LinearLayout contenedor, String texto) {
        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextColor(getColor(R.color.texto_suave));
        t.setPadding(0, 8, 0, 8);
        contenedor.addView(t);
    }
}
