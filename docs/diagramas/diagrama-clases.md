# Diagrama de clases UML

El código del diagrama está en [`diagrama-clases.mmd`](diagrama-clases.mmd), en formato **Mermaid**, y las imágenes exportadas en `tareas/06-Elaboración del Diagrama de Clases del Proyecto/`.

> **Alcance:** el diagrama corresponde a la entrega de la **tarea 5/6** (Fase 11). Incluye `Persona` → `Cliente`/`Empleado`, `Producto`, `Factura` ◆— `FacturaDetalle`, y los DAO, controllers y vistas de esa etapa.
>
> Las clases agregadas después **todavía no están dibujadas**:
> - Modelo: `Categoria`, `Proveedor` (hereda de `Persona`), `Usuario`, `Compra` ◆— `CompraDetalle`.
> - DAO y controllers de esas entidades, más `CopiadorBD`, `Sesion`, `Contrasenas` y `RespaldoController`.
> - Vistas: `FrmCatalogo` (abstracta), `DlgLogin`, `FrmCompra`, `FrmListaCompras`, `FrmCategorias`, `FrmProveedores`, `FrmUsuarios`, `ReporteHtml`, `BarraBusqueda`, `FiltroTabla`.
>
> La descripción al día de todas las clases está en el [manual técnico](../manual-tecnico.md), sección 3.

## Cómo verlo o exportarlo

| Opción | Pasos |
|---|---|
| **Mermaid Live** (en línea) | Abrir https://mermaid.live/, pegar el contenido de `diagrama-clases.mmd` y usar *Download PNG / SVG / PDF* |
| **VS Code** | Instalar la extensión *Markdown Preview Mermaid Support* y abrir la vista previa (Ctrl+Shift+V) |
| **Línea de comandos** | `npx @mermaid-js/mermaid-cli -i diagrama-clases.mmd -o diagrama-clases.png` |
| **GitHub** | Los bloques Mermaid se dibujan solos al abrir el archivo en el repositorio |

## Qué incluye

| Grupo | Clases |
|---|---|
| **Modelo** | `Persona` (abstracta) → `Cliente`, `Empleado`; `Producto`; `Factura` ◆— `FacturaDetalle` |
| **DAO** (patrón DAO + Singleton) | Interfaces `FacturaDAO`, `ProductoDAO`, `ClienteDAO`, `EmpleadoDAO`, implementadas por las clases `…DAOSQLite` |
| **Controladores** | `FacturaController`, `ProductoController`, `ClienteController`, `EmpleadoController` |
| **Vistas** (MDI) | `FrmPrincipal` (contenedor), `FrmFactura`, `FrmListaFacturas`, `FrmDetalleFactura`, `FrmProductos`, `FrmClientes`, `FrmEmpleados`, `FrmVistaPreviaFactura`, `TicketFactura` (implementa `Printable`), `CampoBusqueda` |

**Relaciones:**

| Tipo | Relaciones |
|---|---|
| Herencia | `Persona` → `Cliente` / `Empleado` |
| Composición | `Factura` ◆— `FacturaDetalle` (1..*) |
| Asociación | `Factura` → `Cliente`, `FacturaDetalle` → `Producto` |
| Realización | Las implementaciones `..|>` sus interfaces DAO |
