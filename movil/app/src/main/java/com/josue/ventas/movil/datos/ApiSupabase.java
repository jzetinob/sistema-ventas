package com.josue.ventas.movil.datos;

import android.os.Handler;
import android.os.Looper;
import com.josue.ventas.movil.modelo.Cliente;
import com.josue.ventas.movil.modelo.Producto;
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

    public void productos(String token, String buscar, Respuesta<List<Producto>> respuesta) {
        ejecutar(() -> {
            JSONArray filas = new JSONArray(rpc("app_productos",
                    new JSONObject().put("p_token", token).put("p_buscar", buscar)));
            List<Producto> lista = new ArrayList<>();
            for (int i = 0; i < filas.length(); i++) {
                JSONObject f = filas.getJSONObject(i);
                lista.add(new Producto(f.getString("codigo"), f.getString("nombre"),
                        f.isNull("categoria") ? "" : f.getString("categoria"),
                        f.getDouble("precio"), f.getInt("existencia")));
            }
            return lista;
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
            throw new ApiException(0, "No hay conexión con el servidor. Revise su internet e intente de nuevo.");
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
