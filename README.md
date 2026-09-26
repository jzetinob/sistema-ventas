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
- **Inicio de sesión**: usuarios con rol **Administrador** o **Vendedor**. Las contraseñas se guardan cifradas (PBKDF2 con sal) y el sistema se cierra después de 3 intentos fallidos. El vendedor solo ve facturación y clientes.
- **Compras a proveedores** (maestro-detalle): al guardar una compra aumenta la existencia de los productos. Anular una compra revierte ese aumento.
- **Catálogos nuevos**: categorías de productos, proveedores y usuarios.
- **Búsquedas y reportes**: todos los catálogos y listas tienen un buscador y un botón **Reporte HTML**. El menú **Reportes** ofrece inventario, existencia baja, ventas por período y compras por proveedor. Los reportes se abren en el navegador, desde donde se pueden imprimir o guardar como PDF.
- **Respaldo**: *Administración → Respaldar base de datos* genera una copia completa en un archivo `.db`.
- **Base de datos en la nube (Supabase / PostgreSQL)**: la app de escritorio y la app móvil comparten los mismos datos. Si no hay configuración de nube, el sistema usa SQLite local igual que antes.
- **App móvil Android** ([`movil/`](movil/)): inicio de sesión con los mismos usuarios, consulta de productos con existencia, clientes y ventas por fecha, y registro de clientes desde el teléfono.

## Entregables

| Archivo | Qué es |
|---|---|
| [`entregables/VentasMovil.apk`](entregables/VentasMovil.apk) | App móvil lista para instalar en Android 7.0 o superior |
| [`entregables/manual-usuario.pdf`](entregables/manual-usuario.pdf) | Manual de usuario (escritorio y móvil) |
| [`entregables/manual-tecnico.pdf`](entregables/manual-tecnico.pdf) | Manual técnico (arquitectura, base de datos, seguridad, API, instalación) |
| Respaldo de la BD | Se genera desde *Administración → Respaldar base de datos* |

## Requisitos

- JDK 25 (el proyecto compila con `maven.compiler.release 25`).
- NetBeans 22+ con soporte Maven.
- Para la app móvil: Android Studio (SDK de Android) o solo instalar el APK de `entregables/`.
- Para la base en la nube: una cuenta de Supabase (plan Free) y `config/bd.properties`.
- Internet (solo la primera vez): Maven descarga el driver JDBC de SQLite (`org.xerial:sqlite-jdbc`).
- Opcional: DB Browser for SQLite para ver la base de datos (`datos/sistema_ventas.db`).

## Cómo ejecutar

1. Clona el repositorio y ábrelo en NetBeans (proyecto Maven: `sistema-ventas`).
2. Ejecuta el proyecto (main class: `com.josue.ventas.SistemaVentas`) o corre `FrmPrincipal.java`.
3. Al primer arranque se crea la base de datos y sus tablas automáticamente; si existen los CSV de versiones anteriores, sus datos se migran a la BD.
4. **Base en la nube**: si existe `sistema-ventas/config/bd.properties` (copia de `bd.properties.ejemplo` con los datos de Supabase), el sistema se conecta a la nube. Si no existe, usa SQLite local. Ese archivo tiene la contraseña de la base y **no se sube a GitHub**.
5. **Primer uso**: como no hay usuarios, el sistema pide crear el **administrador** (usuario y contraseña que tú eliges). Las siguientes veces se entra con ese usuario. Los demás usuarios se crean en *Administración → Usuarios*.
6. Para ver los registros de la base local, abre `datos/sistema_ventas.db` con DB Browser for SQLite. Los de la nube se ven en el *Table Editor* de Supabase.
7. **App móvil**: instala `entregables/VentasMovil.apk`, o abre `movil/` en Android Studio.

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
| 12 | 10 entidades en la BD (categorías, proveedores, usuarios, compras, detalle de compras), inicio de sesión con roles, compras que actualizan existencia, llaves foráneas activas |
| 13 | Buscador y reporte HTML en cada módulo, menú Reportes y respaldo de la base de datos |
| 14 | La factura descuenta la existencia al vender y la devuelve al eliminarse |
| 15 | Base de datos en la nube (Supabase/PostgreSQL), contraseñas bcrypt, API segura (RLS + funciones `app_*`) y app móvil Android |
| 16 | Manual de usuario y manual técnico (PDF) |

## Documentación

- [`docs/mini-tutorial.md`](docs/mini-tutorial.md) — tutorial paso a paso de cómo usar la app.
- [`docs/PLAN.md`](docs/PLAN.md) — plan de desarrollo (decisiones y fases).
- [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) — arquitectura detallada.
- [`docs/manual-usuario.md`](docs/manual-usuario.md) y [`docs/manual-tecnico.md`](docs/manual-tecnico.md) — fuentes de los manuales en PDF.
- [`docs/diagrama-entidad-relacion.md`](docs/diagrama-entidad-relacion.md) — diagrama entidad-relación de las 10 tablas y reglas de llaves foráneas.
- Los `.md` futuros se agregan en `docs/`.

## Base de datos (SQLite)

Desde la tarea 3, el almacenamiento es una base de datos relacional SQLite:

- Driver `org.xerial:sqlite-jdbc` (JDBC) en el `pom.xml`.
- `dao/ConexionBD.java`: singleton que abre la conexión (`jdbc:sqlite:datos/sistema_ventas.db`) y crea las tablas si no existen.
- Tablas (10): `clientes`, `productos`, `facturas`, `factura_detalles`, `empleados`, `categorias`, `proveedores`, `usuarios`, `compras` y `compra_detalles`. Las llaves foráneas están activas; ver [el diagrama entidad-relación](docs/diagrama-entidad-relacion.md).
- `dao/ClienteDAOSQLite.java`, `dao/ProductoDAOSQLite.java` y `dao/FacturaDAOSQLite.java`: implementan las mismas interfaces DAO que usaba el CSV, así que las vistas y controladores no cambiaron (solo una línea por controller).
- `dao/MigradorDatos.java`: importa a la BD los registros de los CSV de las tareas anteriores (una sola vez, si la BD está vacía).
- La factura y sus detalles se guardan en **transacción**: o se guarda completa o no se guarda nada.
- La base de datos vive en `datos/sistema_ventas.db` (carpeta ignorada por git).
