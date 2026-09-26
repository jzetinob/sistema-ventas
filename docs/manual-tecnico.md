# Manual Técnico — Sistema de Ventas

**Universidad Mariano Gálvez · Programación II**

Autor: Josue Zetino · Repositorio: https://github.com/jzetinob/sistema-ventas

---

## 1. Visión general

El sistema tiene tres piezas que comparten los mismos datos:

```
 App de escritorio (Java Swing, MVC) ──JDBC + SSL──▶ Supabase (PostgreSQL 17)
   sistema-ventas/                                     11 tablas, RLS activo
   (sin config: base local SQLite)                     funciones app_* seguras
                                                               ▲
 App móvil Android (Java) ─────HTTPS / REST (/rpc)─────────────┘
   movil/
```

| Pieza | Tecnología | Carpeta |
|---|---|---|
| Escritorio | Java 25, Swing (MDI con `JDesktopPane`), Maven, NetBeans | `sistema-ventas/` |
| Base de datos | PostgreSQL 17 en Supabase (nube) o SQLite (local, sin configuración) | `sistema-ventas/src/main/resources/bd/` |
| Móvil | Android (Java 17; minSdk 24, compileSdk 37, targetSdk 34; sin librerías externas), Gradle | `movil/` |
| Documentación | Markdown | `docs/`, `tareas/` |

## 2. Requisitos de desarrollo

| Herramienta | Versión |
|---|---|
| JDK | 25 (`maven.compiler.release 25`) |
| Apache NetBeans | 22 o superior (probado con 25) |
| Maven | el que incluye NetBeans |
| Android Studio | 2026.1 o superior (SDK de Android, API 37) |
| Gradle / Android Gradle Plugin | 9.8 / 9.4.1 (incluidos: `movil/gradlew`) |
| Cuenta de Supabase | plan Free |

Dependencias Maven (`sistema-ventas/pom.xml`):

| Dependencia | Uso |
|---|---|
| `org.xerial:sqlite-jdbc:3.47.1.0` | Base local SQLite |
| `org.postgresql:postgresql:42.7.4` | Base en la nube |
| `org.mindrot:jbcrypt:0.4` | Hash de contraseñas (bcrypt) |

## 3. Arquitectura de la app de escritorio

Patrón **MVC** en el paquete `com.josue.ventas`:

| Paquete | Contenido |
|---|---|
| `modelo` | Entidades: `Persona` (abstracta) → `Cliente`, `Empleado`, `Proveedor`; `Producto`, `Categoria`, `Factura` + `FacturaDetalle`, `Compra` + `CompraDetalle`, `Usuario` |
| `dao` | Interfaces DAO y su implementación JDBC (`…DAOSQLite`, que funcionan también con PostgreSQL), `ConexionBD`, `CopiadorBD`, `MigradorDatos` (CSV antiguos) |
| `controlador` | Un controller por entidad, `Sesion` (usuario conectado), `Contrasenas` (hash), `RespaldoController` |
| `vista` | Formularios (`JInternalFrame`), `FrmCatalogo` (base abstracta), `DlgLogin`, `BarraBusqueda`, `FiltroTabla`, `ReporteHtml`, `Reportes`, `CampoBusqueda` |

Flujo de una operación: **vista → controlador → DAO → base de datos**. Las vistas nunca escriben SQL.

### 3.1 Patrones de diseño aplicados

| Patrón | Dónde |
|---|---|
| **MVC** | Separación `vista` / `controlador` / `modelo` + `dao` |
| **Singleton** | `ConexionBD`, cada `…DAOSQLite`, `Sesion`, `ApiSupabase` (móvil) |
| **DAO** | Interfaces `ClienteDAO`, `ProductoDAO`… con implementación intercambiable (CSV → SQLite/PostgreSQL) sin tocar las vistas |
| **Template Method** | `FrmCatalogo` arma la pantalla y define los pasos abstractos `guardar()`, `actualizar()`, `eliminar()`… que implementan `FrmCategorias`, `FrmProveedores`, `FrmUsuarios` |
| **Herencia / polimorfismo** | `Persona` abstracta; `Empleado` y `Proveedor` sobrescriben `mostrarInformacion()`; `ActividadBase` en Android |
| **Composición** | `Factura` ◆— `FacturaDetalle`, `Compra` ◆— `CompraDetalle` |

### 3.2 Selección del motor de base de datos

`ConexionBD` lee `sistema-ventas/config/bd.properties` al arrancar:

- **No existe** → SQLite en `datos/sistema_ventas.db` (crea el archivo y las tablas).
- **`bd.motor=postgresql`** → se conecta a Supabase y ejecuta `bd/esquema-postgresql.sql`, que se puede correr siempre porque es idempotente.

```properties
bd.motor=postgresql
bd.url=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres
bd.usuario=postgres.<referencia-del-proyecto>
bd.clave=<contraseña de la base de datos>
```

