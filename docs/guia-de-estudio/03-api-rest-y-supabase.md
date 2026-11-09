# Guía de estudio — 03. La API REST y Supabase

## 1. ¿Qué es una API REST? (explicado simple)

Una **API REST** es un servicio al que otra aplicación le hace **peticiones HTTP** y que responde con datos, normalmente en **JSON**. Es como un mesero: la app (cliente) pide algo a una dirección (URL), el servicio lo procesa en la cocina (base de datos) y devuelve el resultado.

Partes de una petición:

| Parte | En este proyecto |
|---|---|
| **Método HTTP** | `POST` (enviar datos para que el servidor haga algo) |
| **URL / endpoint** | `https://nlgvnwonkokdhbgpuoqe.supabase.co/rest/v1/rpc/app_registrar_orden_venta` |
| **Encabezados** | `apikey: <clave publishable>` y `Content-Type: application/json` |
| **Cuerpo** | JSON con los datos (`p_token`, `p_nit`, `p_detalles`…) |
| **Respuesta** | Código HTTP (200, 400, 403…) + JSON |

## 2. ¿Por qué la API es Supabase? (y no ngrok)

El enunciado de la tarea final proponía hacer una API en la computadora y publicarla con **ngrok** (que da una URL pública temporal hacia un servidor local). El proyecto ya tenía su base en **Supabase**, y Supabase **publica cada función de PostgreSQL como un endpoint REST** con HTTPS y una URL fija. Por eso:

- No hace falta un servidor en la computadora ni un túnel.
- La URL no cambia entre ejecuciones (la de ngrok gratuito sí cambia).
- Funciona desde cualquier red, incluso con la computadora apagada.

**El docente aprobó usar Supabase en lugar de ngrok.** El flujo del enunciado *Android → URL pública de ngrok → API REST local → BD → respuesta* queda así: **Android → URL pública de Supabase → API REST (PostgREST) → función PostgreSQL → tablas → respuesta JSON → Android.**

## 3. ¿Cómo funciona por dentro?

Supabase incluye **PostgREST**, un programa que expone la base como API REST. Cuando se llama `POST /rest/v1/rpc/<función>` (RPC = *remote procedure call*, llamar una función a distancia), PostgREST:

1. Toma el cuerpo JSON y pasa cada campo como parámetro de la función (`p_token`, `p_nit`…).
2. Ejecuta la función de PostgreSQL.
3. Devuelve lo que la función retorna, como JSON.
4. Si la función lanza un error (`RAISE EXCEPTION … USING ERRCODE`), lo convierte en un código HTTP con `{"message": "..."}`.

| ERRCODE de PostgreSQL | Código HTTP | Cuándo |
|---|---|---|
| (sin error) | **200 OK** (o 204 si no devuelve nada, como `app_logout`) | Todo bien |
| `22023` (dato inválido) | **400 Bad Request** | NIT inexistente, sin productos, cantidad inválida… |
| `28000` / `28P01` (autorización) | **403 Forbidden** | Token inválido o vencido; usuario o clave incorrectos |
| `23505` (único repetido) | **409 Conflict** | Registrar un cliente con un NIT que ya existe |
| Tabla sin permiso | **401** | Intento de leer una tabla directamente |

Los mensajes ya están en español dentro de la función, y la app los muestra tal cual.

## 4. Todos los endpoints

Todos son `POST https://<proyecto>.supabase.co/rest/v1/rpc/<función>` con `apikey` y cuerpo JSON.

| Función | Parámetros | Devuelve |
|---|---|---|
| `app_login` | `p_usuario`, `p_clave` | `{token, nombre, rol}` |
| `app_logout` | `p_token` | nada (204) |
| `app_productos` | `p_token`, `p_buscar` | lista de `{codigo, nombre, categoria, precio, existencia}` |
| `app_clientes` | `p_token`, `p_buscar` (NIT, nombre o teléfono) | lista de `{nit, nombre, direccion, telefono}`; `telefono` trae todos con su tipo |
| `app_registrar_cliente` | `p_token`, `p_nit`, `p_nombre`, `p_direccion`, `p_telefono` | `{id, nit, nombre}` |
| `app_ventas` | `p_token`, `p_desde`, `p_hasta` (aaaa-mm-dd) | lista de `{numero_factura, fecha, cliente, total}` |
| `app_detalle_venta` | `p_token`, `p_numero_factura` | lista de `{producto, cantidad, precio, subtotal}` |
| `app_resumen` | `p_token`, `p_hoy`, `p_existencia_baja` | indicadores, ventas de 7 días, top 5 y existencia baja |
| `app_registrar_factura` | `p_token`, `p_nit`, `p_cliente`, `p_fecha`, `p_detalles` | `{numero_factura, total}` (descuenta existencia) |
| **`app_registrar_orden_venta`** | `p_token`, `p_nit`, `p_observaciones`, `p_detalles` | `{numero_orden, cliente, total, estado}` (**no** descuenta existencia) |

`app_usuario_de(p_token)` es una función interna que usan todas las demás para validar el token; no la llama el celular.

## 5. El endpoint de la tarea final: crear una Orden de Venta

**Petición:**

