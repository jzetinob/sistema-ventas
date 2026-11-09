# Guía de estudio — 07. Preguntas de repaso

Preguntas que podría hacer el profesor, con una respuesta corta para decir con tus palabras.

## A. Arquitectura y escritorio

**1. ¿Qué arquitectura usa el proyecto?**
MVC: las ventanas están en `vista`, la lógica en `controlador`, las entidades en `modelo` y el acceso a la base en `dao`. Una operación va vista → controlador → DAO → base, y las vistas nunca escriben SQL.

**2. ¿Qué es un DAO y por qué usaste interfaces?**
Es la clase que habla con la base. Cada entidad tiene una interfaz (`FacturaDAO`) y una implementación (`FacturaDAOSQLite`). El controller usa la interfaz, así que cuando pasé de CSV a base de datos solo cambié una línea por controller.

**3. ¿Qué es un Singleton y dónde lo usaste?**
Una clase con una sola instancia: constructor privado y un método `getInstancia()`. Lo usan `ConexionBD` (una sola conexión), cada DAO, `Sesion` y `ApiSupabase` en Android.

**4. ¿Qué es MDI y cómo lo implementaste?**
Una ventana principal que contiene ventanas internas. `FrmPrincipal` tiene un `JDesktopPane` y cada formulario es un `JInternalFrame`. El menú *Ventana* las acomoda en cascada o mosaico, y si una ventana ya está abierta se trae al frente en lugar de abrir otra.

**5. ¿Cómo se ajusta el sistema al rol del usuario?**
`FrmPrincipal.aplicarSesion()` revisa `usuario.esAdministrador()` y oculta opciones. El Vendedor ve facturación, lista de facturas, órdenes de venta y clientes; no ve productos, empleados, categorías, proveedores, compras, administración ni reportes.

**6. ¿Cómo funciona el buscador de la factura?**
`CampoBusqueda` es un `JTextField` con una lista emergente que filtra por "contiene" mientras escribes; eliges con flechas y Enter. Reemplazó los combos porque con muchos productos eran incómodos.

**7. ¿Cómo imprimes la factura?**
`TicketFactura` implementa la interfaz `Printable` y dibuja el ticket con `Graphics2D`. La vista previa usa el mismo dibujo y el botón abre el diálogo de impresión de Windows.

**8. ¿Cómo haces los reportes?**
`ReporteHtml` genera un archivo HTML con los datos (escapados para seguridad) y lo abre en el navegador, donde se puede imprimir o guardar como PDF. Hay reportes de inventario, existencia baja, ventas por período, compras por proveedor y el resumen con gráficos.

**9. ¿Cómo dibujas los gráficos sin librerías?**
Las clases heredan de `JPanel` (escritorio) o `View` (Android) y sobrescriben `paintComponent()` u `onDraw()`. Calculo la altura de cada barra según el valor máximo y la dibujo con `fillRoundRect`; la dona usa arcos proporcionales.

## B. Base de datos

**10. ¿Qué base de datos usas?**
Dos: PostgreSQL en Supabase (nube, la comparten escritorio y celular) y SQLite local si no hay configuración. `ConexionBD` decide al arrancar leyendo `config/bd.properties`.

**11. ¿Cuántas tablas tiene y cuáles?**
13: categorias, productos, clientes, telefonos, empleados, proveedores, usuarios, facturas, factura_detalles, compras, compra_detalles, ordenes_venta y orden_venta_detalles. En la nube hay además `sesiones_app` para el celular.

**12. ¿Cómo te conectas desde Java?**
Con JDBC: `DriverManager.getConnection(url, usuario, clave)` y consultas con `PreparedStatement`. Para la nube el driver de PostgreSQL; para local el de SQLite.

**13. ¿Cómo evitas la inyección SQL?**
Todos los datos del usuario van como parámetros `?` en `PreparedStatement`, nunca pegados al texto del SQL.

**14. ¿Qué pasa si falla algo a la mitad de guardar una factura?**
Se guarda en una transacción: `setAutoCommit(false)`, inserto encabezado y detalles, descuento existencia y hago `commit`. Si algo falla, `rollback` y no queda nada a medias.

**15. ¿Qué pasa si dos vendedores venden el último producto al mismo tiempo?**
El descuento es `UPDATE … SET existencia = existencia - ? WHERE id = ? AND existencia >= ?`. El primero lo logra; al segundo el UPDATE no afecta filas, la factura se revierte y se le avisa. La existencia nunca queda negativa.

**16. ¿Cómo generas el número de factura?**
Busco el mayor número que existe en la tabla y le sumo uno (`FAC-0001`, `FAC-0002`…). No hay contador aparte, así que no se repite ni se pierden números. En la nube, las funciones bloquean la tabla para que dos peticiones a la vez no reciban el mismo número.

