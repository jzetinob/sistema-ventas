package com.josue.ventas.movil.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.josue.ventas.movil.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Grafico de barras dibujado a mano con Canvas (sin librerias).
 * Hereda de View y sobrescribe onDraw(): Android lo llama cada vez que hay
 * que pintar la vista. Se usa para las ventas de los ultimos 7 dias.
 */
public class GraficoBarras extends View {

    private final List<String> etiquetas = new ArrayList<>();
    private final List<Double> valores = new ArrayList<>();
    private final Paint pincelBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelBarraDestacada = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelRejilla = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private int destacada = -1;

    public GraficoBarras(Context contexto, AttributeSet atributos) {
        super(contexto, atributos);
        pincelBarra.setColor(contexto.getColor(R.color.serie_1));
        pincelBarraDestacada.setColor(contexto.getColor(R.color.serie_2));
        pincelRejilla.setColor(contexto.getColor(R.color.rejilla));
        pincelRejilla.setStrokeWidth(dp(1));
        pincelTexto.setColor(contexto.getColor(R.color.texto_suave));
        pincelTexto.setTextSize(sp(11));
        pincelTexto.setTextAlign(Paint.Align.CENTER);
        pincelValor.setColor(contexto.getColor(R.color.texto));
        pincelValor.setTextSize(sp(11));
        pincelValor.setFakeBoldText(true);
        pincelValor.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * @param destacada indice de la barra que se pinta con otro color (por ejemplo, hoy); -1 = ninguna
     */
    public void setDatos(List<String> etiquetas, List<Double> valores, int destacada) {
        this.etiquetas.clear();
        this.etiquetas.addAll(etiquetas);
        this.valores.clear();
        this.valores.addAll(valores);
        this.destacada = destacada;
        setContentDescription(descripcion());
        invalidate(); // pide a Android que vuelva a llamar onDraw()
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = valores.size();
        if (n == 0) {
            return;
        }
        float alto = getHeight();
        float ancho = getWidth();
        float margenArriba = dp(22);   // espacio para el valor encima de la barra
        float margenAbajo = dp(22);    // espacio para las etiquetas de los dias
        float areaAlto = alto - margenArriba - margenAbajo;
        double maximo = 0;
        for (double v : valores) {
            maximo = Math.max(maximo, v);
        }

        // lineas guia horizontales (0 %, 50 % y 100 % del maximo)
        for (int i = 0; i <= 2; i++) {
            float y = margenArriba + areaAlto * i / 2f;
            canvas.drawLine(0, y, ancho, y, pincelRejilla);
        }
        if (maximo <= 0) {
            canvas.drawText("Sin ventas en estos días", ancho / 2, margenArriba + areaAlto / 2 - dp(6), pincelTexto);
        }

        float espacio = ancho / n;
        float anchoBarra = espacio * 0.56f;
        for (int i = 0; i < n; i++) {
            float centro = espacio * i + espacio / 2;
            double v = valores.get(i);
            float altoBarra = maximo > 0 ? (float) (v / maximo) * areaAlto : 0;
            float base = margenArriba + areaAlto;
            if (altoBarra > 0) {
                rect.set(centro - anchoBarra / 2, base - altoBarra, centro + anchoBarra / 2, base);
                canvas.drawRoundRect(rect, dp(4), dp(4), i == destacada ? pincelBarraDestacada : pincelBarra);
                canvas.drawText(compacto(v), centro, base - altoBarra - dp(6), pincelValor);
            }
            canvas.drawText(etiquetas.get(i), centro, alto - dp(6), pincelTexto);
        }
    }

    /** 1250 -> "1.3k"; asi los valores caben encima de barras angostas. */
    static String compacto(double v) {
        if (v >= 1000) {
            return String.format(Locale.US, "%.1fk", v / 1000);
        }
        return String.format(Locale.US, "%.0f", v);
    }

    private String descripcion() {
        StringBuilder sb = new StringBuilder("Ventas por día: ");
        for (int i = 0; i < valores.size(); i++) {
            sb.append(etiquetas.get(i)).append(" Q ").append(String.format(Locale.US, "%.2f", valores.get(i))).append(". ");
        }
        return sb.toString();
    }

    private float dp(float valor) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics());
    }

    private float sp(float valor) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, valor, getResources().getDisplayMetrics());
    }
}
