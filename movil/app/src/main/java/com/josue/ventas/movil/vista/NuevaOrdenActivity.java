package com.josue.ventas.movil.vista;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.josue.ventas.movil.R;
import com.josue.ventas.movil.datos.ApiSupabase;
import com.josue.ventas.movil.modelo.Cliente;
import com.josue.ventas.movil.modelo.LineaVenta;
import com.josue.ventas.movil.modelo.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Crear una Orden de Venta (tarea final: Android + API REST). Se elige el
 * cliente del catalogo y los productos con su cantidad; al enviar, la app
 * hace un POST a la API REST (app_registrar_orden_venta), que valida los
 * datos, guarda la orden en la base y responde con el numero de orden o
 * con el mensaje de error. La orden no descuenta existencia: queda
 * PENDIENTE hasta que el escritorio la confirme.
 */
public class NuevaOrdenActivity extends ActividadBase {

    private static final int CANTIDAD_MAXIMA = 100000;

    private final List<LineaVenta> productos = new ArrayList<>();
    private AdaptadorFilas<LineaVenta> adaptador;
    private EditText txtNit;
    private EditText txtCliente;
    private EditText txtObservaciones;
    private TextView lblTotal;
    private TextView lblVacio;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_orden);
        setTitle(R.string.titulo_orden_venta);
        txtNit = findViewById(R.id.txtNit);
        txtCliente = findViewById(R.id.txtCliente);
        txtObservaciones = findViewById(R.id.txtObservaciones);
        lblTotal = findViewById(R.id.lblTotal);
        lblVacio = findViewById(R.id.lblVacio);
        btnEnviar = findViewById(R.id.btnEnviar);
        ((TextView) findViewById(R.id.lblEndpoint)).setText(getString(R.string.endpoint_orden, ApiSupabase.urlOrdenVenta()));

        adaptador = new AdaptadorFilas<>(this, new AdaptadorFilas.Formato<LineaVenta>() {
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
        ListView lista = findViewById(R.id.lista);
        lista.setAdapter(adaptador);
        lista.setOnItemLongClickListener((padre, vista, posicion, id) -> {
            quitar(posicion);
            return true;
        });

        findViewById(R.id.btnBuscarCliente).setOnClickListener(v -> elegirCliente());
        findViewById(R.id.btnAgregarProducto).setOnClickListener(v -> elegirProducto());
        btnEnviar.setOnClickListener(v -> confirmar());
        actualizarLista();
    }

    private void elegirCliente() {
        DialogoBusqueda.mostrar(this, getString(R.string.titulo_clientes), getString(R.string.buscar_cliente),
                new AdaptadorFilas.Formato<Cliente>() {
                    @Override
                    public String titulo(Cliente c) {
                        return c.getNombre();
                    }

                    @Override
                    public String detalle(Cliente c) {
                        return "NIT " + c.getNit();
                    }
                },
                (texto, respuesta) -> ApiSupabase.getInstancia().clientes(token(), texto, respuesta),
                c -> {
                    txtNit.setText(c.getNit());
                    txtCliente.setText(c.getNombre());
                    txtNit.setError(null);
                });
    }

    private void elegirProducto() {
        DialogoBusqueda.mostrar(this, getString(R.string.titulo_productos), getString(R.string.buscar_producto),
                new AdaptadorFilas.Formato<Producto>() {
                    @Override
                    public String titulo(Producto p) {
                        return p.getNombre();
                    }

                    @Override
                    public String detalle(Producto p) {
                        return p.getCodigo() + "  ·  Existencia: " + p.getExistencia();
                    }

                    @Override
                    public String valor(Producto p) {
                        return Formatos.dinero(p.getPrecio());
                    }
                },
                (texto, respuesta) -> ApiSupabase.getInstancia().productos(token(), texto,
                        new ApiSupabase.Respuesta<JSONArray>() {
                            @Override
                            public void exito(JSONArray json) {
                                try {
                                    respuesta.exito(ApiSupabase.productosDesdeJson(json));
                                } catch (JSONException ex) {
                                    respuesta.error("Respuesta inesperada del servidor.", false);
                                }
                            }

                            @Override
                            public void error(String mensaje, boolean sesionVencida) {
                                respuesta.error(mensaje, sesionVencida);
                            }
                        }),
                this::pedirCantidad);
    }

    /** En una orden se puede pedir mas de lo que hay en existencia: no se descuenta todavia. */
    private void pedirCantidad(Producto p) {
        int margen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        EditText txtCantidad = new EditText(this);
        txtCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);
        int indice = indiceEnLista(p.getCodigo());
        txtCantidad.setText(String.valueOf(indice >= 0 ? productos.get(indice).getCantidad() : 1));
        txtCantidad.selectAll();
        FrameLayout marco = new FrameLayout(this);
        marco.setPadding(margen, 0, margen, 0);
        marco.addView(txtCantidad);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle(p.getNombre())
                .setMessage(Formatos.dinero(p.getPrecio()) + " c/u · Existencia: " + p.getExistencia())
                .setView(marco)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int cantidad;
            try {
                cantidad = Integer.parseInt(txtCantidad.getText().toString().trim());
            } catch (NumberFormatException ex) {
                cantidad = 0;
            }
            if (cantidad <= 0 || cantidad > CANTIDAD_MAXIMA) {
                txtCantidad.setError("Escriba una cantidad entre 1 y " + CANTIDAD_MAXIMA);
                return;
            }
            LineaVenta linea = new LineaVenta(p.getNombre(), p.getCodigo(), cantidad, p.getPrecio(), p.getExistencia());
            if (indice >= 0) {
                productos.set(indice, linea); // si ya estaba, se cambia la cantidad
            } else {
                productos.add(linea);
            }
            actualizarLista();
            dialogo.dismiss();
        }));
        dialogo.show();
    }

    private int indiceEnLista(String codigo) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo().equalsIgnoreCase(codigo)) {
                return i;
            }
        }
        return -1;
    }

    private void quitar(int posicion) {
        LineaVenta l = productos.get(posicion);
        new AlertDialog.Builder(this)
                .setMessage("¿Quitar " + l.getProducto() + " de la orden?")
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    productos.remove(posicion);
                    actualizarLista();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private double total() {
        double total = 0;
        for (LineaVenta l : productos) {
            total += l.getSubtotal();
        }
        return total;
    }

    private void actualizarLista() {
        adaptador.setElementos(new ArrayList<>(productos));
        visible(lblVacio, productos.isEmpty());
        lblTotal.setText(getString(R.string.total_con_valor, Formatos.dinero(total())));
        btnEnviar.setEnabled(!productos.isEmpty());
    }

    /** Validacion basica en el telefono; la API vuelve a validar todo antes de guardar. */
    private void confirmar() {
        String nit = txtNit.getText().toString().trim();
        if (nit.isEmpty()) {
            txtNit.setError("Elija el cliente del catálogo o escriba su NIT");
            txtNit.requestFocus();
            return;
        }
        String cliente = txtCliente.getText().toString().trim();
        new AlertDialog.Builder(this)
                .setTitle(R.string.enviar_orden)
                .setMessage((cliente.isEmpty() ? "NIT " + nit : cliente) + "\n" + productos.size()
                        + " producto(s) · Total " + Formatos.dinero(total()))
                .setPositiveButton(R.string.enviar_orden, (d, w) -> enviar(nit))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void enviar(String nit) {
        btnEnviar.setEnabled(false);
        ApiSupabase.getInstancia().registrarOrdenVenta(token(), nit, txtObservaciones.getText().toString().trim(),
                productos, new ApiSupabase.Respuesta<JSONObject>() {
                    @Override
                    public void exito(JSONObject r) {
                        new AlertDialog.Builder(NuevaOrdenActivity.this)
                                .setTitle("Orden registrada")
                                .setMessage("Orden " + r.optString("numero_orden")
                                        + "\nCliente: " + r.optString("cliente")
                                        + "\nTotal " + Formatos.dinero(r.optDouble("total"))
                                        + "\nEstado: " + r.optString("estado"))
                                .setPositiveButton(android.R.string.ok, (d, w) -> finish())
                                .setCancelable(false)
                                .show();
                    }

                    @Override
                    public void error(String mensaje, boolean sesionVencida) {
                        btnEnviar.setEnabled(true);
                        mostrarError(mensaje, sesionVencida); // mensaje que devolvio la API
                    }
                });
    }
}
