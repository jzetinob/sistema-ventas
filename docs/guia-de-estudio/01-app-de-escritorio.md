# Guía de estudio — 01. La app de escritorio

Carpeta: `sistema-ventas/` (proyecto Maven que se abre en NetBeans). Paquete raíz: `com.josue.ventas`.

## 1. Cómo arranca

`SistemaVentas.main()` es el punto de entrada:

1. **Aplica el tema** (`Tema.instalar`): FlatLaf claro u oscuro según la hora.
2. **Conecta la base** (`ConexionBD.getInstancia()`): si hay error, lo muestra y cierra.
3. **Pide iniciar sesión** (`DlgLogin`). La primera vez, si no hay usuarios, pide crear el administrador.
4. **Abre la ventana principal** (`FrmPrincipal`) y ajusta el menú al rol del usuario.

```java
String error = ConexionBD.getInstancia().getErrorConexion();
if (error != null) { /* muestra el error y cierra */ }
if (!new DlgLogin().mostrar()) { System.exit(0); }
FrmPrincipal ventana = new FrmPrincipal();
ventana.setVisible(true);
```

## 2. Arquitectura MVC

El código está en 4 paquetes. Una operación siempre va **vista → controlador → DAO → base de datos**, y la respuesta regresa por el mismo camino. **Las vistas nunca escriben SQL.**

| Paquete | Qué contiene | Ejemplo |
|---|---|---|
| `modelo` | Las entidades: clases con atributos privados, getters y setters | `Factura`, `Producto`, `Cliente` |
| `dao` | Acceso a datos: interfaces y su implementación con JDBC | `FacturaDAO` (interfaz), `FacturaDAOSQLite` |
| `controlador` | Lógica de negocio y validaciones; une la vista con el DAO | `FacturaController`, `ClienteController` |
| `vista` | Ventanas y componentes Swing | `FrmFactura`, `FrmPrincipal` |

**Ejemplo completo — guardar una factura:**

1. `FrmFactura` (vista) arma un objeto `Factura` con su cliente y sus `FacturaDetalle`, y llama `controller.Guardar(factura)`.
2. `FacturaController` (controlador) llama `dao.guardar(factura)`. El controller recibe el DAO por su interfaz:
   ```java
   FacturaDAO dao;
   public FacturaController() { dao = FacturaDAOSQLite.getInstancia(); }
   public boolean Guardar(Factura factura) { return dao.guardar(factura); }
   ```
3. `FacturaDAOSQLite` (DAO) abre una **transacción**: inserta el encabezado, los detalles y descuenta la existencia. Si un producto no alcanza, hace `rollback` y devuelve `false`.
4. La vista muestra "guardada" o el aviso de existencia insuficiente.

Como el controller usa la **interfaz** `FacturaDAO`, en la tarea 3 se pasó de archivos CSV a base de datos **cambiando una sola línea** (`FacturaDAOCsv.getInstancia()` → `FacturaDAOSQLite.getInstancia()`), sin tocar las vistas.

## 3. Las clases, paquete por paquete

### 3.1 `modelo`

| Clase | Qué representa |
|---|---|
| `Persona` (abstracta) | `id`, `nombre`, `nit`, `telefono` y `mostrarInformacion()` |
| `Cliente extends Persona` | + `direccion` y una `List<Telefono>` (varios teléfonos) |
| `Telefono` | `clienteId`, `numero` y `tipo` (enum `Tipo`: PERSONAL, TRABAJO, CASA, OTRO) |
| `Empleado extends Persona` | + `codigoEmpleado`, `puesto`; **sobrescribe** `mostrarInformacion()` |
| `Proveedor extends Persona` | + `direccion`, `correo`; **sobrescribe** `mostrarInformacion()` |
| `Producto` | `codigo`, `nombre`, `precio`, `existencia`, `categoria`; `hayExistencia(cantidad)` |
| `Categoria` | `nombre`, `descripcion` |
| `Factura` | `numeroFactura`, `fecha`, `cliente`, `List<FacturaDetalle>`, `total`; `agregarDetalle()`, `calcularTotal()` |
| `FacturaDetalle` | `producto`, `cantidad`, `precioUnitario`; `calcularSubtotal()` |
| `Compra` / `CompraDetalle` | Compra a un proveedor (`COM-0001`), con `costoUnitario` por producto |
| `OrdenVenta` / `OrdenVentaDetalle` | Pedido enviado desde el celular (`OV-0001`); enum `Estado` (PENDIENTE, CONFIRMADA, ANULADA); `estaPendiente()` |
| `Usuario` | `usuario`, `nombre`, `claveHash`, `salt`, `rol` (enum `Rol`: ADMINISTRADOR, VENDEDOR), `activo`; `esAdministrador()` |

### 3.2 `dao`

