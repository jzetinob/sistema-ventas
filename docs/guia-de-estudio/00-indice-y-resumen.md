# Guía de estudio del Sistema de Ventas — 00. Índice y resumen

**Curso:** Programación II · Universidad Mariano Gálvez
**Estudiante:** Josue Zetino · **Repositorio:** https://github.com/jzetinob/sistema-ventas
**Guía escrita el:** 2026-11-08 (refleja el proyecto hasta la fase 19 y la tarea 10)

Esta guía explica **todo el proyecto** con palabras sencillas, para estudiarlo y responder preguntas sin tener la computadora a mano. Está dividida en 8 archivos:

| Archivo | Tema |
|---|---|
| `00-indice-y-resumen.md` | Este archivo: resumen general, tecnologías, estructura y glosario |
| `01-app-de-escritorio.md` | La app de escritorio (Java Swing, MVC): clases, pantallas, flujos y seguridad |
| `02-base-de-datos.md` | Las tablas, relaciones, transacciones, los dos motores (SQLite y PostgreSQL) y el respaldo |
| `03-api-rest-y-supabase.md` | Qué es la API REST, cómo funciona en Supabase, cada endpoint y su seguridad |
| `04-app-android.md` | La app móvil: pantallas, cómo llama a la API, sesión, sin conexión y tema |
| `05-tareas-del-curso.md` | Qué pidió y qué se entregó en cada tarea (01 a 10) |
| `06-conceptos-y-decisiones.md` | Los temas del curso aplicados (POO, patrones…) y por qué se decidió cada cosa |
| `07-preguntas-de-repaso.md` | Preguntas que podría hacer el profesor, con su respuesta |

> **Para usarla en un Proyecto de Claude:** sube los 8 archivos al conocimiento del proyecto. Instrucciones sugeridas: *"Eres mi apoyo de estudio para el curso Programación II (UMG). Responde en español, con palabras sencillas, basándote en la guía del Sistema de Ventas. Si algo no está en la guía, dilo."*

---

## 1. El proyecto en un minuto

Es un **sistema de ventas** (facturación e inventario) hecho en **Java** durante el semestre. Tiene **tres piezas que comparten los mismos datos**:

1. **App de escritorio** (Java Swing): se factura, se administran catálogos (productos, clientes, empleados, categorías, proveedores y usuarios), se registran compras a proveedores, se ven reportes y se procesan las órdenes de venta.
2. **Base de datos**: PostgreSQL en la nube (**Supabase**). Si no se configura la nube, la app de escritorio usa una base local **SQLite**.
3. **App móvil Android** (Java): inicia sesión con los mismos usuarios, consulta productos y clientes, factura, crea **órdenes de venta** y muestra un resumen con gráficos. Se comunica con la base por una **API REST** (funciones de PostgreSQL publicadas por Supabase).

```
 App de escritorio (Java Swing, MVC) ──JDBC──────────────▶ Supabase (PostgreSQL 17)
   sistema-ventas/                                         13 tablas + sesiones_app
   (sin configuración: base local SQLite)                  RLS activo en todas
                                                                   ▲
 App móvil Android (Java) ──HTTPS POST /rest/v1/rpc/... (API REST)─┘
   movil/
```

**Lo que se registra en una pieza aparece en la otra:** una factura hecha en el celular aparece en *Ver facturas* del escritorio y descuenta la existencia; un cliente registrado en el celular aparece en el catálogo del escritorio; una orden de venta del celular aparece en *Archivo → Órdenes de Venta*.

## 2. Tecnologías y versiones

| Pieza | Tecnología |
|---|---|
| Escritorio | Java 25, Swing con ventana **MDI** (`JDesktopPane` + `JInternalFrame`), Maven, NetBeans |
| Apariencia | FlatLaf 3.7.2 (tema claro de 06:00 a 17:59 y oscuro de 18:00 a 05:59) |
| Base en la nube | PostgreSQL 17 en Supabase (plan Free, región us-east-1, proyecto `sistema-ventas`) |
| Base local | SQLite (archivo `datos/sistema_ventas.db`) |
| Drivers JDBC | `sqlite-jdbc 3.47.1.0` y `postgresql 42.7.4` |
| Contraseñas | bcrypt (librería jBCrypt 0.4) |
| API REST | Supabase (PostgREST): cada función `app_*` de PostgreSQL es un endpoint `POST /rest/v1/rpc/<función>` |
| Móvil | Android en Java; minSdk 24 (Android 7.0), compileSdk 37, targetSdk 34; Gradle 9.8, AGP 9.4.1. Única dependencia: SwipeRefreshLayout |
| Control de versiones | Git + GitHub (un repositorio público, rama `main`) |

