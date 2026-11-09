# Guía de estudio — 04. La app Android

Carpeta: `movil/` (proyecto Gradle que se abre en Android Studio). Paquete: `com.josue.ventas.movil`. Instalador: `entregables/VentasMovil.apk`.

## 1. Datos generales

| Dato | Valor |
|---|---|
| Lenguaje | Java |
| Android mínimo | 7.0 (minSdk 24) |
| compileSdk / targetSdk | 37 / 34 |
| Dependencias | Solo lo que trae Android (`HttpURLConnection`, `org.json`, `Canvas`) + **SwipeRefreshLayout** (deslizar para actualizar) |
| Conexión | Internet, a la API REST de Supabase (no se conecta directo a la base) |
| Usuarios | Los mismos del escritorio |

**¿Por qué targetSdk 34?** Desde el API 35, Android obliga el modo de pantalla completa (*edge-to-edge*), que taparía la barra de título en este diseño de vistas clásicas.

**¿Por qué el celular no usa JDBC como el escritorio?** Porque tendría que llevar la contraseña de la base dentro del APK, y cualquiera podría sacarla. Con la API, el celular solo lleva la clave publishable y el token de sesión, y la base valida todo.

## 2. Pantallas

| Pantalla (Activity) | Qué hace |
|---|---|
| `LoginActivity` | Usuario y contraseña → `app_login` → guarda el token |
| `MenuActivity` | Inicio: indicadores del día (ventas, facturas, por agotarse) y menú en mosaico de colores |
| `ResumenActivity` | Gráfico de barras (7 días) y dona (top 5 del mes), con `app_resumen` |
| `NuevaVentaActivity` | Facturar desde el celular (`app_registrar_factura`): no deja pedir más de la existencia |
| **`NuevaOrdenActivity`** | **Orden de venta (tarea final)**: cliente, productos, observaciones → `app_registrar_orden_venta`. Muestra la URL del endpoint |
| `ProductosActivity` | Buscar productos; existencia ≤ 5 en rojo; funciona **sin conexión** con la última copia |
| `ClientesActivity` | Buscar por NIT, nombre o teléfono; muestra todos los teléfonos |
| `NuevoClienteActivity` | Registrar un cliente (`app_registrar_cliente`) |
| `VentasActivity` / `DetalleVentaActivity` | Ventas entre dos fechas y el detalle de cada factura |

## 3. Clases de apoyo

| Paquete | Clase | Qué hace |
|---|---|---|
| `datos` | `ConfigSupabase` | URL del proyecto y clave publishable |
| `datos` | `ApiSupabase` (Singleton, **Facade**) | Todas las llamadas a la API; cada una en un hilo aparte |
| `datos` | `Sesion` | Guarda token, nombre y rol en `SharedPreferences`. **Nunca guarda la contraseña** |
| `datos` | `CacheCatalogo` | Copia del catálogo para usar sin conexión; se borra al cerrar sesión |
| `modelo` | `Producto`, `Cliente`, `Venta`, `LineaVenta`, `Resumen` | Datos que llegan de la API |
| `vista` | `ActividadTema` → `ActividadBase` (abstractas) | Base de todas las pantallas: tema por hora y exigir sesión |
| `vista` | `AdaptadorFilas<T>`, `DialogoBusqueda<T>` | Genéricos: listas en tarjetas y diálogo para elegir cliente o producto |
| `vista` | `GraficoBarras`, `GraficoDona` | Extienden `View` y dibujan con `Canvas` en `onDraw()` |
| `vista` | `TemaPorHora`, `Formatos` | Decide claro/oscuro; formatos de dinero y fecha |

## 4. Cómo se hace una llamada a la API

Android **no permite usar la red en el hilo de la pantalla** (daría `NetworkOnMainThreadException` y congelaría la app). Por eso `ApiSupabase`:

1. Ejecuta la petición en **otro hilo** (`ExecutorService`).
2. Cuando llega la respuesta, la entrega en el **hilo principal** con un `Handler`, para poder tocar la pantalla.
3. Avisa con una interfaz de *callback*: `Respuesta<T>` con `exito(resultado)` y `error(mensaje, sesionVencida)`.

