# Diagrama de Clases UML - Sistema de Ventas

Este archivo contiene el diagrama de clases completo en formato **Mermaid** que puede visualizarse y exportarse.

## 📁 Archivos generados
- `diagrama-clases.mmd` - Código fuente Mermaid
- `diagrama-clases.png` - Imagen PNG (generar con las herramientas abajo)
- `diagrama-clases.pdf` - PDF (generar con las herramientas abajo)

## 🔧 Cómo visualizar y exportar

### Opción 1: Mermaid Live Editor (Online - Recomendado)
1. Abre https://mermaid.live/
2. Copia el contenido de `diagrama-clases.mmd` en el editor
3. El diagrama se renderiza automáticamente
4. Usa los botones de exportación: **Download PNG** / **Download SVG** / **Download PDF**

### Opción 2: VS Code con extensión Mermaid
1. Instala la extensión "Mermaid Preview" o "Markdown Preview Mermaid Support"
2. Abre el archivo `.mmd` o crea un `.md` con el código dentro de bloques ```mermaid ... ```
3. Ctrl+Shift+V para previsualizar
4. Click derecho → "Save as PNG/PDF"

### Opción 3: CLI con mermaid-cli (npm)
```bash
npm install -g @mermaid-js/mermaid-cli
mmdc -i diagrama-clases.mmd -o diagrama-clases.png
mmdc -i diagrama-clases.mmd -o diagrama-clases.pdf
```

### Opción 4: GitHub / GitLab
- Los archivos `.mmd` se renderizan automáticamente en repositorios
- Solo sube el archivo y se verá en la web

## 📋 Qué incluye el diagrama

### Modelo (Entidades de negocio)
| Clase | Tipo | Descripción |
|-------|------|-------------|
| `Persona` | `abstract` | Clase base con id, nombre, nit, telefono |
| `Cliente` | `class` | Hereda de Persona + direccion |
| `Empleado` | `class` | Hereda de Persona + codigoEmpleado, puesto (sobrescribe mostrarInformacion) |
| `Producto` | `class` | Catalogo con existencia y validación hayExistencia() |
| `Factura` | `class` | Cabecera + lista de detalles + cliente + total |
| `FacturaDetalle` | `class` | Línea de factura con producto, cantidad, precio |

### Relaciones principales
- **Herencia**: `Persona` → `Cliente`, `Persona` → `Empleado`
- **Composición**: `Factura` ◆→ `FacturaDetalle` (1..*) - ciclo de vida dependiente
- **Asociación**: `Factura` → `Cliente` (1) - cliente puede tener muchas facturas
- **Asociación**: `FacturaDetalle` → `Producto` (1) - referencia a catálogo

### Capa DAO (Patrón DAO + Singleton)
- **Interfaces**: `FacturaDAO`, `ProductoDAO`, `ClienteDAO`, `EmpleadoDAO`
- **Implementaciones SQLite**: `FacturaDAOSQLite`, `ProductoDAOSQLite`, `ClienteDAOSQLite`, `EmpleadoDAOSQLite` (todas Singleton)
- **Realización**: Implementaciones `..|>` Interfaces

### Controladores (MVC)
- `FacturaController`, `ProductoController`, `ClienteController`, `EmpleadoController`
- Asociados a sus respectivas interfaces DAO

### Vistas (MDI - JInternalFrame)
- `FrmPrincipal` (contenedor MDI con JDesktopPane)
- `FrmFactura`, `FrmListaFacturas`, `FrmDetalleFactura`
- `FrmProductos`, `FrmClientes`, `FrmEmpleados`
- `FrmVistaPreviaFactura`, `TicketFactura` (implements Printable)
- `CampoBusqueda` (componente reutilizable con autocompletado)

### Patrones aplicados
- **MVC**: Separación clara Vista → Controlador → DAO → Modelo
- **DAO**: Interfaces para desacoplar persistencia
- **Singleton**: Todos los DAOs y ConexionBD
- **Composición**: FacturaDetalle ligado a Factura
- **Herencia**: Persona → Cliente/Empleado con polimorfismo

## 📝 Notas para la entrega
1. El diagrama refleja el estado **actual (Fase 11)** del proyecto
2. Incluye todas las clases, atributos, métodos principales
3. Multiplicidades correctas (1, 1..*, 0..1, etc.)
4. Tipos de relación diferenciados: herencia, composición, asociación, realización
5. Interfaces DAO con sus implementaciones
6. Arquitectura MDI en la capa vista