**17. Explica CASCADE, SET NULL y RESTRICT con ejemplos tuyos.**
CASCADE: al borrar una factura se borran sus detalles (y los teléfonos al borrar un cliente). SET NULL: al borrar una categoría, sus productos quedan sin categoría. RESTRICT: no se puede borrar un proveedor que tiene compras.

**18. ¿Por qué la factura guarda el nombre del cliente como texto?**
Es una "foto" de la venta: si después cambian o borran el cliente o el producto, la factura conserva los datos de ese día.

**19. ¿Cómo haces el respaldo?**
*Administración → Respaldar base de datos* genera un archivo `.db`. En local con `VACUUM INTO`; desde la nube, `CopiadorBD` copia todas las tablas a un SQLite.

**20. ¿Qué significa que el script de la base sea idempotente?**
Que se puede ejecutar muchas veces sin duplicar nada (`CREATE TABLE IF NOT EXISTS`, `CREATE OR REPLACE FUNCTION`). Por eso el escritorio lo ejecuta en cada arranque y así actualiza la nube.

## C. POO y diseño

**21. ¿Dónde usaste herencia y polimorfismo?**
`Persona` es abstracta; `Cliente`, `Empleado` y `Proveedor` heredan. `Empleado` y `Proveedor` sobrescriben `mostrarInformacion()`. También los gráficos sobrescriben `paintComponent()` / `onDraw()`.

**22. ¿Diferencia entre composición y agregación en tu proyecto?**
Composición: los detalles de una factura no existen sin la factura (se borran con ella, `CASCADE`). Agregación: un producto sigue existiendo aunque se borre su categoría (`SET NULL`).

**23. ¿Qué es el Template Method y dónde está?**
Una clase padre define los pasos y las hijas los completan. `FrmCatalogo` arma la pantalla y declara `guardar()`, `actualizar()`, `eliminar()`; `FrmCategorias`, `FrmProveedores` y `FrmUsuarios` los implementan.

**24. ¿Usaste enums?**
Sí: `Usuario.Rol` (ADMINISTRADOR, VENDEDOR), `Telefono.Tipo` (PERSONAL, TRABAJO, CASA, OTRO) y `OrdenVenta.Estado` (PENDIENTE, CONFIRMADA, ANULADA). La base repite la regla con un `CHECK`.

**25. ¿Qué excepciones manejas?**
`SQLException` en los DAO (con `rollback`), errores de lectura de archivos, y en Android `ApiException` (propia, con el código HTTP y el mensaje) y `JSONException`.

## D. Seguridad

**26. ¿Cómo guardas las contraseñas?**
Con bcrypt: se guarda un *hash*, nunca la contraseña. Los hash antiguos PBKDF2 se convierten a bcrypt al iniciar sesión.

**27. ¿Qué medidas tiene el inicio de sesión?**
Mismo mensaje si el usuario no existe o la clave es incorrecta, cierre tras 3 intentos y creación del administrador en el primer uso (no hay contraseña fija en el código). En el celular, el login espera 1 segundo si falla.

**28. ¿Dónde está la contraseña de la base? ¿Está en GitHub?**
Solo en `sistema-ventas/config/bd.properties`, que está en `.gitignore`. En GitHub solo está `bd.properties.ejemplo` sin datos.

## E. API REST y Supabase

**29. ¿Qué es una API REST?**
Un servicio al que se le hacen peticiones HTTP (aquí `POST` con JSON) y que responde JSON con un código (200 éxito, 400 dato inválido, 403 sin permiso).

**30. ¿Cuál es tu API REST?**
Supabase. Cada función de PostgreSQL (`app_login`, `app_registrar_orden_venta`…) es un endpoint `POST /rest/v1/rpc/<función>`. La documentación está en la carpeta `api/`.

**31. ¿Por qué no usaste ngrok?**
ngrok sirve para publicar un servidor que corre en la computadora. Mi API ya está publicada por Supabase con HTTPS y una URL fija, así que no hace falta servidor ni túnel. El profesor lo aprobó.

**32. ¿Qué es la apikey? ¿Es secreta?**
Es la clave publishable de Supabase que identifica la app. Es pública por diseño (va en el APK) y sola no permite leer ni escribir datos: las tablas tienen RLS y todo pasa por funciones que piden el token.

**33. ¿Qué es RLS?**
*Row Level Security* de PostgreSQL. Lo activé en todas las tablas sin políticas, así que nadie puede leerlas o modificarlas directo por la API (responde 401). Solo se puede usar las funciones `app_*`.

**34. ¿Qué es el token y cuánto dura?**
Un UUID que `app_login` crea en `sesiones_app` y devuelve al celular. Se envía en cada petición y la función `app_usuario_de` lo valida. Vence a las 8 horas; `app_logout` lo borra.

