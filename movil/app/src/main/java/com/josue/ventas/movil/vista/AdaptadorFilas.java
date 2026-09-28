package com.josue.ventas.movil.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador generico (clase con parametro de tipo T) para las listas con
 * forma de tarjeta. Cada pantalla indica como convertir su objeto en el
 * texto de la tarjeta implementando la interfaz Formato.
 */
public class AdaptadorFilas<T> extends BaseAdapter {

    /** Convierte un objeto en el texto de la fila. */
    public interface Formato<T> {
        String titulo(T elemento);

        String detalle(T elemento);

        /** Valor destacado a la derecha (precio, total...); null = no se muestra. */
        default String valor(T elemento) {
            return null;
        }

        /** Color del detalle (0 = color normal). */
        default int colorDetalle(T elemento) {
            return 0;
        }
    }

    /** Guarda las vistas de la fila para no buscarlas cada vez (patron ViewHolder). */
    private static final class Fila {
        final TextView titulo;
        final TextView detalle;
        final TextView valor;
        final int colorDetalleNormal;

        Fila(View vista) {
            titulo = vista.findViewById(R.id.txtTitulo);
            detalle = vista.findViewById(R.id.txtDetalle);
            valor = vista.findViewById(R.id.txtValor);
            colorDetalleNormal = detalle.getCurrentTextColor();
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

    public List<T> getElementos() {
        return elementos;
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
        Fila fila;
        if (vista == null) {
            vista = inflador.inflate(R.layout.item_fila, padre, false);
            fila = new Fila(vista);
            vista.setTag(fila);
        } else {
            fila = (Fila) vista.getTag();
        }
        T elemento = getItem(posicion);
        fila.titulo.setText(formato.titulo(elemento));
        fila.detalle.setText(formato.detalle(elemento));
        int color = formato.colorDetalle(elemento);
        fila.detalle.setTextColor(color != 0 ? color : fila.colorDetalleNormal);
        String valor = formato.valor(elemento);
        fila.valor.setVisibility(valor == null ? View.GONE : View.VISIBLE);
        fila.valor.setText(valor);
        return vista;
    }
}