> `config/bd.properties` contiene la contraseña: está en `.gitignore` y **nunca** se sube a GitHub. En el repositorio solo está `bd.properties.ejemplo`.

Los DAO usan SQL estándar que funciona en ambos motores. Lo único específico de cada motor está en `ConexionBD`: la creación de tablas, las migraciones y el respaldo.

**Primera conexión a la nube:** si la base de Supabase está vacía y existe `datos/sistema_ventas.db` con datos, `CopiadorBD` los sube automáticamente. Conserva los `id` y ajusta las secuencias.

**Migraciones SQLite:** las bases creadas con versiones anteriores se actualizan solas (`agregarColumnaSiFalta`): `productos.existencia`, `productos.categoria_id`, `factura_detalles.producto_id`.

## 4. Base de datos

### 4.1 Tablas

Hay 10 tablas del sistema, más `sesiones_app` para el móvil (solo en PostgreSQL). El diagrama completo está en [`diagrama-entidad-relacion.md`](diagrama-entidad-relacion.md).

| Tabla | Descripción | Llaves foráneas |
|---|---|---|
| `categorias` | Clasificación de productos | — |
| `productos` | Catálogo, precio y existencia | `categoria_id` → categorias (SET NULL) |
| `clientes` | NIT único | — |
| `empleados` | Código único | — |
| `proveedores` | NIT único | — |
| `usuarios` | Usuario, hash de contraseña, rol, activo | — |
| `facturas` | Encabezado de venta | — |
| `factura_detalles` | Líneas de la factura | `factura_id` (CASCADE), `producto_id` (SET NULL) |
| `compras` | Encabezado de compra | `proveedor_id` (RESTRICT), `usuario_id` (SET NULL) |
| `compra_detalles` | Líneas de la compra | `compra_id` (CASCADE), `producto_id` (RESTRICT) |
| `sesiones_app` | Tokens de la app móvil (8 h) | `usuario_id` (CASCADE) |

En SQLite, las llaves foráneas se activan en cada conexión con `PRAGMA foreign_keys = ON`.

### 4.2 Transacciones

Estas operaciones se ejecutan completas o no se ejecutan (`setAutoCommit(false)` + `commit`/`rollback`):

- **Guardar factura:** inserta el encabezado y los detalles, y descuenta la existencia con `UPDATE … WHERE existencia >= ?`. Si un producto no alcanza, se revierte todo.
- **Eliminar factura:** devuelve la existencia y borra la factura (los detalles se borran en cascada).
- **Guardar compra:** inserta el encabezado y los detalles, y suma la existencia.
- **Anular compra:** resta la existencia (se rechaza si ya no alcanza) y borra la compra.

## 5. Seguridad

| Medida | Implementación |
|---|---|
| Inyección SQL | Todo dato del usuario viaja en `PreparedStatement`. Los únicos SQL armados con texto usan nombres de tablas y columnas fijos del código |
| Contraseñas | bcrypt, costo 10 (`Contrasenas`). Los hash antiguos PBKDF2-SHA256 (120 000 iteraciones) siguen siendo válidos y se convierten a bcrypt en el siguiente inicio de sesión |
| Inicio de sesión | El mismo mensaje para usuario inexistente y contraseña incorrecta; cierre tras 3 intentos; `char[]` limpiado después de usarse |
| Roles | Administrador / Vendedor; el menú se ajusta al rol (`FrmPrincipal.aplicarSesion`) |
| Reglas de negocio | No es posible eliminarse a uno mismo; siempre queda al menos un administrador activo |
| Reportes | Todo dato se escapa (`ReporteHtml.escapar`) antes de escribirse en HTML |
| Búsqueda | `Pattern.quote`: lo que escribe el usuario no se interpreta como expresión regular |
| Credenciales | La contraseña de la base solo está en `config/bd.properties` (ignorado por Git) |
| **API pública (Supabase)** | **RLS activo en las 11 tablas y sin políticas**, y permisos de tablas revocados a `anon`/`authenticated`. La API no puede leer ni escribir tablas. Solo puede ejecutar las funciones `app_*` |
| Funciones móviles | `SECURITY DEFINER` con `search_path` fijo; todas validan el token (`app_usuario_de`); el login espera 1 s tras un fallo; los tokens vencen a las 8 h y `app_logout` los elimina |

### 5.1 API de la app móvil

Todas se llaman con `POST https://<proyecto>.supabase.co/rest/v1/rpc/<función>`, el encabezado `apikey: <clave publishable>` y un cuerpo JSON.

