# Sistema de Ventas

Sistema de facturación e inventario en **Java**, desarrollado durante el curso **Programación II** (Universidad Mariano Gálvez). Tiene tres piezas que comparten los mismos datos:

| Pieza | Tecnología | Carpeta |
|---|---|---|
| **App de escritorio** | Java 25 · Swing (MDI) · patrón MVC · Maven / NetBeans | [`sistema-ventas/`](sistema-ventas/) |
| **Base de datos** | PostgreSQL en la nube (Supabase) · o SQLite local si no se configura la nube | [`sistema-ventas/src/main/resources/bd/`](sistema-ventas/src/main/resources/bd/) |
| **App móvil** | Android (Java) · API REST segura de Supabase | [`movil/`](movil/) |

## Entregables del proyecto final

| Archivo | Qué es |
|---|---|
| [`entregables/VentasMovil.apk`](entregables/VentasMovil.apk) | App móvil para Android 7.0 o superior |
| [`entregables/manual-usuario.pdf`](entregables/manual-usuario.pdf) | Manual de usuario (escritorio y móvil) |
| [`entregables/manual-tecnico.pdf`](entregables/manual-tecnico.pdf) | Manual técnico: arquitectura, base de datos, seguridad, API e instalación |
| Respaldo de la base de datos | Se genera desde *Administración → Respaldar base de datos* |

## Entregas del semestre

| # | Tarea | Carpeta |
|---|---|---|
| 01 | Desarrollo del formulario de Facturación | [`tareas/01-desarrollo-formulario-facturacion/`](tareas/01-desarrollo-formulario-facturacion/) |
| 02 | Investigación: Formulario contenedor (Menú MDI) | [`tareas/02-investigacion-menu-mdi/`](tareas/02-investigacion-menu-mdi/) |
| 03 | Integración del proyecto con Base de Datos | [`tareas/03-integracion-base-de-datos/`](tareas/03-integracion-base-de-datos/) |
| 04 | Publicación y control de versiones del proyecto | [`tareas/04-control-versiones/`](tareas/04-control-versiones/) |
| 05 | Implementación del diagrama de clases | [`tareas/05-implementacion-diagrama-clases/`](tareas/05-implementacion-diagrama-clases/) |
| 06 | Elaboración del diagrama de clases del proyecto | [`tareas/06-Elaboración del Diagrama de Clases del Proyecto/`](<tareas/06-Elaboración del Diagrama de Clases del Proyecto/>) |

Cada carpeta tiene el documento fuente (`.md`) y, cuando corresponde, la presentación (`.pptx`) y el PDF que se entregó.

## Qué hace

| Área | Funciones |
|---|---|
| **Facturación** | Número automático (FAC-0001…) y buscador de cliente y producto con autocompletado. Descuenta la existencia al guardar y no deja vender si no alcanza. Ticket con vista previa e impresión |
| **Catálogos** | Productos (con categoría), clientes, empleados, categorías, proveedores y usuarios. En todos: altas, bajas, cambios, búsqueda y **Reporte HTML** |
| **Compras** | Compras a proveedores: suman la existencia y proponen el último costo; se pueden anular |
| **Reportes** | Inventario valorizado, existencia baja, ventas por período y compras por proveedor (HTML; desde el navegador se imprimen o se guardan como PDF) |
| **Seguridad** | Inicio de sesión con roles (Administrador / Vendedor), contraseñas bcrypt, bloqueo tras 3 intentos y `PreparedStatement` contra inyección SQL. En la nube: RLS en todas las tablas y funciones con token para el móvil |
| **Datos** | 10 tablas con llaves primarias y foráneas, operaciones en transacción y respaldo a un archivo `.db` |
| **Móvil** | Mismos usuarios. Consulta de productos y existencia, clientes y ventas por fecha. Alta de clientes |

## Cómo ejecutar

1. Abre `sistema-ventas/` en **NetBeans** (proyecto Maven) y ejecútalo (F6). Requiere JDK 25.
2. **Modo nube:** si existe `sistema-ventas/config/bd.properties` (copia de `bd.properties.ejemplo` con los datos de Supabase), se conecta a la nube. **Modo local:** sin ese archivo, usa SQLite (`datos/sistema_ventas.db`). El archivo tiene la contraseña de la base, y por eso no se sube a GitHub.
3. En el primer uso, el sistema pide crear el **administrador**.
4. **Móvil:** instala `entregables/VentasMovil.apk`, o abre `movil/` en Android Studio.

## Documentación

Todo está en [`docs/`](docs/), con un [índice](docs/README.md) que dice qué leer según lo que busques:

- [Manual de usuario](docs/manual-usuario.md) · [Manual técnico](docs/manual-tecnico.md)
- [Diagrama entidad-relación](docs/diagramas/diagrama-entidad-relacion.md) · [Diagrama de clases](docs/diagramas/diagrama-clases.md)
- [Historial de desarrollo](docs/proyecto/historial-de-desarrollo.md): qué se hizo en cada una de las 16 fases y por qué.
- [Estado del proyecto](docs/proyecto/estado.md): dónde estamos y qué falta.

## Estructura del repositorio

```
├── sistema-ventas/     código de la app de escritorio (Maven)
├── movil/              código de la app Android (Gradle)
├── docs/               documentación (manuales, diagramas, historial, estado)
├── tareas/             entregas del semestre (01 a 06)
├── entregables/        APK y manuales en PDF del proyecto final
└── notas/              notas personales (no se suben a GitHub)
```
