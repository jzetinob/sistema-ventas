# API REST del Sistema de Ventas

La app Android se comunica con la base de datos mediante una **API REST**: peticiones HTTPS `POST` con cuerpo JSON, que responden JSON. La API está publicada en **Supabase**, que expone cada función de PostgreSQL como un endpoint REST:

```
POST https://nlgvnwonkokdhbgpuoqe.supabase.co/rest/v1/rpc/<funcion>
```

```
App Android ──HTTPS POST (JSON)──▶ API REST de Supabase ──▶ función PostgreSQL ──▶ tablas
     ▲                                                              │
     └──────────── respuesta JSON: datos o {"message": error} ◀─────┘
```

La API ya tiene una **URL pública con HTTPS**, por eso no hace falta levantar un servidor en la computadora ni un túnel con ngrok. El docente aprobó usar Supabase para la tarea final.

## Endpoint de la tarea final: crear una Orden de Venta

```
POST https://nlgvnwonkokdhbgpuoqe.supabase.co/rest/v1/rpc/app_registrar_orden_venta
apikey: <clave publishable>
Content-Type: application/json
```

```json
{
  "p_token": "4f1c…-token-de-sesion",
  "p_nit": "8888888-8",
  "p_observaciones": "Entregar el lunes en bodega central",
  "p_detalles": [
    { "codigo": "ESC-0102", "cantidad": 12 },
    { "codigo": "CUA-0202", "cantidad": 5 }
  ]
}
```

| Respuesta | Cuándo | Cuerpo |
|---|---|---|
| **200 OK** | La orden se guardó | `{"numero_orden": "OV-0001", "cliente": "…", "total": 90.00, "estado": "PENDIENTE"}` |
| **400 Bad Request** | Un dato no es válido | `{"message": "No hay un cliente registrado con el NIT …"}` |
| **403 Forbidden** | El token de sesión no existe o venció | `{"message": "Sesión inválida o vencida. Inicie sesión de nuevo."}` |

**Validaciones que hace la API antes de guardar** (si falla alguna, no se guarda nada):

- El token de sesión es válido y el usuario está activo.
- El cliente existe (se busca por NIT, sin importar guiones ni espacios).
- Hay al menos un producto, ninguno repetido, y cada cantidad está entre 1 y 100 000.
- Cada producto existe en el catálogo.
- Las observaciones no pasan de 500 caracteres.

**Qué guarda:** una fila en `ordenes_venta` (número correlativo `OV-0001`, cliente, usuario que la envió, fecha, estado `PENDIENTE`, observaciones y total) y una fila por producto en `orden_venta_detalles`. El precio lo toma la base del catálogo, no lo envía el teléfono. Todo ocurre en una transacción, con un bloqueo de tabla para que dos órdenes simultáneas no reciban el mismo número. La orden **no descuenta existencia**: en el escritorio se confirma o se anula (*Archivo → Órdenes de Venta*).

## Seguridad

- **Clave publishable (`apikey`):** identifica la app ante Supabase. Es pública por diseño (va dentro de la app); por sí sola no permite leer ni escribir nada.
- **Token de sesión (`p_token`):** se obtiene con `app_login` usando el mismo usuario y contraseña del escritorio, y vence a las 8 horas. Cada función lo valida.
- **Tablas protegidas:** RLS activo y permisos revocados al rol público. Leer `ordenes_venta` directamente por la API responde **401**; solo se puede entrar por las funciones, que validan todo.
- La contraseña de la base de datos **no está en el repositorio** (va en `sistema-ventas/config/bd.properties`, ignorado por Git).

## Todos los endpoints

| Función | Uso |
|---|---|
| `app_login` / `app_logout` | Iniciar y cerrar sesión (devuelve el token) |
| `app_productos`, `app_clientes` | Consultar el catálogo (los clientes incluyen todos sus teléfonos) |
| `app_registrar_cliente` | Registrar un cliente desde el teléfono |
| `app_registrar_factura` | Facturar desde el teléfono (descuenta existencia) |
| **`app_registrar_orden_venta`** | **Crear una Orden de Venta (tarea final)** |
| `app_ventas`, `app_detalle_venta`, `app_resumen` | Ventas por fecha, detalle e indicadores |

Parámetros y respuestas de cada una: [manual técnico, sección 5.1](../docs/manual-tecnico.md).

## Dónde está el código

| Qué | Dónde |
|---|---|
| Tablas y funciones de la API (SQL) | [`sistema-ventas/src/main/resources/bd/esquema-postgresql.sql`](../sistema-ventas/src/main/resources/bd/esquema-postgresql.sql) (busque `app_registrar_orden_venta`) |
| Cliente de la API en Android | [`movil/.../datos/ApiSupabase.java`](../movil/app/src/main/java/com/josue/ventas/movil/datos/ApiSupabase.java), método `registrarOrdenVenta` |
| Formulario Android | [`movil/.../vista/NuevaOrdenActivity.java`](../movil/app/src/main/java/com/josue/ventas/movil/vista/NuevaOrdenActivity.java) |
| Consulta en el escritorio | [`sistema-ventas/.../vista/FrmOrdenesVenta.java`](../sistema-ventas/src/main/java/com/josue/ventas/vista/FrmOrdenesVenta.java) |

## Cómo ponerla en marcha

1. **Instalar o actualizar la API:** abra la app de escritorio en modo nube (`sistema-ventas/config/bd.properties`). Al conectarse ejecuta `esquema-postgresql.sql`, que crea las tablas y funciones; se puede ejecutar siempre sin duplicar nada. También se puede pegar ese script en el *SQL Editor* de Supabase.
2. **Probarla desde la computadora** (pide la contraseña sin mostrarla):
   ```
   cd api
   .\probar-orden-venta.ps1 -Usuario jzetinob -Nit 8888888-8 -Productos "ESC-0102:12,CUA-0202:5" -Observaciones "Prueba"
   ```
3. **Usarla desde Android:** en la app, menú **Orden de venta** → elegir cliente y productos → **Enviar orden**. La pantalla muestra la URL del endpoint al que envía el POST.
4. **Comprobar que se guardó:** en el escritorio, *Archivo → Órdenes de Venta*, o en la base:
   ```sql
   SELECT o.numero_orden, o.fecha, c.nombre, o.estado, o.total
   FROM ordenes_venta o JOIN clientes c ON c.id = o.cliente_id
   ORDER BY o.id DESC;
   ```