| Función | Parámetros | Devuelve |
|---|---|---|
| `app_login` | `p_usuario`, `p_clave` | `{token, nombre, rol}` |
| `app_logout` | `p_token` | — |
| `app_productos` | `p_token`, `p_buscar` | `[{codigo, nombre, categoria, precio, existencia}]` |
| `app_clientes` | `p_token`, `p_buscar` | `[{nit, nombre, direccion, telefono}]` |
| `app_registrar_cliente` | `p_token`, `p_nit`, `p_nombre`, `p_direccion`, `p_telefono` | `{id, nit, nombre}` |
| `app_ventas` | `p_token`, `p_desde`, `p_hasta` (aaaa-mm-dd) | `[{numero_factura, fecha, cliente, total}]` |

Los errores llegan con código HTTP 4xx y `{"message": "..."}`. El mensaje ya está en español y la app lo muestra tal cual.

## 6. App móvil (`movil/`)

| Paquete | Clases |
|---|---|
| `datos` | `ConfigSupabase` (URL y clave publishable), `ApiSupabase` (singleton: llamadas HTTP en un hilo aparte con `ExecutorService`, respuesta en el hilo principal con `Handler`), `Sesion` (token en `SharedPreferences`; nunca guarda la contraseña) |
| `modelo` | `Producto`, `Cliente`, `Venta` |
| `vista` | `LoginActivity`, `MenuActivity`, `ProductosActivity`, `ClientesActivity`, `NuevoClienteActivity`, `VentasActivity`, `ActividadBase` (exige sesión), `AdaptadorFilas<T>` (lista genérica) |

No usa librerías externas: `HttpURLConnection` y `org.json` vienen incluidos en Android.

**Compilar:** abra la carpeta `movil/` en Android Studio y use *Build → Build APK(s)*, o desde la terminal ejecute `gradlew assembleDebug` con `JAVA_HOME` apuntando al JDK que trae Android Studio (carpeta `jbr`). El APK queda en `movil/app/build/outputs/apk/debug/`, y la copia lista para instalar está en `entregables/VentasMovil.apk`.

**targetSdk 34:** se deja así a propósito. Desde el API 35, Android obliga el modo de pantalla completa (*edge-to-edge*), que taparía la barra de título en este diseño con vistas clásicas.

## 7. Instalación y puesta en marcha

### 7.1 Escritorio (desarrollo)

1. Clone el repositorio y abra `sistema-ventas` en NetBeans (proyecto Maven).
2. **Modo local:** ejecute (F6). No se necesita nada más.
3. **Modo nube:** copie `config/bd.properties.ejemplo` como `config/bd.properties` y complete los datos del *Session pooler* de Supabase. Al ejecutar se crean las tablas y funciones.
4. En el primer uso, el sistema pide crear el administrador.

### 7.2 Crear el proyecto en Supabase (una vez)

1. Cree una cuenta en supabase.com, una organización del plan **Free** y un proyecto (región `us-east-1`).
2. **Connect → Session pooler:** anote el host y el usuario para `bd.properties`.
3. **Project Settings → API Keys:** copie la *Project URL* y la *publishable key* a `movil/.../ConfigSupabase.java`.
4. Ejecute la app de escritorio una vez: crea todo el esquema.

> El plan Free **pausa el proyecto tras 7 días sin actividad**. Se reactiva desde el panel de Supabase con **Restore project**.

## 8. Respaldo y restauración

- **Respaldo:** *Administración → Respaldar base de datos* genera un archivo `.db` (SQLite) con todas las tablas. Funciona igual con la base local y con la nube; desde la nube se copian las tablas con `CopiadorBD`.
- **Restaurar en local:** cierre el sistema, reemplace `datos/sistema_ventas.db` por el respaldo y quite `config/bd.properties`.
- **Restaurar en la nube:** con la base de la nube vacía, coloque el respaldo como `datos/sistema_ventas.db` y abra el sistema en modo nube. La importación automática sube los datos.

## 9. Pruebas realizadas

Se ejecutaron pruebas automatizadas contra bases temporales (SQLite) y contra el proyecto real de Supabase. Al terminar, se borraron los datos de prueba.

| Área | Qué se verificó |
|---|---|
| Esquema | 10 tablas (+1 en la nube), llaves foráneas activas, RLS en las 11 tablas |
| Migración | Una base de una versión anterior se actualiza sin perder datos |
| Usuarios | Validaciones, contraseñas hasheadas, intento de inyección SQL rechazado, conversión de PBKDF2 a bcrypt, reglas de administrador |
| Inventario | La compra suma, la factura descuenta, una venta sin existencia se rechaza, eliminar o anular devuelve la existencia |
| Integridad | No se borran productos ni proveedores con compras; las categorías pasan a SET NULL; los detalles se borran en cascada |
| Búsquedas y reportes | Filtro literal, selección correcta con la tabla filtrada, escape HTML |
| Respaldo | Local (`VACUUM INTO`) y desde la nube a `.db` |
| API móvil | Login correcto e incorrecto, token falso o cerrado rechazado, consultas, alta de cliente visible en el escritorio, **lectura directa de tablas bloqueada (401)** |
