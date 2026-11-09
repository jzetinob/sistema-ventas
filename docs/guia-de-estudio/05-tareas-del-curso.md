# Guía de estudio — 05. Las tareas del curso

Cada tarea está en `tareas/NN-nombre/` del repositorio, con su documento y, cuando aplica, presentación y PDF.

| # | Tarea | Qué se entregó |
|---|---|---|
| 01 | Formulario de Facturación | `FrmFactura` en Swing con MVC |
| 02 | Investigación: Menú MDI | Documento y presentación sobre MDI |
| 03 | Integración con Base de Datos | Paso de CSV a SQLite con JDBC |
| 04 | Control de versiones | Repositorio en GitHub y su historial |
| 05 | Implementación del diagrama de clases | Herencia, composición y Empleado en código |
| 06 | Elaboración del diagrama de clases | 5 imágenes del diagrama del proyecto |
| 07 | Revisión presencial de avance | Presentación de 10 diapositivas con la rúbrica |
| 08 | Revisión presencial: Guía Didáctica | Cumplimiento punto por punto con capturas |
| 09 | Prueba práctica: cambio en caliente | Varios teléfonos por cliente |
| 10 | Tarea final: Android + API REST | Orden de Venta desde Android |

---

## Tarea 01 — Desarrollo del formulario de Facturación

- **Qué se pidió:** el formulario de facturación del sistema en Java Swing.
- **Qué se hizo:** `FrmFactura` con cliente (nombre y NIT), fecha automática, número automático `FAC-0001`, tabla de productos (cantidad, precio, subtotal), total calculado y botones Agregar, Eliminar y Guardar, más Imprimir con vista previa. Catálogos de productos y clientes. Buscador con autocompletado. Datos guardados en **CSV**. Validaciones de NIT, número único y productos no repetidos.
- **Organización:** MVC en 4 paquetes; la factura **compone** sus detalles.
- **Dificultades:** los combos eran incómodos y se cambiaron por un buscador; el correlativo se "quemaba" al abrir y cerrar sin guardar; los `;` en los datos rompían el CSV y hubo que escaparlos; ajustar la versión de Java al JDK instalado.

## Tarea 02 — Investigación: Formulario contenedor (Menú MDI)

- **Temas:** qué es un formulario MDI, `JDesktopPane`, `JInternalFrame`, `JMenuBar`, cómo abrir formularios hijos desde un menú y las ventajas de esta arquitectura.
- **Aplicación (fase 8):** `FrmPrincipal` pasó a ser el contenedor MDI con el menú **Ventana** (Cascada, Mosaico, Minimizar todo, Restaurar todo), y todos los formularios pasaron de `JFrame` a `JInternalFrame`.

## Tarea 03 — Integración del proyecto con Base de Datos

- **Qué se pidió:** reemplazar el almacenamiento en memoria/CSV por una base de datos relacional con JDBC.
- **Qué se hizo:** **SQLite** (no necesita servidor; un solo archivo). `ConexionBD` (Singleton) con `DriverManager.getConnection("jdbc:sqlite:datos/sistema_ventas.db")` y 4 tablas: `clientes`, `productos`, `facturas`, `factura_detalles` (FK con `ON DELETE CASCADE`). DAO con `PreparedStatement`, factura en **transacción**, `MigradorDatos` para los CSV.
- **Idea clave:** como los DAO implementan la misma interfaz, en cada controller solo cambió **una línea** (`FacturaDAOCsv` → `FacturaDAOSQLite`). Las vistas no se tocaron.
- **Verificación:** cerrar y volver a abrir el programa: los datos siguen y la numeración continúa.

## Tarea 04 — Publicación y control de versiones

- Un repositorio público en GitHub (`jzetinob/sistema-ventas`, rama `main`) con el código, la estructura Maven, el README y los entregables de cada tarea.
- `.gitignore` excluye la base local (`datos/`), `target/`, `build/` y, más adelante, `config/bd.properties` (la contraseña).
- Se documentó el historial de commits y el trabajo del semestre.

## Tarea 05 — Implementación del diagrama de clases

- **Herencia:** `Persona` abstracta → `Cliente` (hereda) y `Empleado` (sobrescribe `mostrarInformacion()`: polimorfismo).
- **Composición:** `Factura` → `FacturaDetalle` (clase pública con `producto`, `cantidad`, `precioUnitario`, `calcularSubtotal()`); en la base, `ON DELETE CASCADE`.
- **Asociación:** `FacturaDetalle` → `Producto` (el producto no pertenece al detalle).
- **Nuevo:** catálogo de **Empleados** (CRUD completo) y **existencia** en productos con `hayExistencia()`; la factura la valida y la resta.

