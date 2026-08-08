# Prompt para generar la presentación (PPTX) o documento (Word/PDF) de la Tarea 5

> Copia y pega este archivo completo en la otra IA (ChatGPT/Gemini/Claude, etc.) junto con las capturas de la carpeta `capturas/`. El texto entre «» es todo lo que debe recibir la IA.

---

«

## Rol

Eres un asistente experto en documentación académica y presentaciones profesionales en español. Vas a crear la entrega de la **Tarea 5 — Programación II: "Implementación del Diagrama de Clases"** del proyecto **Sistema de Ventas** (Java Swing + patrón MVC + SQLite).

## Formato pedido

Genera una **presentación de PowerPoint (PPTX)** de 10-12 diapositivas (o un **documento Word** con estructura de informe si el usuario lo prefiere) con diseño limpio y profesional, en español, apto para ser entregado a un docente universitario. Título de la portada: **"Tarea 5 — Implementación del Diagrama de Clases"**.

## Contexto del proyecto

Sistema de facturación de escritorio desarrollado en Java Swing con patrón MVC, base de datos SQLite (JDBC) y formulario contenedor MDI (JDesktopPane + JInternalFrame). En esta tarea se trasladó el diagrama de clases UML del sistema a código Java real.

## Contenido que debe incluir

### 1. Portada
Título de la tarea, nombre del curso (Programación II), nombre del estudiante (Josue Zetino), fecha (agosto 2026).

### 2. Objetivo
Trasladar el diagrama de clases del sistema de ventas a código Java, manteniendo arquitectura MVC, BD SQLite y formulario MDI. Cada clase, atributo, método y relación del diagrama quedó implementado y funcionando.

### 3. Clases implementadas (tabla comparativa diagrama → Java)

| Clase (diagrama) | Tipo | Implementación Java | Archivo |
|---|---|---|---|
| `Persona` | «abstract» | Clase abstracta: `id`, `nombre`, `nit`, `telefono`, método concreto `mostrarInformacion()` | `modelo/Persona.java` |
| `Cliente` | herencia de Persona | `extends Persona` + `direccion` con `getDireccion()` | `modelo/Cliente.java` |
| `Empleado` | herencia de Persona | `extends Persona` + `codigoEmpleado`, `puesto`; **sobreescribe** `mostrarInformacion()` | `modelo/Empleado.java` |
| `Producto` | Clase | `idProducto`, `nombre`, `precio`, `existencia` y `hayExistencia(cantidad)` | `modelo/Producto.java` |
| `DetalleFactura` | Composición | `producto: Producto`, `cantidad`, `precioUnitario`, `calcularSubtotal()` | `modelo/FacturaDetalle.java` |
| `Factura` | Clase | `idFactura`, `fecha: LocalDate`, `cliente: Cliente`, `detalles: List<DetalleFactura>`, `agregarDetalle()`, `calcularTotal()` | `modelo/Factura.java` |

### 4. Relaciones del diagrama
- **Herencia** (Persona → Cliente / Empleado): `extends`; `Empleado` redefine `mostrarInformacion()` (polimorfismo), `Cliente` conserva el método padre.
- **Asociación Cliente ↔ Factura (1..*)**: una factura tiene un solo `cliente: Cliente`; un cliente participa en muchas facturas (en BD: columna `cliente_id` de `facturas`).
- **Composición Factura → DetalleFactura**: la lista de detalles pertenece exclusivamente a la factura (se agregan/eliminan solo a través de ella; en BD `factura_detalles` con `ON DELETE CASCADE`).
- **Asociación DetalleFactura ↔ Producto**: cada detalle apunta a `producto: Producto`; el producto puede aparecer en muchos detalles (columna `producto_id`).

### 5. Cambios en la base de datos
- Nueva tabla `empleados` (código único), creada automáticamente en `ConexionBD` al arrancar.
- Nueva columna `existencia` en `productos`, agregada con `ALTER TABLE` automático sobre BD creadas en tareas anteriores (migración sin pérdida de datos).

### 6. Funcionalidad nueva visible en la aplicación
- **Catálogo de Empleados**: menú **Catálogos → Empleados** → CRUD completo con persistencia SQLite, validación de código único (botones Guardar / Actualizar / Eliminar / Limpiar).
- **Existencia de productos**: columna Existencia en el CRUD de Productos; al agregar un producto a la factura se valida `hayExistencia()`.

### 7. Capturas (insertar imágenes en el orden dado, con sus pies)
Coloca aquí las imágenes que te adjunto (carpeta `capturas/`) con estos pies:
1. `01-menu-catalogos.png` — Menú principal con **Catálogos → Empleados**.
2. `02-frm-empleados.png` — Catálogo de Empleados con registros.
3. `03-frm-productos.png` — Catálogo de Productos con columna **Existencia**.
4. `04-diagrama-uml.png` — Diagrama de clases del sistema.
5. `05-frm-factura.png` — Formulario de Facturación.
6. `06-lista-facturas.png` — Lista de facturas registradas.
7. `07-detalle-factura.png` — Detalle de una factura (productos y total).
8. `08-ticket.png` — Vista previa del ticket.

### 8. Verificación
- Compilación completa del proyecto con `javac` (JDK 25) sin errores.
- Prueba de humo automatizada: creación de tablas (incluye `empleados` y columna `existencia`), CRUD de `EmpleadoDAO`, `hayExistencia()`, composición de `Factura` con `calcularTotal()` = 151.00, apertura del menú **Catálogos → Empleados** → todos los resultados `[OK]`.

### 9. Conclusiones
El diagrama de clases no quedó como dibujo: cada elemento se programó, se persistió en SQLite y se usa desde la ventana principal (MDI), validando polimorfismo (`mostrarInformacion()`), composición (`Factura → FacturaDetalle`) y asociación `1..*`.

## Instrucciones de estilo

- Usa un tema sobrio (blanco/gris oscuro o azul), títulos claros, tablas legibles.
- No inventes información que no esté en este prompt.
- Si es PPTX: 10-12 diapositivas, una idea por diapositiva, mínimo texto.
- Si es Word: portada, índice, secciones numeradas, conclusión.

»

---

**Notas para el usuario:**
- Adjunta a la conversación las imágenes de la carpeta `capturas/` (son 8 PNG) o pégales la ruta local si la IA puede leer archivos.
- Si la IA genera PPTX, puedes convertir después a PDF desde PowerPoint.
