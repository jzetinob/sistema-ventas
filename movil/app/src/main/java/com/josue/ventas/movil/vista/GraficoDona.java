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

/**
 * Grafico de dona (anillo) dibujado con Canvas: cada porcion es un arco
 * proporcional a su valor. En el centro muestra el total. Se usa para los
 * productos mas vendidos del mes; la leyenda la arma la pantalla con colorDe().
 */
public class GraficoDona extends View {

    private static final int[] COLORES = {R.color.serie_1, R.color.serie_2, R.color.serie_3, R.color.serie_4, R.color.serie_5};

    private final List<Double> valores = new ArrayList<>();
    private final Paint pincelArco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelVacio = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTotal = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelEtiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalo = new RectF();
    private String textoCentro = "";
    private String etiquetaCentro = "";

    public GraficoDona(Context contexto, AttributeSet atributos) {
        super(contexto, atributos);
        pincelArco.setStyle(Paint.Style.STROKE);
        pincelVacio.setStyle(Paint.Style.STROKE);
        pincelVacio.setColor(contexto.getColor(R.color.rejilla));
        pincelTotal.setColor(contexto.getColor(R.color.texto));
        pincelTotal.setTextAlign(Paint.Align.CENTER);
        pincelTotal.setFakeBoldText(true);
        pincelTotal.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));
        pincelEtiqueta.setColor(contexto.getColor(R.color.texto_suave));
        pincelEtiqueta.setTextAlign(Paint.Align.CENTER);
        pincelEtiqueta.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11, getResources().getDisplayMetrics()));
    }

    /** Color de la porcion i (el mismo que usa la leyenda). */
    public static int colorDe(Context contexto, int i) {
        return contexto.getColor(COLORES[i % COLORES.length]);
    }

    public void setDatos(List<Double> valores, String textoCentro, String etiquetaCentro) {
        this.valores.clear();
        this.valores.addAll(valores);
        this.textoCentro = textoCentro;
        this.etiquetaCentro = etiquetaCentro;
        setContentDescription(etiquetaCentro + ": " + textoCentro);
        invalidate();
    }

    @Override
    protected void onMeasure(int anchoMedida, int altoMedida) {
        // cuadrada: el alto igual al ancho disponible, sin pasar de 180dp
        int maximo = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
        int lado = Math.min(MeasureSpec.getSize(anchoMedida), maximo);
        setMeasuredDimension(lado, lado);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float lado = Math.min(getWidth(), getHeight());
        float grosor = lado * 0.16f;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radio = lado / 2f - grosor / 2f;
        ovalo.set(cx - radio, cy - radio, cx + radio, cy + radio);
        pincelArco.setStrokeWidth(grosor);
        pincelVacio.setStrokeWidth(grosor);

        double total = 0;
        for (double v : valores) {
            total += v;
        }
        if (total <= 0) {
            canvas.drawArc(ovalo, 0, 360, false, pincelVacio);
        } else {
            float inicio = -90; // empieza arriba, en sentido del reloj
            float separacion = valores.size() > 1 ? 1.5f : 0;
            for (int i = 0; i < valores.size(); i++) {
                float barrido = (float) (valores.get(i) / total * 360);
                pincelArco.setColor(colorDe(getContext(), i));
                canvas.drawArc(ovalo, inicio, Math.max(barrido - separacion, 0.5f), false, pincelArco);
                inicio += barrido;
            }
        }
        canvas.drawText(textoCentro, cx, cy + pincelTotal.getTextSize() * 0.2f, pincelTotal);
        canvas.drawText(etiquetaCentro, cx, cy + pincelTotal.getTextSize() * 0.2f + pincelEtiqueta.getTextSize() * 1.4f, pincelEtiqueta);
    }
}
