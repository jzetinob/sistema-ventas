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
 * Facturar desde el telefono. Se elige el cliente (o se escribe) y los
 * productos con su cantidad; al guardar, la base de datos registra la
 * factura, descuenta la existencia y asigna el numero en una sola
 * transaccion. El precio lo pone la base, no el telefono.
 */
public class NuevaVentaActivity extends ActividadBase {

    private final List<LineaVenta> carrito = new ArrayList<>();
    private AdaptadorFilas<LineaVenta> adaptador;
    private EditText txtNit;
    private EditText txtCliente;
    private TextView lblTotal;
    private TextView lblVacio;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_venta);
        setTitle(R.string.titulo_nueva_venta);
        txtNit = findViewById(R.id.txtNit);
        txtCliente = findViewById(R.id.txtCliente);
        lblTotal = findViewById(R.id.lblTotal);
        lblVacio = findViewById(R.id.lblVacio);
        btnGuardar = findViewById(R.id.btnGuardar);

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
        btnGuardar.setOnClickListener(v -> confirmar());
        actualizarCarrito();
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
                    txtCliente.setError(null);
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
                        return p.getCodigo() + "  ·  " + (p.getExistencia() == 0 ? "Agotado" : "Existencia: " + p.getExistencia());
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

    private void pedirCantidad(Producto p) {
        if (p.getExistencia() <= 0) {
            mostrarError(p.getNombre() + " está agotado.", false);
            return;
        }
        int margen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        EditText txtCantidad = new EditText(this);
        txtCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtCantidad.setHint("Disponible: " + p.getExistencia());
        int indice = indiceEnCarrito(p.getCodigo());
        txtCantidad.setText(String.valueOf(indice >= 0 ? carrito.get(indice).getCantidad() : 1));
        txtCantidad.selectAll();
        FrameLayout marco = new FrameLayout(this);
        marco.setPadding(margen, 0, margen, 0);
        marco.addView(txtCantidad);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle(p.getNombre())
                .setMessage(Formatos.dinero(p.getPrecio()) + " c/u · Disponible: " + p.getExistencia())
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
            if (cantidad <= 0 || cantidad > p.getExistencia()) {
                txtCantidad.setError("Escriba una cantidad entre 1 y " + p.getExistencia());
                return;
            }
            LineaVenta linea = new LineaVenta(p.getNombre(), p.getCodigo(), cantidad, p.getPrecio(), p.getExistencia());
            if (indice >= 0) {
                carrito.set(indice, linea); // si ya estaba, se cambia la cantidad
            } else {
                carrito.add(linea);
            }
            actualizarCarrito();
            dialogo.dismiss();
        }));
        dialogo.show();
    }

    private int indiceEnCarrito(String codigo) {
        for (int i = 0; i < carrito.size(); i++) {
            if (carrito.get(i).getCodigo().equalsIgnoreCase(codigo)) {
                return i;
            }
        }
        return -1;
    }

    private void quitar(int posicion) {
        LineaVenta l = carrito.get(posicion);
        new AlertDialog.Builder(this)
                .setMessage("¿Quitar " + l.getProducto() + " de la venta?")
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    carrito.remove(posicion);
                    actualizarCarrito();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private double total() {
        double total = 0;
        for (LineaVenta l : carrito) {
            total += l.getSubtotal();
        }
        return total;
    }

    private void actualizarCarrito() {
        adaptador.setElementos(new ArrayList<>(carrito));
        visible(lblVacio, carrito.isEmpty());
        lblTotal.setText(getString(R.string.total_con_valor, Formatos.dinero(total())));
        btnGuardar.setEnabled(!carrito.isEmpty());
    }

    private void confirmar() {
        String nit = txtNit.getText().toString().trim();
        String cliente = txtCliente.getText().toString().trim();
        if (!nit.replace("-", "").replace(" ", "").matches("\\d{8,13}")) {
            txtNit.setError("El NIT debe tener entre 8 y 13 dígitos");
            txtNit.requestFocus();
            return;
        }
        if (cliente.isEmpty()) {
            txtCliente.setError("Escriba el nombre del cliente");
            txtCliente.requestFocus();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.guardar_venta)
                .setMessage(cliente + "\n" + carrito.size() + " producto(s) · Total " + Formatos.dinero(total()))
                .setPositiveButton(R.string.guardar, (d, w) -> guardar(nit, cliente))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void guardar(String nit, String cliente) {
        btnGuardar.setEnabled(false);
        ApiSupabase.getInstancia().registrarFactura(token(), nit, cliente, Formatos.hoy(), carrito,
                new ApiSupabase.Respuesta<JSONObject>() {
                    @Override
                    public void exito(JSONObject r) {
                        new AlertDialog.Builder(NuevaVentaActivity.this)
                                .setTitle("Venta guardada")
                                .setMessage("Factura " + r.optString("numero_factura") + "\nTotal "
                                        + Formatos.dinero(r.optDouble("total")))
                                .setPositiveButton(android.R.string.ok, (d, w) -> finish())
                                .setCancelable(false)
                                .show();
                    }

                    @Override
                    public void error(String mensaje, boolean sesionVencida) {
                        btnGuardar.setEnabled(true);
                        mostrarError(mensaje, sesionVencida); // p. ej. otro vendedor agoto un producto
                    }
                });
    }
}