```
POST https://nlgvnwonkokdhbgpuoqe.supabase.co/rest/v1/rpc/app_registrar_orden_venta
apikey: <clave publishable>
Content-Type: application/json

{
  "p_token": "<token de la sesión>",
  "p_nit": "8888888-8",
  "p_observaciones": "Entregar el lunes en bodega central",
  "p_detalles": [ { "codigo": "ESC-0102", "cantidad": 12 }, { "codigo": "CUA-0202", "cantidad": 5 } ]
}
```

**Respuestas:**

| Código | Cuerpo |
|---|---|
| 200 | `{"numero_orden": "OV-0001", "cliente": "Cliente Demo - Prueba en caliente", "total": 90.00, "estado": "PENDIENTE"}` |
| 400 | `{"message": "No hay un cliente registrado con el NIT 1111111-1."}` |
| 403 | `{"message": "Sesión inválida o vencida. Inicie sesión de nuevo."}` |

**Qué hace la función, paso a paso** (`esquema-postgresql.sql`):

1. `v_usuario := app_usuario_de(p_token);` → valida la sesión (si falla: 403).
2. Normaliza el NIT (quita guiones y espacios) y **busca el cliente**. Si no existe: 400.
3. Revisa que las observaciones no pasen de 500 caracteres.
4. Revisa que `p_detalles` sea una lista **no vacía** y **sin códigos repetidos**.
5. `LOCK TABLE ordenes_venta …` y calcula el siguiente número `OV-0001`, `OV-0002`… (el bloqueo evita números repetidos si llegan dos órdenes a la vez).
6. Inserta la orden con estado `PENDIENTE` y la fecha en hora de Guatemala.
7. Por cada producto: valida la cantidad (1 a 100 000) y que el producto exista; inserta el detalle con el **precio del catálogo** (el celular no manda precios) y suma el total.
8. Actualiza el total y devuelve el JSON de respuesta.

Si cualquier paso falla, PostgreSQL **revierte todo** (la función es una sola transacción): no queda una orden a medias.

**Prueba real (31 de octubre de 2026):** desde el emulador se creó la orden **OV-0001** (12 lapiceros a Q2.50 + 5 cuadernos a Q12.00 = **Q90.00**, estado PENDIENTE). Con el NIT `1111111-1` la API respondió 400 y no guardó nada. La orden se vio en el escritorio y en la base. La evidencia está en `tareas/10-integracion-android-api-rest/`.

## 6. Seguridad de la API

| Capa | Cómo protege |
|---|---|
| **apikey (clave publishable)** | Identifica la app ante Supabase. Es **pública por diseño** (va dentro del APK); por sí sola no permite leer ni escribir nada |
| **Token de sesión** | `app_login` verifica usuario y contraseña (bcrypt con `crypt()` de pgcrypto) y crea un token UUID en `sesiones_app` que **vence a las 8 horas**. Cada función lo valida con `app_usuario_de` (token existente, no vencido y usuario activo) |
| **Freno a ataques** | Si el login falla, la función espera 1 segundo (`pg_sleep(1)`) |
| **RLS + permisos revocados** | Las tablas no se pueden leer ni escribir por la API (401). Solo se entra por las funciones |
| **Funciones `SECURITY DEFINER` con `search_path` fijo** | Corren con permisos del dueño, pero solo hacen lo que su código permite |
| **`REVOKE ALL ON FUNCTION … FROM PUBLIC`** | Cada función queda ejecutable solo por los roles que la necesitan |
| **Precio desde la base** | El celular manda solo código y cantidad; no puede inventar precios |
| **Contraseña de la base** | No está en el repositorio |

## 7. Supabase: datos del proyecto

| Dato | Valor |
|---|---|
| Organización / proyecto | `sistema-ventas-umg` / `sistema-ventas` |
| Referencia del proyecto | `nlgvnwonkokdhbgpuoqe` (región us-east-1) |
| Plan | Free |
| Conexión del escritorio | *Session pooler* (`aws-0-us-east-1.pooler.supabase.com:5432`), usuario `postgres.<referencia>` |
| Conexión del celular | Project URL + clave publishable (en `movil/.../ConfigSupabase.java`) |

**Plan Free y pausa:** el plan gratuito **no vence**, pero **pausa el proyecto si pasa 7 días sin actividad**. Mientras está pausado, la API no responde (el celular no puede iniciar sesión) y el escritorio no se conecta en modo nube. Se reactiva en supabase.com con **Restore project**, sin perder datos. Por eso el proyecto se usa al menos una vez por semana y se anota en `docs/proyecto/estado.md`.

**Evidencia en el panel de Supabase:** el panel del proyecto (*Healthy*, peticiones de la última hora) y los registros (*Logs*), donde aparecen las peticiones de la app, por ejemplo `POST /rest/v1/rpc/app_logout` con 204 desde `Dalvik` (la máquina virtual de Android).

## 8. Cómo probar la API sin el celular

Script `api/probar-orden-venta.ps1` (PowerShell): pide la contraseña sin mostrarla, inicia sesión, envía una orden y cierra sesión.

```
cd api
.\probar-orden-venta.ps1 -Usuario jzetinob -Nit 8888888-8 -Productos "ESC-0102:12,CUA-0202:5" -Observaciones "Prueba"
```
