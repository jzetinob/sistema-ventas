# Guía de estudio — 06. Conceptos del curso y decisiones de diseño

## 1. Programación orientada a objetos en el proyecto

| Concepto | Qué es | Dónde está |
|---|---|---|
| **Clase y objeto** | La clase es el molde; el objeto, una instancia | `Producto` es la clase; cada producto del catálogo es un objeto |
| **Encapsulamiento** | Atributos `private` y acceso por `get`/`set` | Todas las clases de `modelo` |
| **Herencia** | Una clase hija reutiliza la clase padre (`extends`) | `Cliente`, `Empleado` y `Proveedor` heredan de `Persona` |
| **Clase abstracta** | No se puede instanciar; sirve de base | `Persona`, `FrmCatalogo`, `ActividadBase` y `ActividadTema` (Android) |
| **Polimorfismo (sobrescritura)** | La hija redefine un método del padre (`@Override`) | `mostrarInformacion()` en `Empleado` y `Proveedor`; `paintComponent()` en los gráficos; `onDraw()` en Android |
| **Sobrecarga** | Mismo nombre, distintos parámetros | Los constructores de `Telefono` |
| **Composición** | El "todo" crea y destruye sus partes; sin él no existen | `Factura` ◆ `FacturaDetalle`, `Compra` ◆ `CompraDetalle`, `OrdenVenta` ◆ `OrdenVentaDetalle`, `Cliente` ◆ `Telefono`. En la base: `ON DELETE CASCADE` |
| **Agregación** | La parte puede existir sin el todo | `Producto` → `Categoria`: si se borra la categoría, el producto queda sin categoría (`SET NULL`) |
| **Asociación** | Una clase usa o conoce a otra | `FacturaDetalle` → `Producto`; `Compra` → `Proveedor` |
| **Interfaces** | Contrato de métodos sin implementación | `ClienteDAO`, `FacturaDAO`…; `Printable` en `TicketFactura`; `Respuesta<T>` en Android |
| **Enum** | Lista cerrada de valores | `Usuario.Rol`, `Telefono.Tipo`, `OrdenVenta.Estado` |
| **Genéricos** | Clases que funcionan con cualquier tipo | `List<Cliente>`, `AdaptadorFilas<T>`, `DialogoBusqueda<T>` |
| **Excepciones** | `try-catch` para manejar errores | Los DAO capturan `SQLException` y hacen `rollback`; Android define `ApiException` |
| **Listas de objetos** | Colecciones en memoria | `List<Producto>`, `List<FacturaDetalle>` en DAO, controllers y vistas |
| **Traslado de información entre formularios** | Pasar datos de una ventana a otra | `FrmListaFacturas` abre `FrmDetalleFactura` con la factura elegida, y de ahí `FrmVistaPreviaFactura` |

## 2. Patrones de diseño

| Patrón | Idea | Dónde |
|---|---|---|
| **MVC** | Separar vista, lógica y datos | Paquetes `vista`, `controlador`, `modelo` + `dao` |
| **DAO** | Una clase por entidad para hablar con la base, detrás de una interfaz | `ClienteDAO` + `ClienteDAOSQLite`… Permitió cambiar CSV por SQLite y luego por PostgreSQL sin tocar las vistas |
| **Singleton** | Una sola instancia (constructor privado + `getInstancia()`) | `ConexionBD`, cada `…DAOSQLite`, `Sesion`, `ApiSupabase` |
| **Template Method** | La clase padre define el esqueleto; las hijas, los pasos | `FrmCatalogo` arma la pantalla y define `guardar()`, `actualizar()`, `eliminar()` abstractos; los implementan `FrmCategorias`, `FrmProveedores`, `FrmUsuarios` |
| **Observer** | Avisar a los interesados cuando algo pasa | Los *listeners* de Swing; `CampoBusqueda.addActionListener`; `Tema.alCambiar` avisa a las ventanas cuando cambia el tema |
| **Facade** | Una clase simple que esconde algo complejo | `ApiSupabase` en Android: las pantallas llaman `registrarOrdenVenta(...)` sin saber de HTTP, JSON ni hilos |

Ejemplo de Singleton (`FacturaDAOSQLite`):

```java
private static final FacturaDAOSQLite instancia = new FacturaDAOSQLite();
private FacturaDAOSQLite() { }
public static FacturaDAOSQLite getInstancia() { return instancia; }
```

## 3. Decisiones de diseño y por qué

| Decisión | Por qué |
|---|---|
| **Empezar con CSV y pasar a SQLite** | La tarea 3 pedía base de datos; con DAO e interfaces, el cambio fue de una línea por controller |
| **SQLite para lo local** | No necesita instalar un servidor; la base es un archivo |
| **Supabase (PostgreSQL) para la nube** | Gratis, PostgreSQL real, y además publica la API REST para el móvil sin programar un servidor |
| **Mantener los dos motores** | Sin `bd.properties` el sistema funciona igual en local (útil sin internet o para probar) |
| **El móvil usa la API, no JDBC** | Para no poner la contraseña de la base dentro del APK |
| **Supabase en lugar de ngrok (tarea final)** | URL pública fija con HTTPS, sin servidor local ni túnel; aprobado por el docente |
| **Buscador con autocompletado en lugar de combos** | Con muchos productos, un desplegable es lento de usar |
| **Correlativo = mayor número + 1** | No se repite al reiniciar y no se "queman" números al cerrar sin guardar |
| **Facturas guardan nombre de cliente y producto como texto** | Son una "foto" de la venta: no cambian si después se edita el catálogo |
| **Descontar existencia con `WHERE existencia >= ?`** | Si dos venden el último producto a la vez, el segundo falla y se revierte; nunca queda negativa |
| **La orden de venta no descuenta existencia** | Es un pedido; la existencia baja cuando se factura. Así una orden anulada no deja el inventario mal |
| **El precio lo pone la base (móvil)** | El celular solo manda código y cantidad; no se puede alterar el precio |
| **Varios teléfonos en tabla aparte (1 a N)** | Sin límite fijo; conservar `clientes.telefono` como principal evitó romper reportes y la app móvil |
| **bcrypt para contraseñas** | Hash lento y con sal; PostgreSQL lo puede verificar con `pgcrypto` en el login del móvil |
| **RLS sin políticas + funciones `app_*`** | La API no puede tocar tablas; solo ejecutar funciones que validan el token |
| **Gráficos hechos a mano, sin librerías** | Sin dependencias, y cada línea se puede explicar (herencia de `View`/`JPanel`, sobrescribir `onDraw`/`paintComponent`) |
| **targetSdk 34 en Android** | Evita el modo *edge-to-edge* obligatorio del API 35, que taparía la barra de título |
| **Contraseña de la base fuera de Git** | `config/bd.properties` está en `.gitignore`; en el repo solo va el `.ejemplo` |

## 4. Código seguro (resumen)

- Consultas parametrizadas (`PreparedStatement` con `?`) → sin inyección SQL.
- Contraseñas con bcrypt; mismo mensaje de error para usuario o clave; cierre tras 3 intentos; espera de 1 s en el login del móvil.
- Roles (Administrador / Vendedor) y reglas: no eliminarse a uno mismo, siempre un administrador activo.
- Datos escapados en reportes HTML; búsqueda literal con `Pattern.quote`.
- En la nube: RLS, permisos revocados, token de 8 horas, funciones con `search_path` fijo.
- Credenciales fuera del repositorio.
