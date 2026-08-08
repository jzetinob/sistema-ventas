# Plan: Completar el Sistema de Ventas

**Fecha:** 2026-08-01
**Proyecto:** Sistema de Ventas
**Repo:** https://github.com/jzetinob/sistema-ventas
**Documentación completa:** [README.md](../README.md) · [ARQUITECTURA.md](ARQUITECTURA.md)

## Estado actual

| Fase | Estado |
|---|---|
| 1 — Persistencia CSV | ✅ Completada |
| 2 — Lista de facturas completa | ✅ Completada |
| 3 — Catálogos | ✅ Completada |
| 4 — Combos en la factura | ✅ Completada |
| 5 — Impresión con vista previa | ✅ Completada |
| 6 — Validaciones | ✅ Completada |
| 7 — Pruebas y documentación | ⏳ En curso |
| 8 — Formulario contenedor MDI | ✅ Completada |
| 9 — Base de datos SQLite (JDBC) | ✅ Completada |
| 10 — Publicación y control de versiones | ✅ Completada |
| 11 — Implementación del diagrama de clases | ✅ Completada |

Este archivo es el **historial de decisiones** (el "por qué" de cada cosa). La descripción técnica de cómo está implementado vive en [ARQUITECTURA.md](ARQUITECTURA.md).

## Cómo saber si una fase está terminada

Regla de cierre de fase (se cumple cuando el commit de la fase existe):

1. **Compila** — el proyecto compila sin errores.
2. **Se probó** — el flujo correspondiente se probó (en NetBeans o con prueba automatizada).
3. **Estado actualizado** — la tabla de estado de este archivo tiene la fase en "✅ Completada".
4. **Commit hecho** — el historial de git tiene el commit de la fase (todos se nombran con "Fase N: ...").

Si la fase aparece "✅ Completada" en la tabla y su commit está en `git log`, **no se vuelve a ejecutar**: ya está implementada. Los fixes posteriores a una fase terminada van en commits separados y se anotan en "Correcciones posteriores al plan".

Para verificar rápido:

```
git status                     # debe estar limpio si todo lo planeado está subido
git log --oneline              # muestra los commits de cada fase
```

## Contexto
El objetivo era un formulario de facturación con menú principal (Archivo, Edición, Ayuda en español). El software ya tiene: ventana principal con menú, factura funcional (cliente, fecha, número, tabla de productos, total, Agregar/Eliminar/Guardar) y lista de facturas registradas. Se decidió completarlo para que sea un sistema de ventas completo, con persistencia de datos.

## Decisiones tomadas
1. **Persistencia en CSV / texto plano** (sin librerías) como puente hasta SQLite. Cuando se necesite base de datos, solo se crea un DAO nuevo (`FacturaDAOSQLite`) y se cambia una línea en el controlador; la interfaz `FacturaDAO` ya lo permite.
2. **Catálogos integrados con la factura mediante combos** (JComboBox): el producto se elige del catálogo y el precio se autollena; el cliente se autollena con su NIT. (Reemplazada más adelante por el buscador autocompletado, ver "Correcciones posteriores al plan").
3. **Impresión con vista previa**: se dibuja el ticket con Graphics2D y se imprime con el diálogo estándar de Windows (`java.awt.print.Printable`), sin dependencias externas.
4. Todo sigue el patrón **MVC** (modelo, dao, controlador, vista) y las relaciones de asociación, agregación y composición se conservan.

## Detalle de implementación importante
`FacturaDetalle` es una clase anidada package-private (composición). El DAO (paquete `dao`) y las vistas (paquete `vista`) no pueden iterar sus campos directamente. Por eso `Factura` ganó un método público `getDetallesFilas()` que devuelve `Object[][]` (producto, cantidad, precio, subtotal), manteniendo el encapsulamiento.

## Fases

### Fase 1 — Persistencia CSV
- `dao/CsvUtil.java`: escapar/parsear campos separados con `;` (evita romper si un campo trae `;` o salto de línea).
- `dao/FacturaDAOCsv.java` (patrón Singleton): carga las CSV al arrancar; en cada guardar/eliminar reescribe:
  - `datos/facturas.csv` → `id;numeroFactura;nit;cliente;fecha;total`
  - `datos/detalles.csv` → `idFactura;producto;cantidad;precio;subtotal`
  - `datos/contador.txt` → próximo id y próximo correlativo (para que el número de factura no se repita aunque se elimine la última).
- `FacturaDAO.java`: se agregan `obtenerSiguienteNumeroFactura()` y `eliminarConDetalles(int id)` a la interfaz.
- `FacturaController.java`: usa `FacturaDAOCsv`.
- `FrmFactura.java`: el número de factura se autollena (`FAC-0001`, `FAC-0002`, ...) y queda solo lectura.
- `.gitignore`: se ignora la carpeta `datos/` (los datos no se suben al repo).

### Fase 2 — Lista de facturas completa
- `FrmListaFacturas.java`: botones **Eliminar** (con confirmación, llama `EliminarConDetalles`), **Ver Detalle** y **Actualizar**.
- Nueva `vista/FrmDetalleFactura.java`: muestra datos del cliente y la tabla de productos de la factura seleccionada (usa `getDetallesFilas()`).

