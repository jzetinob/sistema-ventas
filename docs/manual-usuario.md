# Manual de Usuario — Sistema de Ventas

**Universidad Mariano Gálvez · Programación II**

Autor: Josue Zetino

Este manual explica cómo usar el sistema día a día. Tiene dos partes:

1. **Aplicación de escritorio** (Windows): para facturar, administrar catálogos, registrar compras y sacar reportes.
2. **Aplicación móvil** (Android): para ver el resumen con gráficos, **vender**, consultar productos, clientes y ventas, y registrar clientes desde el teléfono.

Las dos trabajan con **la misma base de datos en la nube**, así que lo que se registra en una aparece en la otra.

---

## Parte 1. Aplicación de escritorio

### 1.1 Iniciar sesión

Al abrir el sistema aparece la ventana **Iniciar sesión**.

- Escriba su **usuario** y **contraseña** y presione **Entrar**.
- Si se equivoca 3 veces seguidas, el sistema se cierra por seguridad.
- **Primer uso:** si todavía no existe ningún usuario, el sistema pide crear el **administrador** (usuario, nombre y contraseña dos veces). Guarde bien esa contraseña: es la única cuenta que puede crear a los demás usuarios.

Hay dos tipos de usuario:

| Rol | Puede usar |
|---|---|
| **Administrador** | Todo el sistema |
| **Vendedor** | Facturación, lista de facturas y catálogo de clientes |

La barra de título muestra quién inició sesión, su rol y si la base es **local** o **en la nube**.

### 1.2 Menú principal

| Menú | Opciones |
|---|---|
| **Archivo** | Nueva factura, Ver facturas, Cambiar mi contraseña, Cerrar sesión, Salir |
| **Catálogos** | Productos, Empleados, Clientes, Categorías, Proveedores |
| **Compras** | Nueva compra, Ver compras |
| **Edición** | Limpiar formulario (de la factura abierta) |
| **Ventana** | Cascada, Mosaico, Minimizar todo, Restaurar todo, **Tema** (Automático por hora, Claro, Oscuro) |
| **Administración** | Usuarios, Respaldar base de datos |
| **Reportes** | Resumen con gráficos, Inventario, Existencia baja, Ventas por período, Compras por proveedor |
| **Ayuda** | Acerca de |

Cada opción abre una ventana **dentro** de la ventana principal. Se pueden tener varias abiertas a la vez y ordenarlas con el menú **Ventana**.

**Tema claro y oscuro:** el sistema se ve en **tema claro de 06:00 a 17:59** y en **tema oscuro de 18:00 a 05:59**, según la hora de la computadora. Cambia solo, sin cerrar las ventanas abiertas. En *Ventana → Tema* se puede dejar fijo en **Claro** u **Oscuro**, o volver a **Automático (por hora)**. La elección se recuerda para la próxima vez.

### 1.3 Catálogos (productos, clientes, empleados, categorías, proveedores, usuarios)

Todos los catálogos funcionan igual:

1. **Agregar:** escriba los datos arriba y presione **Guardar**.
2. **Modificar:** haga clic en un registro de la tabla (sus datos suben al formulario), cambie lo necesario y presione **Actualizar**.
3. **Eliminar:** seleccione el registro y presione **Eliminar**. El sistema pide confirmación.
4. **Limpiar:** vacía el formulario para empezar de nuevo.
5. **Buscar:** escriba en el campo **Buscar** y la tabla muestra solo los registros que contienen ese texto en cualquier columna.
6. **Reporte HTML:** genera un reporte con lo que se ve en la tabla (respeta la búsqueda) y lo abre en el navegador. Desde ahí se puede **imprimir** o **guardar como PDF** (Ctrl + P → "Guardar como PDF").

Validaciones que hace el sistema:

- **NIT:** entre 8 y 13 dígitos (los guiones son opcionales). No se repiten NIT de clientes ni de proveedores.
- **Código de producto** y **código de empleado:** no se repiten.
- **Precio** mayor que 0 y **existencia** no negativa.
- No se puede eliminar un **producto** ni un **proveedor** que aparecen en compras registradas.
- Al eliminar una **categoría**, sus productos quedan "sin categoría".

**Productos:** además de código, nombre, precio y existencia, cada producto puede tener una **categoría**, que se elige de la lista (primero cree las categorías en *Catálogos → Categorías*).

