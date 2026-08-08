# Implementación del Diagrama de Clases

**Tarea 5 — Programación II**
**Estudiante:** Josue Zetino
**Módulo:** Sistema de Ventas (Java Swing + MVC + SQLite)

---

## 1. Objetivo

Trasladar el diagrama de clases del sistema de ventas a código Java real en el proyecto, manteniendo la arquitectura MVC, la base de datos SQLite y el formulario contenedor MDI de tareas anteriores. Cada clase, atributo, método y relación del diagrama queda implementado y **funcionando** en la aplicación.

## 2. Clases del diagrama y su implementación

| Clase (diagrama) | Tipo | Arquitectura en Java | Archivo |
|---|---|---|---|
| `Persona` | «abstract» | Clase abstracta con atributos `id`, `nombre`, `nit`, `telefono` y método concreto `mostrarInformacion()` | `modelo/Persona.java` |
| `Cliente` | herecia Persona | `extends Persona` + atributo `direccion` con `getDireccion()` | `modelo/Cliente.java` |
| `Empleado` | herecia Persona | `extends Persona` + atributos `codigoEmpleado`, `puesto`; **sobreescribe** `mostrarInformacion()` | `modelo/Empleado.java` |
| `Producto` | Clase | `idProducto`, `nombre`, `precio`, `existencia` y `hayExistencia(cantidad)` | `modelo/Producto.java` |
| `DetalleFactura` | Composición | `producto: Producto`, `cantidad`, `precioUnitario` y `calcularSubtotal()` | `modelo/FacturaDetalle.java` |
| `Factura` | Clase | `idFactura`, `fecha: LocalDate`, `cliente: Cliente`, `detalles: List<DetalleFactura>`, `agregarDetalle()`, `calcularTotal()` | `modelo/Factura.java` |

## 3. Relaciones del diagrama

- **Herencia (Persona → Cliente / Empleado)**: ambas clases usan `extends Persona`. `Empleado` redefine `mostrarInformacion()` (polimorfismo); `Cliente` conserva el método de la clase padre.
- **Asociación Cliente ↔ Factura (1..*)**: una factura tiene un solo `cliente: Cliente`; un cliente puede participar en muchas facturas. En la BD la asociación se recuerda mediante la columna `cliente_id` de `facturas`.
- **Composición Factura → DetalleFactura**: la lista de detalles pertenece exclusivamente a la factura: se agregan/eliminan solo a través de la factura (con el resto de `detalles` no se comparte referencia y mueren con ella). La tabla `factura_detalles` usa llave foránea con `ON DELETE CASCADE`.
- **Asociación DetalleFactura ↔ Producto**: cada detalle apunta a `producto: Producto`; el producto no le pertenece (puede aparecer en muchos detalles/facturas). La BD lo modela con `producto_id` en `factura_detalles`.

## 4. Cambios en la base de datos

- Nueva tabla `empleados` (`codigo_empleado` único): creada automáticamente en `ConexionBD` al arrancar.
- Nueva columna `existencia` en `productos`**: se agrega con `ALTER TABLE` automático sobre bases de datos creadas en tareas anteriores (migración sin perder datos).

## 5. Nueva funcionalidad visible

- **Catálogo de Empleados**: menú **Catálogos → Empleados** abre el formulario CRUD (`vista/FrmEmpleados.java`) con persistencia completa en SQLite (`dao/EmpleadoDAO` + `EmpleadoDAOSQLite`), validación de código único y tabla con botones Guardar / Actualizar / Eliminar / Limpiar.
- **Existencia de productos**: el CRUD de Productos ahora muestra y mantiene la columna **Existencia**; al agregar un producto a la factura se valida `hayExistencia()` y la cantidad se resta al guardar.

## 6. Capturas de la aplicación

> Capturas automáticas tomadas con la aplicación en ejecución: `capturas/01-menu-catalogos.png`, `capturas/02-frm-empleados.png`, `capturas/03-frm-productos.png` y `capturas/04-diagrama-uml.png` (carpeta local, excluida del repositorio como en tareas anteriores).

## 7. Verificación

- Compilación completa del proyecto con `javac` (JDK 25) **sin errores**.
- Prueba de humo automatizada: creación de tablas (incluye `empleados` y columna `existencia`), CRUD de `EmpleadoDAO`, `hayExistencia`, composición de `Factura` con `calcularTotal()` = 151.00, y apertura del menú **Catálogos → Empleados** → resultados todos `[OK]`.

## 8. Commits

- Implementación de modelo/DAO/vistas del diagrama de clases y commit+pull a `main` (ver historial de git en el repositorio público).

## 9. Conclusiones

El diagrama de clases no quedó como dibujo: cada elemento se programó, se persistió en SQLite y se usa desde la ventana principal (MDI), validando además el polimorfismo (`mostrarInformacion()`), la composición (`Factura → FacturaDetalle`) y la asociación `1..*` con el catálogo de empleados completo.