```java
// la petición HTTP (dentro del hilo aparte)
conexion = (HttpURLConnection) new URL(ConfigSupabase.URL + "/rest/v1/rpc/" + funcion).openConnection();
conexion.setRequestMethod("POST");
conexion.setRequestProperty("apikey", ConfigSupabase.CLAVE_PUBLICA);
conexion.setRequestProperty("Content-Type", "application/json");
// se escribe el JSON en el cuerpo y se lee el código de respuesta
int codigo = conexion.getResponseCode();
if (codigo >= 400) throw new ApiException(codigo, mensajeDeError(cuerpo)); // usa {"message": ...}
```

Si no hay internet, el error es "No hay conexión con el servidor…". Si la API responde 403 por sesión vencida, la app cierra la sesión y vuelve al login.

**Armar la orden de venta** (`ApiSupabase.registrarOrdenVenta`):

```java
JSONArray detalles = new JSONArray();
for (LineaVenta l : lineas) {
    detalles.put(new JSONObject().put("codigo", l.getCodigo()).put("cantidad", l.getCantidad()));
}
JSONObject p = new JSONObject().put("p_token", token).put("p_nit", nit)
        .put("p_observaciones", observaciones).put("p_detalles", detalles);
return new JSONObject(rpc("app_registrar_orden_venta", p));
```

## 5. Flujo de la Orden de Venta en el celular

1. Menú → **Orden de venta**.
2. **Cliente:** *Del catálogo* (busca con `app_clientes`) o escribir el NIT.
3. **Productos:** *Agregar* → buscar → cantidad (1 a 100 000; puede ser más que la existencia porque la orden no la descuenta). Mantener presionado para quitar.
4. **Observaciones** (opcional, hasta 500 caracteres).
5. **Enviar orden** → diálogo de confirmación con cliente, cantidad de productos y total.
6. La app valida lo básico (NIT y al menos un producto) y llama a la API.
7. **Éxito:** diálogo *Orden registrada* con número, cliente, total y estado. **Error:** el mensaje de la API (por ejemplo, *No hay un cliente registrado con el NIT 1111111-1.*).

Abajo de la pantalla se muestra la URL a la que se envía el POST (para la demostración).

## 6. Diferencia entre Nueva venta y Orden de venta

| | Nueva venta | Orden de venta |
|---|---|---|
| Qué crea | Factura `FAC-…` | Orden `OV-…` (pedido) |
| Existencia | **Descuenta** (no deja vender más de lo que hay) | **No descuenta** |
| Cliente | Del catálogo o NIT + nombre escritos | Debe estar registrado |
| Después | Aparece en *Ver facturas* | Aparece en *Órdenes de Venta*; ahí se confirma o anula |

## 7. Sesión

- `app_login` devuelve un token que `Sesion` guarda en `SharedPreferences` (con nombre y rol).
- `ActividadBase.onResume()` revisa que haya sesión; si no, manda al login.
- El token vence a las **8 horas** (lo controla la base). **Cerrar sesión** llama `app_logout`, que borra el token, y limpia los datos guardados.

## 8. Sin conexión, tema y gráficos

- **Sin conexión:** cada consulta completa del catálogo se guarda (`CacheCatalogo`). Sin internet, *Productos* muestra esa copia con un aviso y su fecha. Vender, enviar órdenes y registrar clientes sí necesitan internet.
- **Tema por hora:** `values-night/` redefine colores y estilos. `TemaPorHora` decide (claro de 06:00 a 17:59, oscuro de 18:00 a 05:59) y `ActividadTema` aplica el "modo noche"; cada minuto revisa la hora y recrea la pantalla si cambió.
- **Gráficos:** `GraficoBarras` escala cada barra al máximo de la semana; `GraficoDona` dibuja un arco por producto (`drawArc`). Sin librerías.
- **Fechas:** se usa `Calendar` y `SimpleDateFormat` porque `java.time` requiere Android 8 y la app funciona desde Android 7. El celular envía su fecha local porque el servidor trabaja en UTC.

## 9. Compilar e instalar

- **Compilar:** abrir `movil/` en Android Studio → *Build → Build APK(s)*, o `gradlew assembleDebug` con `JAVA_HOME` apuntando al JDK de Android Studio (`jbr`). El APK queda en `movil/app/build/outputs/apk/debug/`; la copia lista está en `entregables/VentasMovil.apk`.
- **Instalar:** pasar el APK al teléfono y abrirlo (permitir "orígenes desconocidos"). Si ya había una versión firmada en otra computadora, **desinstalarla primero**: Android no deja instalar encima un APK con otra firma.
- **Probado** en el emulador (Pixel 7, API 36) y en un **teléfono Android real** (6 de noviembre de 2026).