## 3. Estructura del repositorio

```
sistema-ventas/   (repositorio)
├── sistema-ventas/     app de escritorio (proyecto Maven de NetBeans)
│   ├── src/main/java/com/josue/ventas/
│   │   ├── modelo/         entidades (Persona, Cliente, Producto, Factura…)
│   │   ├── dao/            acceso a datos (interfaces + implementación JDBC, ConexionBD)
│   │   ├── controlador/    lógica de negocio (un controller por entidad, Sesion, Contrasenas)
│   │   └── vista/          ventanas Swing (FrmPrincipal, FrmFactura…) y componentes
│   ├── src/main/resources/bd/esquema-postgresql.sql   tablas y funciones de la nube
│   └── config/bd.properties.ejemplo                    plantilla de conexión (sin contraseña)
├── api/                documentación de la API REST y script de prueba
├── movil/              app Android (proyecto Gradle de Android Studio)
├── docs/               manuales, diagramas, historial, estado y esta guía
├── tareas/             las 10 entregas del semestre (01 a 10)
└── entregables/        VentasMovil.apk y los manuales en PDF
```

## 4. Datos clave para recordar

| Dato | Valor |
|---|---|
| Tablas | **13** del sistema + `sesiones_app` (solo en la nube, para el móvil) |
| Fases de desarrollo | **19** (del CSV inicial a la tarea final) |
| Tareas del curso | **10** |
| Roles | **Administrador** (todo) y **Vendedor** (facturación, lista de facturas, órdenes de venta y clientes) |
| Números automáticos | Facturas `FAC-0001`, compras `COM-0001`, órdenes de venta `OV-0001` |
| Sesión del móvil | Token que vence a las **8 horas** |
| Inicio de sesión | Se cierra el sistema tras **3 intentos** fallidos |
| Estados de una orden | `PENDIENTE`, `CONFIRMADA`, `ANULADA` |
| Tipos de teléfono | `PERSONAL`, `TRABAJO`, `CASA`, `OTRO` |
| Supabase Free | Pausa el proyecto tras **7 días sin uso**; se reactiva con *Restore project* |

## 5. Glosario rápido

| Término | Qué significa en este proyecto |
|---|---|
| **MVC** | Modelo-Vista-Controlador: separar las ventanas (vista), la lógica (controlador) y los datos (modelo + DAO) |
| **DAO** | *Data Access Object*: la clase que habla con la base. Las vistas nunca escriben SQL |
| **JDBC** | La forma estándar de Java para conectarse a una base de datos (`Connection`, `PreparedStatement`, `ResultSet`) |
| **Singleton** | Una clase con una sola instancia para toda la app (por ejemplo `ConexionBD`) |
| **MDI** | Ventana principal que contiene ventanas internas (`JDesktopPane` con `JInternalFrame`) |
| **Transacción** | Varias operaciones que se guardan todas o ninguna (`commit` / `rollback`) |
| **Llave foránea (FK)** | Columna que apunta al `id` de otra tabla; su regla dice qué pasa al borrar (CASCADE, SET NULL, RESTRICT) |
| **API REST** | Servicio que recibe peticiones HTTP (aquí `POST` con JSON) y responde JSON |
| **Endpoint** | Una URL de la API que hace una tarea, por ejemplo `/rest/v1/rpc/app_registrar_orden_venta` |
| **JSON** | Formato de texto para enviar datos: `{"p_nit": "8888888-8", "p_detalles": [...]}` |
| **Supabase** | Servicio en la nube que da una base PostgreSQL y la publica como API REST |
| **PostgREST / RPC** | La parte de Supabase que convierte cada función de la base en un endpoint (`/rpc/` = llamar una función) |
| **RLS** | *Row Level Security*: seguridad de PostgreSQL que bloquea el acceso directo a las tablas desde la API |
| **Token** | Código temporal (UUID) que recibe el celular al iniciar sesión; se envía en cada petición |
| **apikey / clave publishable** | Clave pública que identifica la app ante Supabase; por sí sola no da acceso a los datos |
| **bcrypt** | Algoritmo para guardar contraseñas como *hash* (no se pueden leer) |
| **Idempotente** | Que se puede ejecutar varias veces sin duplicar nada (el script de la base) |
| **APK** | El instalador de una app Android |
| **ngrok** | Herramienta que publica temporalmente un servidor local en internet. **No se usó**: Supabase ya da una URL pública (el profesor lo aprobó) |