**Usuarios** (solo administrador):

- La contraseña se escribe solo al **crear** el usuario (mínimo 6 caracteres).
- **Actualizar** cambia nombre, usuario, rol y estado, pero no la contraseña.
- **Cambiar contraseña:** seleccione el usuario, escriba la nueva contraseña en el campo *Contraseña* y presione ese botón.
- Desmarcar **Activo** impide que ese usuario entre, sin borrarlo.
- El sistema no deja eliminarse ni desactivarse a uno mismo, y siempre exige que quede al menos un administrador activo.

### 1.4 Facturación

*Archivo → Nueva factura* (o **Ctrl + N**).

1. El **número de factura** (FAC-0001, FAC-0002…) y la **fecha** se llenan solos.
2. **Cliente:** escriba parte del NIT o del nombre en el buscador y elija de la lista (flechas + Enter o clic). Se llenan el nombre y el NIT.
3. **Producto:** busque igual, escriba la **cantidad** (el precio se llena solo) y presione **Agregar**. Repita por cada producto.
4. Para quitar una línea, selecciónela y presione **Eliminar**.
5. Presione **Guardar**.

Al guardar, el sistema **descuenta la existencia** de cada producto. Si algún producto ya no tiene suficiente (por ejemplo, otro vendedor lo vendió mientras tanto), la factura **no se guarda** y aparece un aviso. **Imprimir** muestra la vista previa del ticket y permite enviarlo a la impresora.

*Archivo → Ver facturas* muestra las facturas registradas, con buscador y reporte. **Ver detalle** muestra los productos de la factura. **Eliminar** borra la factura y **devuelve** la existencia de sus productos.

### 1.5 Compras a proveedores

*Compras → Nueva compra* sirve para registrar la mercadería que llega de un proveedor:

1. Elija el **proveedor** en el buscador.
2. Por cada producto: elíjalo, escriba **cantidad** y **costo unitario** y presione **Agregar**.
   - El **costo unitario** es lo que se le **paga al proveedor** por cada unidad. No es lo mismo que el **precio de venta** del catálogo, que es lo que paga el cliente.
   - Al elegir el producto, a la derecha aparecen su precio de venta, su existencia y su último costo. El costo se llena solo con **el de la última compra** de ese producto; cámbielo si esta vez fue distinto. En la primera compra de un producto hay que escribirlo.
3. Presione **Guardar compra**. La existencia de cada producto **aumenta** en la cantidad comprada.

*Compras → Ver compras* lista las compras (con buscador y reporte). Al seleccionar una, abajo aparece su detalle. **Anular compra** la elimina y resta la existencia que había sumado. Si parte de esa mercadería ya se vendió, el sistema no permite anularla.

### 1.6 Reportes

En el menú **Reportes** (administrador):

| Reporte | Qué muestra |
|---|---|
| **Resumen con gráficos** | Tablero con las ventas de hoy, las facturas de hoy, las ventas del mes y los productos por agotarse. Incluye un **gráfico de barras** con las ventas de los últimos 7 días (hoy en naranja), un **gráfico de dona** con los 5 productos más vendidos del mes y la tabla de productos con existencia de 5 o menos. **Actualizar** vuelve a calcular todo |
| Inventario de productos | Todos los productos con su valor en inventario (precio × existencia) y el total |
| Productos con existencia baja | Los productos con existencia menor o igual al número que usted indique (por defecto 5) |
| Ventas por período | Facturas entre dos fechas y el total vendido |
| Compras por proveedor | Cuántas compras y cuánto se le compró a cada proveedor entre dos fechas |

Las fechas se escriben como **aaaa-mm-dd** (ejemplo: 2026-09-01). Los reportes se abren en el navegador y quedan guardados en la carpeta `reportes`.

### 1.7 Respaldo de la base de datos

*Administración → Respaldar base de datos* crea una copia completa en un archivo `.db` (se propone un nombre con la fecha). Se recomienda hacerlo al final de cada día y guardar el archivo en otra unidad o en la nube. El archivo se puede abrir con **DB Browser for SQLite**.

### 1.8 Cambiar mi contraseña y cerrar sesión