### Fase 3 — Catálogos (Productos y Clientes)
- `modelo/Producto.java` (id, código, nombre, precio) y `modelo/Cliente.java` (id, nit, nombre, dirección, teléfono).
- DAOs CSV + controllers con CRUD completo (Guardar, Listar, Actualizar, Eliminar). Validación de código/NIT únicos.
- `vista/FrmProductos.java` y `vista/FrmClientes.java`: formularios tipo CRUD con tabla.
- `FrmPrincipal.java`: nuevo menú **Catálogos** → Productos / Clientes.

### Fase 4 — Combos en la factura
- `FrmFactura.java`: `txtProducto` se convierte en `cmbProducto` (muestra "código - nombre"; al elegir autollena el precio). `cmbCliente` autollena NIT + nombre. Los campos siguen siendo editables para entrada manual. (Ver la corrección del buscador más abajo.)

### Fase 5 — Impresión con vista previa
- `vista/TicketFactura.java`: implementa `java.awt.print.Printable`; dibuja el ticket (cliente, fecha, número, productos, total).
- `vista/FrmVistaPreviaFactura.java`: diálogo que pinta el ticket (mismo painter) + botón **Imprimir**.
- Botón **Imprimir** en `FrmFactura`; el ticket también se puede imprimir desde el detalle de factura.

### Fase 6 — Validaciones
- NIT con formato guatemalteco (8-13 dígitos, guiones opcionales).
- Número de factura duplicado.
- Producto duplicado en la misma factura.
- Código de producto y NIT de cliente duplicados en catálogos.

### Fase 7 — Pruebas y documentación
- Compilar después de cada fase (`javac` manual, sin Maven local) y probar el flujo completo en NetBeans.
- Commits incrementales y push a `origin/main`.
- Documentación final con explicación (asociación, agregación, composición, singleton, persistencia CSV, impresión).

## Notas para el futuro (SQLite)

> ✅ **Implementado en la Fase 9.** Se dejó escrita la nota original para documentar cómo se cumplió.

- Agregar dependencia `org.xerial:sqlite-jdbc` al `pom.xml`. → ✅ `3.47.1.0`.
- Crear `FacturaDAOSQLite` (y análogos para Producto/Cliente) que implementen las mismas interfaces. → ✅ ver Fase 9.
- Cambiar la instancia usada en los controllers (1 línea por controller). → ✅.
- El resto del código no se toca. → ✅ las vistas no cambiaron.

## Correcciones posteriores al plan
- Se corrigió el orden de inicialización del singleton CSV (la instancia se creaba antes que las constantes de rutas, provocando NPE) y el correlativo de factura ahora se consume al pre-fill (evita números repetidos).
- La persistencia se verificó con una prueba automatizada: guardar → simular reinicio (nueva JVM) → los datos se recuperan y el correlativo continúa (FAC-0002).
- **Buscador inteligente**: la decisión 2 (combos) se reemplazó por el componente reutilizable `CampoBusqueda` (autocompletado): se escribe y filtra por código/NIT o nombre, con flechas + Enter o clic para elegir. Motivo: con catálogos grandes, un desplegable no es productivo. Si algún día hay millones de registros, la búsqueda real debe pasar a SQLite con `LIKE`.
- **Correlativo sin quemar**: `obtenerSiguienteNumeroFactura()` ya no incrementa el contador (solo "peek"); el contador avanza recién al guardar la factura (el DAO adelanta el correlativo según el último número usado). Antes, abrir y cerrar el formulario sin guardar perdía números.
- **Detalles del formulario**: precio y subtotal se muestran con formato de 2 decimales en la tabla; el total se toma de `facturaActual.getTotal()` (modelo) en lugar de parsear el label de la UI.
- **Errores de I/O visibles**: los `catch` vacíos de `FacturaDAOCsv` ahora registran las fallas con `java.util.logging.Logger`.
- **Compilación**: `maven.compiler.release` bajó de 26 a 25 para coincidir con el JDK instalado.

## Fase 8 — Formulario contenedor (MDI)
Corresponde a la tarea "Investigación: Formulario contenedor (Menú MDI)" y convierte la aplicación a la arquitectura MDI:
- `FrmPrincipal` es ahora el **contenedor MDI**: su contenido es un `JDesktopPane` y el `JMenuBar` incluye el menú **Ventana** (Cascada, Mosaico, Minimizar todo, Restaurar todo).
- Todos los formularios (`FrmFactura`, `FrmProductos`, `FrmClientes`, `FrmListaFacturas`, `FrmDetalleFactura`) pasaron de `JFrame` a **`JInternalFrame`** (ventanas internas movibles, minimizables, maximizables, redimensionables y cerrables dentro del escritorio).
- `FrmListaFacturas` recibe una referencia al `JDesktopPane` para abrir el detalle de factura como ventana interna del mismo escritorio.
- `FrmVistaPreviaFactura` (diálogo modal) ahora acepta cualquier `Component` como padre y resuelve la ventana contenedora.
- Se eliminaron los `main()` de los formularios hijos (las ventanas internas no se pueden mostrar fuera de un escritorio).