**35. ¿Qué valida la API al crear una orden de venta?**
Que el token sea válido, que el cliente exista (por NIT), que haya al menos un producto, sin repetidos, con cantidades de 1 a 100 000, que cada producto exista y que las observaciones no pasen de 500 caracteres. Si algo falla, no se guarda nada.

**36. ¿Qué responde la API?**
200 con `{numero_orden, cliente, total, estado}`; 400 con el motivo (por ejemplo, NIT no registrado); 403 si la sesión no es válida.

**37. ¿Por qué Supabase "se pausa"?**
El plan gratuito pausa el proyecto si pasa 7 días sin uso. No es una prueba que vence: se reactiva con *Restore project* sin perder datos. Por eso lo uso al menos una vez por semana y lo anoto en `docs/proyecto/estado.md`.

## F. Android

**38. ¿Cómo se comunica Android con la base?**
No se conecta directo: hace peticiones HTTPS a la API REST de Supabase con `HttpURLConnection`, envía JSON y recibe JSON. Las funciones de la base hacen el trabajo.

**39. ¿Por qué Android no usa JDBC como el escritorio?**
Porque la contraseña de la base quedaría dentro del APK y cualquiera podría sacarla. Con la API, el celular solo lleva una clave pública y el token.

**40. ¿Por qué las llamadas de red van en otro hilo?**
Android no permite red en el hilo de la pantalla (lanza `NetworkOnMainThreadException` y congelaría la app). `ApiSupabase` usa un `ExecutorService` para la petición y un `Handler` para devolver el resultado a la pantalla.

**41. ¿Qué pasa si no hay internet en el celular?**
La pantalla de Productos muestra la última copia guardada del catálogo, con un aviso. Vender, enviar órdenes y registrar clientes necesitan internet.

**42. ¿Cuál es la diferencia entre Nueva venta y Orden de venta?**
La venta crea una factura y descuenta existencia. La orden es un pedido: no descuenta existencia, queda PENDIENTE y en el escritorio se confirma o se anula.

**43. ¿Qué versión de Android necesita?**
Android 7.0 o superior (minSdk 24). Se probó en el emulador (Pixel 7, API 36) y en un teléfono real.

## G. Tarea final: explicar el flujo completo

**44. Explica el flujo de la orden de venta de principio a fin.**
1. En el celular elijo cliente y productos y toco *Enviar orden*.
2. `NuevaOrdenActivity` llama `ApiSupabase.registrarOrdenVenta`, que arma el JSON (token, NIT, observaciones, códigos y cantidades).
3. En otro hilo hace `POST` a `https://…supabase.co/rest/v1/rpc/app_registrar_orden_venta` con la apikey.
4. Supabase ejecuta la función `app_registrar_orden_venta`: valida el token y los datos, bloquea la tabla para el número, inserta la orden y sus detalles con el precio del catálogo, todo en una transacción.
5. Responde 200 con el número de orden, o 400/403 con el mensaje.
6. El celular muestra *Orden registrada* (OV-0001, total, PENDIENTE) o el error.
7. En el escritorio, *Archivo → Órdenes de Venta* la muestra y se puede confirmar o anular.

**45. ¿Cómo compruebas que la orden llegó a la base?**
En el escritorio, *Archivo → Órdenes de Venta*; con SQL: `SELECT * FROM ordenes_venta ORDER BY id DESC;`; o en el *Table Editor* y los *Logs* de Supabase.

**46. ¿Cuáles son los 3 repositorios?**
Un solo repositorio con tres carpetas: `sistema-ventas/` (escritorio), `api/` (API REST) y `movil/` (Android), cada una con su README.

## H. Proceso y herramientas

**47. ¿Cómo usaste Git?**
Un repositorio público en GitHub, rama `main`. Cada cambio con `git add`, `git commit -m "..."` y `git push`. El `.gitignore` excluye la base local, los compilados y el archivo con la contraseña.

**48. ¿Usaste inteligencia artificial?**
Sí, como apoyo. En la prueba práctica (tarea 9) se pedía usar un agente de IA y se usó Claude Code. Lo importante es que puedo explicar cada parte del código.

**49. ¿Qué mejorarías?**
Ideas posibles: convertir una orden confirmada en factura automáticamente, pruebas unitarias con JUnit, paginación en listas grandes, y notificaciones al escritorio cuando llega una orden.

**50. ¿Qué fue lo más difícil?**
*(Responde con tu experiencia.)* Ideas reales del proyecto: el correlativo que se "quemaba", pasar de combos a buscador, el orden de inicialización del Singleton, hacer que la misma app funcione con SQLite y PostgreSQL, y que el celular trabaje en otro hilo sin congelarse.
