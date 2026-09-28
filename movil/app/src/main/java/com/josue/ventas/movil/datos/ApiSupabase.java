package com.josue.ventas.movil.datos;

import android.os.Handler;
import android.os.Looper;
import com.josue.ventas.movil.modelo.Cliente;
import com.josue.ventas.movil.modelo.LineaVenta;
import com.josue.ventas.movil.modelo.Producto;
import com.josue.ventas.movil.modelo.Resumen;
import com.josue.ventas.movil.modelo.Venta;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Acceso a la base de datos en la nube (Supabase) mediante su API REST.
 * Patron singleton: una sola instancia para toda la app.
 *
 * Cada operacion llama una funcion app_* de PostgreSQL por
 * POST /rest/v1/rpc/{funcion}. Las llamadas de red se hacen en un hilo
 * aparte (Android no permite red en el hilo de la pantalla) y el resultado
 * se entrega de vuelta en el hilo principal.
 */
public final class ApiSupabase {

    /** Resultado de una operacion: exito con el dato, o error con un mensaje para el usuario. */
    public interface Respuesta<T> {
        void exito(T resultado);

        void error(String mensaje, boolean sesionVencida);
    }

    private interface Operacion<T> {
        T ejecutar() throws ApiException, JSONException;
    }

    /** Error devuelto por la API (codigo HTTP y mensaje ya legible). */
    static final class ApiException extends Exception {
        final int codigo;

        ApiException(int codigo, String mensaje) {
            super(mensaje);
            this.codigo = codigo;
        }
    }

    /** Mensaje de error cuando no hay internet (las pantallas lo comparan para usar datos guardados). */
    public static final String SIN_CONEXION = "No hay conexión con el servidor. Revise su internet e intente de nuevo.";

    private static final ApiSupabase instancia = new ApiSupabase();
    private static final int TIEMPO_ESPERA_MS = 15000;

    private final ExecutorService hilo = Executors.newSingleThreadExecutor();
    private final Handler principal = new Handler(Looper.getMainLooper());

    private ApiSupabase() {
    }

    public static ApiSupabase getInstancia() {
        return instancia;
    }

    // ------------------------------------------------------------------ operaciones

    /** Devuelve {token, nombre, rol}. */
    public void login(String usuario, String clave, Respuesta<JSONObject> respuesta) {
        ejecutar(() -> {
            JSONObject p = new JSONObject().put("p_usuario", usuario).put("p_clave", clave);
            return new JSONObject(rpc("app_login", p));
        }, respuesta);
    }

    public void logout(String token) {
        ejecutar(() -> {
            rpc("app_logout", new JSONObject().put("p_token", token));
            return null;
        }, null);
    }

    /** Devuelve el JSON tal cual llego (para guardarlo en el telefono) junto con la lista ya convertida. */
    public void productos(String token, String buscar, Respuesta<JSONArray> respuesta) {
        ejecutar(() -> new JSONArray(rpc("app_productos",
                new JSONObject().put("p_token", token).put("p_buscar", buscar))), respuesta);
    }

    /** Convierte el JSON de app_productos en objetos (tambien se usa con la copia guardada sin conexion). */
    public static List<Producto> productosDesdeJson(JSONArray filas) throws JSONException {
        List<Producto> lista = new ArrayList<>();
        for (int i = 0; i < filas.length(); i++) {
            JSONObject f = filas.getJSONObject(i);
            lista.add(new Producto(f.getString("codigo"), f.getString("nombre"),
                    f.isNull("categoria") ? "" : f.getString("categoria"),
                    f.getDouble("precio"), f.getInt("existencia")));
        }
        return lista;
    }

    /** Fecha de hoy con formato aaaa-mm-dd. */
    public void resumen(String token, String hoy, Respuesta<Resumen> respuesta) {
        ejecutar(() -> {
            JSONObject r = new JSONObject(rpc("app_resumen",
                    new JSONObject().put("p_token", token).put("p_hoy", hoy)));
            List<Resumen.VentaDia> semana = new ArrayList<>();
            JSONArray dias = r.optJSONArray("ventas_7_dias");
            for (int i = 0; dias != null && i < dias.length(); i++) {
                JSONObject d = dias.getJSONObject(i);
                semana.add(new Resumen.VentaDia(d.getString("fecha"), d.getDouble("total")));
            }
            List<Resumen.ProductoVendido> top = new ArrayList<>();
            JSONArray vendidos = r.optJSONArray("top_productos");
            for (int i = 0; vendidos != null && i < vendidos.length(); i++) {
                JSONObject v = vendidos.getJSONObject(i);
                top.add(new Resumen.ProductoVendido(v.getString("producto"), v.getInt("cantidad"), v.getDouble("total")));
            }
            List<Producto> baja = new ArrayList<>();
            JSONArray agotandose = r.optJSONArray("existencia_baja");
            for (int i = 0; agotandose != null && i < agotandose.length(); i++) {
                JSONObject p = agotandose.getJSONObject(i);
                baja.add(new Producto(p.getString("codigo"), p.getString("nombre"), "", 0, p.getInt("existencia")));
            }
            return new Resumen(r.getDouble("ventas_hoy"), r.getInt("facturas_hoy"), r.getDouble("ventas_mes"),
                    semana, top, baja);
        }, respuesta);
    }

