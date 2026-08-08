# ESTADO del Proyecto

> Guardado el 2026-08-08. Archivo de referencia para retomar el trabajo sin perder contexto.

## Situación general

- **Proyecto:** Sistema de Ventas (Java Swing + MVC + SQLite), repo `https://github.com/jzetinob/sistema-ventas` (rama `main`).
- **Última tarea del semestre entregable:** Tarea 5 — Implementación del diagrama de clases (**terminada e implementada**).
- **Git:** árbol limpio; últimos commits: `9becdf7` (corrección menor) y `d4fd001` (Fase 11).

## Completado en la sesión previa (Tarea 5)

| Pieza | Archivos |
|---|---|
| `Persona` abstracta (id, nombre, nit, telefono; `mostrarInformacion()` concreto) | `modelo/Persona.java` |
| Herencia: `Cliente extends Persona` (no sobreescribe) | `modelo/Cliente.java` |
| Herencia: `Empleado extends Persona` (sobreescribe `mostrarInformacion()`) | `modelo/Empleado.java` |
| Composición: `FacturaDetalle` (producto, cantidad, precioUnitario, `calcularSubtotal()`) | `modelo/FacturaDetalle.java` |
| `Factura` reescrito (idFactura, fecha LocalDate, cliente Cliente, `List<FacturaDetalle>`, `agregarDetalle()`, `calcularTotal()` + alias de compatibilidad) | `modelo/Factura.java` |
| `Producto` + existencia + `hayExistencia()` | `modelo/Producto.java` |
| BD: tabla `empleados`; columna `existencia` en `productos` (migración `verificarColumnaExistencia()`) | `dao/ConexionBD.java` |
| DAO Empleado (singleton) | `dao/EmpleadoDAO.java`, `dao/EmpleadoDAOSQLite.java` |
| Controller Empleado | `controlador/EmpleadoController.java` |
| CRUD Empleados (menú **Catálogos → Empleados**) | `vista/FrmEmpleados.java`, `vista/FrmPrincipal.java` |
| Adaptaciones (LocalDate, getNombreCliente, existencia) | `dao/FacturaDAOSQLite.java`, `dao/FacturaDAOCsv.java`, `dao/ProductoDAOSQLite.java`, `vista/FrmFactura.java`, `vista/FrmProductos.java`, `vista/FrmListaFacturas.java`, `vista/FrmDetalleFactura.java`, `vista/TicketFactura.java` |

**Verificación:** compilación con `javac` sin errores; prueba de humo `[OK]` (tablas, CRUD Empleado, `hayExistencia`, Factura composición `calcularTotal()` = 151.00, menú Empleados). Clases compiladas y probadas en `C:\Users\Usuario\AppData\Local\Temp\opencode\sv-build`.

## Entregables de la tarea 5

- Documento fuente (lista para exportar): `tareas/05-implementacion-diagrama-clases/implementacion-diagrama-clases.md`
- **Capturas** (validadas con OCR) en `tareas/05-implementacion-diagrama-clases/capturas/`: `01-menu-catalogos.png`, `02-frm-empleados.png`, `03-frm-productos.png`, `04-diagrama-uml.png`
- Índices actualizados: `README.md` (tabla de tareas + etapa 11) y `docs/PLAN.md` (Fase 11 ✅ Completada)

## PENDIENTE — que hacer al retomar (lo único que falta)

1. **Generar PDF (y/o Word) de la tarea 5**: tomar `tareas/05-implementacion-diagrama-clases/implementacion-diagrama-clases.md` y **insertar las capturas** de `tareas/.../capturas/` (el `.gitignore` excluye la carpeta `capturas/`, por eso no están subidas; las capturas van dentro del PDF entregable, igual que en las tareas 2-4).
2. **Subir (opcional)**: el PDF/PPTX de la tarea 5 al repo como se hizo en tareas anteriores (`git add`, `git commit`, `git push` a `origin/main`).
3. No hay nada más pendiente del código: ya está implementado, compilado, probado y publicada.

## Notas útiles para retomar

- La BD local `datos/sistema_ventas.db` contiene datos demo (EMP-001, EMP-002, producto Teclado) usados para las capturas; está **gitignored**.
- Compilación manual (sin Maven en PATH): `javac -encoding UTF-8 -cp <ruta>/sqlite-jdbc-3.47.1.0.jar -d <salida> <todos los *.java>`; el jar está en `$env:USERPROFILE\.m2\repository\org\xerial\sqlite-jdbc\3.47.1.0\`.
- Scripts temporales fuera del repo: `C:\Users\Usuario\AppData\Local\Temp\opencode\smoke\` (`PruebaHumo.java`, `Capturas.java`, `Pinta.java`) y salida en `...\Temp\opencode\sv-build\`.
- Si algo no compila con Maven en NetBeans, recordar `maven.compiler.release 25` ya configurado.