## Tarea 06 — Elaboración del diagrama de clases del proyecto

- 5 imágenes del diagrama de clases del proyecto. Hoy el diagrama está dividido en 5 vistas por capa: modelo, acceso a datos, controladores, vistas y app móvil (`docs/diagramas/`).

## Tarea 07 — Revisión presencial de avance

- Presentación de 10 diapositivas (con notas y guion de demostración) siguiendo la rúbrica: diagrama de clases, diagrama entidad-relación, MDI y menú, formularios y operaciones, flujo funcional e integración.
- **Flujo de integración mostrado:** Categorías → Productos; Proveedores + Productos → Compra (suma existencia); Clientes + Productos → Factura (descuenta existencia); Facturas y Compras → Reportes; todo → App móvil.

## Tarea 08 — Revisión presencial: cumplimiento de la Guía Didáctica

- Se revisó la Guía Didáctica del curso punto por punto: diseño modular, altas/bajas/cambios, búsquedas, reportes, al menos 10 entidades, app móvil, Java, manuales, código fuente y backup.
- Tabla de los temas del curso aplicados (POO, herencia, composición, clases abstractas, polimorfismo, excepciones, listas, SQL, código seguro, patrones, desarrollo móvil).
- Capturas reales: escritorio (17 de octubre) y móvil en el emulador (24 de octubre).

## Tarea 09 — Prueba práctica en clase: cambio en caliente

- **Requerimiento:** un cliente puede tener **uno o varios teléfonos** (número y tipo: Personal, Trabajo, Casa u Otro), **sin campos fijos** `telefono1`, `telefono2`, `telefono3`.
- **Solución:** tabla `telefonos` (relación 1 a N con `ON DELETE CASCADE` y `CHECK` del tipo); `Telefono` con enum `Tipo` y `List<Telefono>` en `Cliente`; `TelefonoDAO` + `TelefonoDAOSQLite` (guarda la lista en una transacción); validaciones en `ClienteController` (8 a 15 dígitos, sin repetir); `PanelTelefonos` en `FrmClientes` (Agregar, Modificar, Quitar).
- **Decisión:** conservar `clientes.telefono` como teléfono principal para que los reportes y la app móvil siguieran funcionando. Se migraron los teléfonos de 28 clientes.
- Se hizo con apoyo de un agente de IA (Claude Code), como pedía la prueba.

## Tarea 10 — Tarea final: integración Android + API REST

- **Requerimiento:** la app Android crea una **Orden de Venta** con un `POST` a una API REST, que valida, la guarda en la base y responde éxito o error; la app muestra el resultado. Entregar 3 repositorios y hacer una demostración.
- **Solución:**
  - **API REST:** Supabase (aprobado por el docente en lugar de ngrok), endpoint `app_registrar_orden_venta`. Documentada en `api/README.md`.
  - **Base:** tablas `ordenes_venta` y `orden_venta_detalles`.
  - **Android:** `NuevaOrdenActivity` + `ApiSupabase.registrarOrdenVenta`.
  - **Escritorio:** `FrmOrdenesVenta` para ver, confirmar y anular.
- **Los 3 enlaces:**
  - Escritorio: https://github.com/jzetinob/sistema-ventas/tree/main/sistema-ventas
  - API REST: https://github.com/jzetinob/sistema-ventas/tree/main/api
  - Android: https://github.com/jzetinob/sistema-ventas/tree/main/movil
- **Evidencia:** orden OV-0001 (Q90.00) desde el emulador, error de NIT inexistente, la orden en el escritorio y en la base, y capturas del panel y los registros de Supabase. Después se instaló el APK en un teléfono real (6 de noviembre).
- **Criterios (8 puntos):** API REST 2.0, Android 2.0, integración 1.5, escritorio 0.5, repositorios 1.0, demostración 1.0.

## Fases de desarrollo (resumen)

| Fases | Qué se hizo |
|---|---|
| 1–7 | Persistencia CSV, lista de facturas, catálogos, buscador, impresión, validaciones, pruebas |
| 8 | MDI |
| 9 | SQLite con JDBC |
| 10 | GitHub |
| 11 | Diagrama de clases en código |
| 12 | 10 entidades, inicio de sesión con roles y compras |
| 13 | Búsquedas, reportes HTML y respaldo |
| 14 | La factura descuenta existencia |
| 15 | Base en la nube (Supabase) y app móvil |
| 16 | Manuales |
| 17 | Gráficos, factura desde el móvil, modo oscuro, sin conexión |
| 18 | Varios teléfonos por cliente (tarea 9) |
| 19 | Orden de venta desde Android por la API REST (tarea 10) |