| Clase | Qué hace |
|---|---|
| `ConexionBD` (Singleton) | Lee `config/bd.properties`: con `bd.motor=postgresql` se conecta a Supabase y ejecuta el script de la nube; sin el archivo usa SQLite y crea las tablas |
| `XxxDAO` (interfaces) | El "contrato": `guardar`, `listar`, `actualizar`, `eliminar`… para Cliente, Producto, Factura, Compra, Categoria, Proveedor, Empleado, Usuario, Telefono y OrdenVenta |
| `XxxDAOSQLite` (Singleton) | La implementación con JDBC y `PreparedStatement`. Funciona igual con SQLite y con PostgreSQL porque usa SQL estándar |
| `XxxDAOCsv` + `CsvUtil` | La versión con archivos CSV de las primeras tareas; queda en el código como referencia |
| `MigradorDatos` | Importa una sola vez los CSV antiguos si la base local está vacía |
| `CopiadorBD` | Sube los datos de SQLite a la nube la primera vez, y copia la nube a un archivo `.db` (respaldo) |

### 3.3 `controlador`

| Clase | Qué hace |
|---|---|
| `ClienteController`, `ProductoController`, `FacturaController`, `CompraController`, `CategoriaController`, `ProveedorController`, `EmpleadoController` | Un controller por entidad: validan y llaman al DAO |
| `UsuarioController` | Crear usuarios, iniciar sesión, cambiar contraseña. Reglas: el mismo mensaje si el usuario no existe o la clave es incorrecta, no se puede eliminar a uno mismo, siempre queda un administrador activo |
| `OrdenVentaController` | Lista las órdenes del celular y las confirma o anula (solo si están PENDIENTE). Devuelve `null` si salió bien o el mensaje de error |
| `ResumenController` | Calcula los indicadores y datos de los gráficos del resumen |
| `RespaldoController` | Genera el respaldo `.db` |
| `Sesion` (Singleton) | Guarda el usuario que inició sesión |
| `Contrasenas` | Hash de contraseñas con bcrypt (costo 10); acepta los hash antiguos PBKDF2 y los convierte |

### 3.4 `vista`

| Clase | Qué es |
|---|---|
| `FrmPrincipal` | La ventana **MDI**: un `JDesktopPane` y el menú. Abre cada formulario como `JInternalFrame`; si ya está abierto, lo trae al frente (`isDisplayable()`) |
| `DlgLogin` | Inicio de sesión (3 intentos) y creación del administrador en el primer uso |
| `FrmFactura` | Factura: número automático, buscador de cliente y producto, tabla, total, Guardar e Imprimir |
| `FrmListaFacturas` / `FrmDetalleFactura` | Facturas registradas, su detalle y eliminar (devuelve existencia) |
| `FrmVistaPreviaFactura` / `TicketFactura` | Vista previa e impresión del ticket (`Printable` + `Graphics2D`) |
| `FrmProductos`, `FrmClientes`, `FrmEmpleados` | Catálogos hechos con el diseñador de NetBeans |
| `FrmCatalogo` (abstracta) | Plantilla de catálogo (**Template Method**); de ella heredan `FrmCategorias`, `FrmProveedores` y `FrmUsuarios` |
| `PanelTelefonos` | Componente para agregar, modificar y quitar teléfonos dentro de `FrmClientes` |
| `FrmCompra` / `FrmListaCompras` | Compras a proveedores y su lista (anular) |
| `FrmOrdenesVenta` | Órdenes que llegan del celular: lista, detalle, Confirmar y Anular |
| `FrmResumen` + `GraficoBarrasPanel` + `GraficoDonaPanel` + `PanelTarjeta` | Tablero con indicadores y gráficos dibujados a mano |
| `CampoBusqueda` | Buscador con autocompletado (se usa en factura y compra) |
| `BarraBusqueda` / `FiltroTabla` | Barra "Buscar + Reporte HTML" que filtra cualquier tabla mientras se escribe |
| `ReporteHtml` / `Reportes` | Genera reportes HTML y los abre en el navegador |
| `Tema` | Tema claro/oscuro según la hora (FlatLaf) |
| `Validaciones` | Validaciones de formato que comparten los formularios: NIT (8 a 13 dígitos), correo y teléfono (8 a 15 dígitos) |

## 4. Menú y roles

| Menú | Opciones |
|---|---|
| **Archivo** | Nueva factura (Ctrl+N), Ver facturas, Órdenes de Venta, Cambiar mi contraseña, Cerrar sesión, Salir |
| **Catálogos** | Productos, Empleados, Clientes, Categorías, Proveedores |
| **Compras** | Nueva compra, Ver compras |
| **Edición** | Limpiar formulario |
| **Ventana** | Cascada, Mosaico, Minimizar todo, Restaurar todo, Tema (Automático, Claro, Oscuro) |
| **Administración** | Usuarios, Respaldar base de datos |
| **Reportes** | Resumen con gráficos, Inventario, Existencia baja, Ventas por período, Compras por proveedor |
| **Ayuda** | Acerca de |

