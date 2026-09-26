package com.josue.ventas.movil.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador generico para listas de dos lineas (titulo y detalle).
 * Cada pantalla indica como convertir su objeto en esas dos lineas.
 */
public class AdaptadorFilas<T> extends BaseAdapter {

    /** Convierte un objeto en el texto de la fila. */
    public interface Formato<T> {
        String titulo(T elemento);

        String detalle(T elemento);

        /** Color del detalle (0 = color normal). */
        default int colorDetalle(T elemento) {
            return 0;
        }
    }

    private final LayoutInflater inflador;
    private final Formato<T> formato;
    private List<T> elementos = new ArrayList<>();

    public AdaptadorFilas(Context contexto, Formato<T> formato) {
        this.inflador = LayoutInflater.from(contexto);
        this.formato = formato;
    }

    public void setElementos(List<T> elementos) {
        this.elementos = elementos;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return elementos.size();
    }

    @Override
    public T getItem(int posicion) {
        return elementos.get(posicion);
    }

    @Override
    public long getItemId(int posicion) {
        return posicion;
    }

    @Override
    public View getView(int posicion, View vista, ViewGroup padre) {
        if (vista == null) {
            vista = inflador.inflate(android.R.layout.simple_list_item_2, padre, false);
        }
        T elemento = getItem(posicion);
        TextView titulo = vista.findViewById(android.R.id.text1);
        TextView detalle = vista.findViewById(android.R.id.text2);
        titulo.setText(formato.titulo(elemento));
        detalle.setText(formato.detalle(elemento));
        int color = formato.colorDetalle(elemento);
        if (color != 0) {
            detalle.setTextColor(color);
        } else {
            detalle.setTextColor(titulo.getCurrentTextColor());
            detalle.setAlpha(0.7f);
        }
        return vista;
    }
}