- *Archivo → Cambiar mi contraseña:* pide la contraseña actual y la nueva dos veces.
- *Archivo → Cerrar sesión:* vuelve a la ventana de inicio de sesión (por ejemplo, para cambiar de usuario).

---

## Parte 2. Aplicación móvil (Android)

### 2.1 Instalación

Instale el archivo **`VentasMovil.apk`** en el teléfono (Android 7.0 o superior). Si el teléfono lo pide, permita "instalar apps de origen desconocido" para el administrador de archivos. Se necesita **conexión a internet**.

### 2.2 Iniciar sesión

Use **el mismo usuario y contraseña** de la aplicación de escritorio. La sesión dura 8 horas; al vencer, la app pide iniciar sesión de nuevo.

> Si un usuario fue creado antes de la versión con base en la nube y no puede entrar desde el teléfono, basta con que inicie sesión una vez en la aplicación de escritorio.

### 2.3 Pantalla de inicio

Arriba aparecen tres **indicadores del día**: ventas de hoy, facturas de hoy y productos por agotarse (en rojo si hay alguno). Al tocarlos se abre el **Resumen**. Abajo está el menú en botones de colores, y al final, **Cerrar sesión**.

| Botón | Para qué sirve |
|---|---|
| **Resumen** | Tablero con gráficos: ventas de hoy y del mes, **barras** con las ventas de los últimos 7 días (hoy en otro color), **dona** con los 5 productos más vendidos del mes y lista de productos por agotarse. |
| **Nueva venta** | Facturar desde el teléfono (ver 2.4). |
| **Productos** | Buscar por código o nombre; cada tarjeta muestra el precio, la categoría y la existencia. Los productos con existencia de 5 o menos se marcan en rojo. |
| **Clientes** | Buscar clientes por NIT o nombre (escriba y presione la lupa del teclado). |
| **Nuevo cliente** | Registrar un cliente (NIT de 8 a 13 dígitos y nombre obligatorios). Queda disponible de inmediato en la aplicación de escritorio. |
| **Ventas** | Elegir fecha inicial y final; muestra las facturas del período y el total. **Toque una factura para ver sus productos.** |

En todas las listas se puede **deslizar hacia abajo para actualizar**.

### 2.4 Nueva venta (facturar desde el teléfono)

1. **Cliente:** toque **Del catálogo** para buscarlo y elegirlo, o escriba el NIT y el nombre.
2. **Productos:** toque **Agregar**, busque el producto, tóquelo y escriba la cantidad. La app muestra cuánto hay disponible y no deja pedir más. Si agrega un producto que ya estaba, se cambia su cantidad. Para quitar un producto, **manténgalo presionado**.
3. Revise el **total** y toque **Guardar venta**, y luego confirme.

La factura recibe el siguiente número (FAC-…), descuenta la existencia y aparece de inmediato en la aplicación de escritorio y en los reportes. El precio lo toma el sistema del catálogo. Si otro vendedor agotó un producto mientras tanto, la venta **no se guarda** y se muestra el aviso.

### 2.5 Sin conexión y modo oscuro

- **Sin internet:** la pantalla **Productos** muestra la última copia del catálogo guardada en el teléfono, con un aviso rojo y la fecha de esa copia. Vender, registrar clientes y ver el resumen sí necesitan internet. La copia se borra al cerrar sesión.
- **Tema según la hora:** la app se ve **clara de 06:00 a 17:59** y **oscura de 18:00 a 05:59**, según la hora del teléfono. Cambia sola, incluso con la app abierta. Es el mismo horario de la aplicación de escritorio.

---

## Problemas frecuentes

| Problema | Solución |
|---|---|
| "No se pudo conectar a la base de datos en la nube" | Revise la conexión a internet. Si el proyecto de Supabase estuvo una semana sin uso, se pausa: entre a supabase.com y presione **Restore project**. |
| "Usuario o contraseña incorrectos" | Revise mayúsculas y minúsculas de la contraseña. El administrador puede asignarle una nueva en *Administración → Usuarios*. |
| "El usuario está desactivado" | Pida al administrador que marque **Activo** en su usuario. |
| La factura no se guarda por existencia | Revise la existencia en *Catálogos → Productos* o registre una compra. |
| No aparece un menú | Su usuario es **Vendedor**; esas opciones son solo para el administrador. |