## Fase 9 — Base de datos SQLite (JDBC)

Corresponde a la tarea "Integración del proyecto con Base de Datos" y reemplaza el almacenamiento en CSV por una base de datos relacional:

- **Dependencia**: `org.xerial:sqlite-jdbc:3.47.1.0` en el `pom.xml` (controlador JDBC de SQLite).
- **`dao/ConexionBD.java`** (singleton): abre la conexión con `DriverManager.getConnection("jdbc:sqlite:datos/sistema_ventas.db")` y ejecuta `CREATE TABLE IF NOT EXISTS` para las 4 tablas.
- **Esquema**: `clientes` (nit UNIQUE), `productos` (codigo UNIQUE), `facturas` (numero_factura UNIQUE) y `factura_detalles` (FK `factura_id` → `facturas(id)` con `ON DELETE CASCADE`).
- **DAOs SQLite**: `ClienteDAOSQLite`, `ProductoDAOSQLite` y `FacturaDAOSQLite` implementan las interfaces `ClienteDAO`, `ProductoDAO` y `FacturaDAO` con `PreparedStatement` (evita inyección SQL). Usan `RETURN_GENERATED_KEYS` para el id autoincremental.
- **Transacciones**: guardar/actualizar factura ejecuta factura + detalles dentro de una transacción (`setAutoCommit(false)` + `commit`/`rollback`). `eliminarConDetalles` borra la factura y el CASCADE elimina sus detalles.
- **Correlativo**: `obtenerSiguienteNumeroFactura()` calcula `MAX` del número FAC-XXXX en la tabla (sin contador externo).
- **`dao/MigradorDatos.java`**: si la BD está vacía y existen los CSV de las fases anteriores, importa los registros (una sola vez).
- **Controllers**: solo cambió 1 línea en cada uno (`...DAOCsv.getInstancia()` → `...DAOSQLite.getInstancia()`). Las vistas no se tocaron.
- **Fix de singleton**: `MigradorDatos` recibe la conexión por parámetro (no puede llamar `getInstancia()` durante la construcción de `ConexionBD`, cuando la instancia aún no existe).
- **Verificación automatizada**: se compiló con `javac` + el jar del driver y se probó: creación de BD/tablas, migración CSV, guardado de factura con detalles y persistencia al reabrir (segunda ejecución en nueva JVM).

## Fase 10 — Publicación y control de versiones

Corresponde a la tarea "Publicación y control de versiones del proyecto": no agrega funcionalidad, documenta el uso de Git/GitHub del semestre.

- Un único repositorio público: `https://github.com/jzetinob/sistema-ventas` (rama `main`, 35 commits).
- El repositorio contiene: código fuente completo, estructura Maven de NetBeans (`pom.xml`, `nbactions.xml`), README índice, `docs/` y los entregables de cada tarea en `tareas/NN-nombre/`.
- `.gitignore` excluye `datos/` (base de datos local), `capturas/`, `target/` y `build/`.
- Documento de la tarea: `tareas/04-control-versiones/control-versiones.md` (fuente del PDF de entrega).

## Fase 11 — Implementación del diagrama de clases

Corresponde a la tarea 5 e implementa en código real el diagrama de clases del sistema:

- **modelo/Persona.java** nuevo: clase abstracta (id, nombre, nit, telefono) con metodo concreto mostrarInformacion(). Cliente ahora xtends Persona (heredad; NO sobreescribe) y Empleado (nuevo) xtends Persona y **sobreescribe** mostrarInformacion() (polimorfismo).
- **modelo/FacturaDetalle.java** nuevo: se convirtio la clase anidada en clase publica top-level con producto: Producto, cantidad, precioUnitario y calcularSubtotal() (composicion Factura → FacturaDetalle).
- **modelo/Factura.java** reescrito: idFactura, echa: LocalDate, cliente: Cliente, detalles: List<FacturaDetalle>, gregarDetalle() y calcularTotal(). Se conservaron metodos de compatibilidad (getNombreCliente, getNit, getDetallesFilas) para no romper vistas/DAOs.
- **modelo/Producto.java**: nuevo atributo xistencia + hayExistencia(cantidad): boolean.
- **BD**: tabla mpleados nueva (codigo_empleado UNIQUE) y columna xistencia en productos (migracion automatica con erificarColumnaExistencia() en ConexionBD). DAOs/Controller/vista JSON para Empleado (EmpleadoDAO, EmpleadoDAOSQLite, EmpleadoController, FrmEmpleados) y ProductoDAOSQLite/FrmProductos adaptados a existencia. Factura guarda cliente_id y resta existencia al guardar.
- **Vistas**: FrmPrincipal gana menu **Catálogos → Empleados** (miEmpleados); FrmFactura usa FacturaDetalle con Producto y valida hayExistencia().
- **Verificacion**: compilacion javac sin errores + prueba de humo automatizada OK (tablas, CRUD Empleado, hayExistencia, composicion Factura, menu). Commit Fase 11: ... en el historial.