    public void detalleVenta(String token, String numeroFactura, Respuesta<List<LineaVenta>> respuesta) {
        ejecutar(() -> {
            JSONArray filas = new JSONArray(rpc("app_detalle_venta",
                    new JSONObject().put("p_token", token).put("p_numero_factura", numeroFactura)));
            List<LineaVenta> lineas = new ArrayList<>();
            for (int i = 0; i < filas.length(); i++) {
                JSONObject f = filas.getJSONObject(i);
                lineas.add(new LineaVenta(f.getString("producto"), f.getInt("cantidad"), f.getDouble("precio")));
            }
            return lineas;
        }, respuesta);
    }

    /**
     * Registra la factura. Solo se envian codigo y cantidad de cada linea:
     * el precio y la existencia los toma y valida la base de datos.
     * Devuelve {numero_factura, total}.
     */
    public void registrarFactura(String token, String nit, String cliente, String fecha, List<LineaVenta> lineas,
            Respuesta<JSONObject> respuesta) {
        ejecutar(() -> {
            JSONArray detalles = new JSONArray();
            for (LineaVenta l : lineas) {
                detalles.put(new JSONObject().put("codigo", l.getCodigo()).put("cantidad", l.getCantidad()));
            }
            JSONObject p = new JSONObject().put("p_token", token).put("p_nit", nit).put("p_cliente", cliente)
                    .put("p_fecha", fecha).put("p_detalles", detalles);
            return new JSONObject(rpc("app_registrar_factura", p));
        }, respuesta);
    }

    public void clientes(String token, String buscar, Respuesta<List<Cliente>> respuesta) {
        ejecutar(() -> {
            JSONArray filas = new JSONArray(rpc("app_clientes",
                    new JSONObject().put("p_token", token).put("p_buscar", buscar)));
            List<Cliente> lista = new ArrayList<>();
            for (int i = 0; i < filas.length(); i++) {
                JSONObject f = filas.getJSONObject(i);
                lista.add(new Cliente(f.getString("nit"), f.getString("nombre"),
                        f.optString("direccion", ""), f.optString("telefono", "")));
            }
            return lista;
        }, respuesta);
    }

    public void registrarCliente(String token, String nit, String nombre, String direccion, String telefono,
            Respuesta<Void> respuesta) {
        ejecutar(() -> {
            JSONObject p = new JSONObject().put("p_token", token).put("p_nit", nit).put("p_nombre", nombre)
                    .put("p_direccion", direccion).put("p_telefono", telefono);
            rpc("app_registrar_cliente", p);
            return null;
        }, respuesta);
    }

    /** Fechas con formato aaaa-mm-dd. */
    public void ventas(String token, String desde, String hasta, Respuesta<List<Venta>> respuesta) {
        ejecutar(() -> {
            JSONArray filas = new JSONArray(rpc("app_ventas",
                    new JSONObject().put("p_token", token).put("p_desde", desde).put("p_hasta", hasta)));
            List<Venta> lista = new ArrayList<>();
            for (int i = 0; i < filas.length(); i++) {
                JSONObject f = filas.getJSONObject(i);
                lista.add(new Venta(f.getString("numero_factura"), f.getString("fecha"),
                        f.getString("cliente"), f.getDouble("total")));
            }
            return lista;
        }, respuesta);
    }

    // ------------------------------------------------------------------ infraestructura

    private <T> void ejecutar(Operacion<T> operacion, Respuesta<T> respuesta) {
        hilo.execute(() -> {
            try {
                T resultado = operacion.ejecutar();
                if (respuesta != null) {
                    principal.post(() -> respuesta.exito(resultado));
                }
            } catch (ApiException ex) {
                // 401/403 con token: la sesion vencio o se cerro en otro lado
                boolean vencida = ex.codigo == 403 && ex.getMessage() != null && ex.getMessage().startsWith("Sesi");
                if (respuesta != null) {
                    principal.post(() -> respuesta.error(ex.getMessage(), vencida));
                }
            } catch (JSONException ex) {
                if (respuesta != null) {
                    principal.post(() -> respuesta.error("Respuesta inesperada del servidor.", false));
                }
            }
        });
    }

    /** Llama una funcion de la base y devuelve el cuerpo JSON de la respuesta. */
    private String rpc(String funcion, JSONObject parametros) throws ApiException {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) new URL(ConfigSupabase.URL + "/rest/v1/rpc/" + funcion).openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(TIEMPO_ESPERA_MS);
            conexion.setReadTimeout(TIEMPO_ESPERA_MS);
            conexion.setRequestProperty("apikey", ConfigSupabase.CLAVE_PUBLICA);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.setRequestProperty("Accept", "application/json");
            conexion.setDoOutput(true);
            try (OutputStream out = conexion.getOutputStream()) {
                out.write(parametros.toString().getBytes(StandardCharsets.UTF_8));
            }
            int codigo = conexion.getResponseCode();
            String cuerpo = leer(codigo >= 400 ? conexion.getErrorStream() : conexion.getInputStream());
            if (codigo >= 400) {
                throw new ApiException(codigo, mensajeDeError(cuerpo));
            }
            return cuerpo;
        } catch (IOException ex) {
            throw new ApiException(0, SIN_CONEXION);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    /** La API responde {"message": "..."}; se muestra ese texto (lo escriben las funciones de la base). */
    private static String mensajeDeError(String cuerpo) {
        try {
            String mensaje = new JSONObject(cuerpo).optString("message", "");
            if (!mensaje.isEmpty()) {
                return mensaje;
            }
        } catch (JSONException ignorada) {
            // el cuerpo no era JSON: se usa el mensaje generico
        }
        return "El servidor no pudo completar la operación.";
    }

    private static String leer(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        try (InputStream entrada = in) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = entrada.read(buffer)) != -1) {
                bytes.write(buffer, 0, n);
            }
            return bytes.toString("UTF-8");
        }
    }
}
