# Sistema de Ventas

Sistema de facturación de escritorio en **Java Swing** con patrón **MVC**.

## Entregas del semestre

| # | Tarea | Carpeta | Archivos |
|---|---|---|---|
| 01 | Desarrollo del formulario de Facturación | [`tareas/01-desarrollo-formulario-facturacion/`](tareas/01-desarrollo-formulario-facturacion/) | PDF, PPTX, explicación |
| 02 | Investigación: Formulario contenedor (Menú MDI) | [`tareas/02-investigacion-menu-mdi/`](tareas/02-investigacion-menu-mdi/) | investigación (fuente), PPTX, PDF |
| 03 | Integración del proyecto con Base de Datos | [`tareas/03-integracion-base-de-datos/`](tareas/03-integracion-base-de-datos/) | documento (fuente), PPTX, PDF |
| 04 | Publicación y control de versiones del proyecto | [`tareas/04-control-versiones/`](tareas/04-control-versiones/) | documento (fuente), PPTX, PDF |
| 05 | Implementación del diagrama de clases | [`tareas/05-implementacion-diagrama-clases/`](tareas/05-implementacion-diagrama-clases/) | documento (fuente) |

> La lista se actualiza cada vez que se agrega una tarea. El código del proyecto vive siempre en [`sistema-ventas/`](sistema-ventas/) y evoluciona con cada tarea.

## ¿Qué hace?

- **Ventana principal** con menú clásico: Archivo, Catálogos, Edición, Ventana y Ayuda (en español). Es un **formulario contenedor MDI**: mantiene un `JDesktopPane` donde se abren los formularios como `JInternalFrame` (ventanas internas con cascada y mosaico desde el menú Ventana).
- **Facturación**: cliente, NIT, fecha automática, número de factura automático (FAC-0001, FAC-0002, ...), tabla de productos con subtotales y total, botones Agregar / Eliminar / Guardar / Imprimir.
- **Catálogos**: CRUD completo de productos (código, nombre, precio, existencia), clientes (NIT, nombre, dirección, teléfono) y empleados (código, NIT, teléfono, puesto), integrados con la factura mediante un **buscador con autocompletado** (filtra por código/NIT o nombre mientras escribes, sin depender del tamaño del catálogo).
- **Persistencia**: los datos se guardan en una base de datos **SQLite** (`datos/sistema_ventas.db`) mediante **JDBC** con el patrón DAO. Los registros permanecen al cerrar y volver a abrir la aplicación.
- **Impresión**: vista previa del ticket y envío a la impresora con el diálogo estándar de Windows (`java.awt.print`, sin librerías externas).
- **Validaciones**: NIT guatemalteco (8-13 dígitos), números de factura únicos, productos sin repetir, código/NIT únicos en catálogos.

## Requisitos

- JDK 25 (el proyecto compila con `maven.compiler.release 25`).
- NetBeans 22+ con soporte Maven.
- Internet (solo la primera vez): Maven descarga el driver JDBC de SQLite (`org.xerial:sqlite-jdbc`).
- Opcional: DB Browser for SQLite para ver la base de datos (`datos/sistema_ventas.db`).

## Cómo ejecutar

1. Clona el repositorio y ábrelo en NetBeans (proyecto Maven: `sistema-ventas`).
2. Ejecuta el proyecto (main class: `com.josue.ventas.SistemaVentas`) o corre `FrmPrincipal.java`.
3. Al primer arranque se crea la base de datos y sus tablas automáticamente; si existen los CSV de versiones anteriores, sus datos se migran a la BD.
4. Para ver los registros almacenados, abre `datos/sistema_ventas.db` con DB Browser for SQLite.

## Arquitectura (resumen)

Patrón **MVC** en 4 paquetes bajo `com.josue.ventas`:

```
┌───────────────┐      ┌──────────────────┐      ┌────────────────────┐
│  vista        │ ───► │  controlador     │ ───► │  dao               │
│  (JFrames)    │      │  (Controllers)   │      │  (DAO + SQLite)    │
└───────────────┘      └──────────────────┘      └────────┬───────────┘
                                                          │
                                               ┌───────────▼───────────┐
                                               │  modelo               │
                                               │  (Factura, Producto…) │
                                               └───────────────────────┘
```

- Relaciones: **asociación** (vista → controlador → dao), **agregación** (Factura contiene una lista de detalles) y **composición** (los detalles se crean y destruyen dentro de la Factura).
- Los DAO son **singleton**: `ClienteDAOSQLite`, `ProductoDAOSQLite`, `EmpleadoDAOSQLite` y `FacturaDAOSQLite` implementan las mismas interfaces y ejecutan JDBC contra SQLite. `ConexionBD` administra la conexión única y crea las tablas al arrancar.
- El modelo sigue el **diagrama de clases** (tarea 5): «abstract» `Persona` con herencia de `Cliente` y `Empleado`, asociación `Cliente 1..* Factura`, composición `Factura → FacturaDetalle` y asociación `FacturaDetalle ↔ Producto` (`Producto.hayExistencia()`).

Documentación detallada en [`docs/`](docs/).

## Historial de desarrollo

| Etapa | Qué se hizo |
|---|---|
| — | Formulario de facturación base + menú principal |
| — | Lista de facturas registradas |
| 1 | Persistencia CSV + número de factura automático |
| 2 | Lista completa: eliminar, ver detalle, actualizar |
| 3 | Catálogos de productos y clientes |
| 4 | Combos de catálogo en la factura |
| 5 | Impresión con vista previa |
| 6 | Validaciones |
| — | Fix: inicialización del singleton y correlativo |
| — | Buscador con autocompletado en la factura |
| — | Mejoras: correlativo sin quemar, formato de precios, logging del DAO |
| — | MDI: formulario contenedor con JDesktopPane y JInternalFrame |
| 9 | Base de datos: SQLite + JDBC (reemplaza el CSV) |
| 11 | Diagrama de clases: `Persona` abstracta, `Cliente`/`Empleado`, `FacturaDetalle`, catálogo de empleados, existencia de productos |

## Documentación

- [`docs/mini-tutorial.md`](docs/mini-tutorial.md) — tutorial paso a paso de cómo usar la app.
- [`docs/PLAN.md`](docs/PLAN.md) — plan de desarrollo (decisiones y fases).
- [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) — arquitectura detallada.
- Los `.md` futuros se agregan en `docs/`.

## Base de datos (SQLite)

Desde la tarea 3, el almacenamiento es una base de datos relacional SQLite:

- Driver `org.xerial:sqlite-jdbc` (JDBC) en el `pom.xml`.
- `dao/ConexionBD.java`: singleton que abre la conexión (`jdbc:sqlite:datos/sistema_ventas.db`) y crea las tablas si no existen.
- Tablas: `clientes`, `productos`, `facturas` y `factura_detalles` (con llave foránea y `ON DELETE CASCADE`).
- `dao/ClienteDAOSQLite.java`, `dao/ProductoDAOSQLite.java` y `dao/FacturaDAOSQLite.java`: implementan las mismas interfaces DAO que usaba el CSV, así que las vistas y controladores no cambiaron (solo una línea por controller).
- `dao/MigradorDatos.java`: importa a la BD los registros de los CSV de las tareas anteriores (una sola vez, si la BD está vacía).
- La factura y sus detalles se guardan en **transacción**: o se guarda completa o no se guarda nada.
- La base de datos vive en `datos/sistema_ventas.db` (carpeta ignorada por git).