`FrmPrincipal.aplicarSesion()` oculta opciones según el rol: al **Vendedor** se le ocultan Productos, Empleados, Categorías, Proveedores y los menús Compras, Administración y Reportes. Ve el menú Archivo completo (facturas y órdenes de venta), Catálogos → Clientes, Edición, Ventana y Ayuda. La barra de título muestra el usuario, su rol y si la base es local o en la nube.

## 5. Funciones, una por una

**Facturación**
- El número (`FAC-0001`…) se calcula como el mayor número de la tabla + 1. No hay contador aparte, así que no se repite ni se "quema" al cerrar sin guardar.
- Cliente y producto se eligen con `CampoBusqueda`: se escribe parte del NIT, código o nombre y se elige con flechas + Enter.
- Al guardar se **descuenta la existencia** en la misma transacción: `UPDATE productos SET existencia = existencia - ? WHERE id = ? AND existencia >= ?`. Si no alcanza, no se guarda nada y se avisa.
- **Eliminar** una factura devuelve la existencia.
- **Imprimir:** `TicketFactura` implementa `java.awt.print.Printable` y dibuja el ticket con `Graphics2D`; la vista previa usa el mismo dibujo.

**Compras a proveedores**
- Proveedor + productos con **cantidad y costo unitario** (lo que se le paga al proveedor, distinto del precio de venta). Propone el costo de la última compra.
- Al guardar **suma** la existencia. **Anular** la resta, y no se permite si esa mercadería ya se vendió.

**Catálogos**
- Todos tienen Guardar, Actualizar, Eliminar, Limpiar, **Buscar** y **Reporte HTML**.
- Clientes: **varios teléfonos** por cliente con `PanelTelefonos`. El primero se copia a `clientes.telefono` como teléfono principal.
- Usuarios (solo administrador): la contraseña mínima es de 6 caracteres; *Activo* desactiva sin borrar.

**Órdenes de venta** (tarea final)
- Llegan desde el celular por la API REST. `FrmOrdenesVenta` muestra número, fecha, cliente, quién la envió, estado y total; al seleccionar una, sus productos y observaciones.
- **Confirmar** o **Anular** solo si está `PENDIENTE`. El DAO usa `UPDATE … WHERE id = ? AND estado = 'PENDIENTE'`, así que una orden ya cambiada no se puede cambiar otra vez.
- La orden **no descuenta existencia** (es un pedido; la existencia baja al facturar).

**Reportes**
- Resumen con gráficos: ventas de hoy, facturas de hoy, ventas del mes, barras de los últimos 7 días, dona con el top 5 del mes y productos con existencia ≤ 5.
- Inventario valorizado (precio × existencia), existencia baja, ventas por período y compras por proveedor.
- Se generan en HTML (`ReporteHtml`, que **escapa** los datos) y se abren en el navegador para imprimir o guardar como PDF.

**Respaldo:** *Administración → Respaldar base de datos* crea un archivo `.db` (SQLite). En local usa `VACUUM INTO`; desde la nube copia las tablas con `CopiadorBD`.

**Tema:** claro de 06:00 a 17:59 y oscuro de 18:00 a 05:59. Un `javax.swing.Timer` revisa la hora cada minuto. Se puede fijar en *Ventana → Tema* y se recuerda con `Preferences`.

## 6. Validaciones

- NIT de 8 a 13 dígitos (guiones opcionales); no se repite en clientes ni en proveedores.
- Código de producto y de empleado únicos; precio > 0 y existencia ≥ 0.
- Teléfonos: 8 a 15 dígitos, con tipo, sin repetirse en el mismo cliente.
- Factura: sin productos repetidos y con existencia suficiente.
- No se borra un producto o proveedor que tiene compras; al borrar una categoría, sus productos quedan sin categoría.

## 7. Seguridad

| Medida | Cómo |
|---|---|
| Inyección SQL | Todos los datos van en `PreparedStatement` con `?` |
| Contraseñas | bcrypt; nunca en texto plano. El arreglo `char[]` de la contraseña se limpia después de usarse |
| Inicio de sesión | Mismo mensaje para usuario inexistente y clave incorrecta; cierre tras 3 intentos |
| Roles | El menú se ajusta al rol |
| Reportes | Los datos se escapan antes de escribirse en HTML |
| Búsqueda | `Pattern.quote`: lo que se escribe no se interpreta como expresión regular |
| Credenciales | La contraseña de la base solo está en `config/bd.properties`, que Git ignora |

## 8. Cómo ejecutarla

1. Abrir `sistema-ventas/` en NetBeans (JDK 25) y ejecutar (F6).
2. **Modo nube:** copiar `config/bd.properties.ejemplo` como `config/bd.properties` y completar los datos del *Session pooler* de Supabase. **Modo local:** sin ese archivo usa SQLite.
3. En el primer uso, crear el administrador.
