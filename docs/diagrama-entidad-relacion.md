# Diagrama entidad-relación

Base de datos SQLite `datos/sistema_ventas.db`, con **10 tablas**. `ConexionBD` crea las tablas al arrancar y activa las llaves foráneas (`PRAGMA foreign_keys = ON`).

```mermaid
erDiagram
    CATEGORIAS ||--o{ PRODUCTOS : "clasifica"
    PRODUCTOS ||--o{ COMPRA_DETALLES : "se compra en"
    PROVEEDORES ||--o{ COMPRAS : "vende"
    USUARIOS |o--o{ COMPRAS : "registra"
    COMPRAS ||--|{ COMPRA_DETALLES : "contiene"
    FACTURAS ||--|{ FACTURA_DETALLES : "contiene"

    CATEGORIAS {
        INTEGER id PK
        TEXT nombre UK
        TEXT descripcion
    }
    PRODUCTOS {
        INTEGER id PK
        TEXT codigo UK
        TEXT nombre
        REAL precio
        INTEGER existencia
        INTEGER categoria_id FK
    }
    CLIENTES {
        INTEGER id PK
        TEXT nit UK
        TEXT nombre
        TEXT direccion
        TEXT telefono
    }
    EMPLEADOS {
        INTEGER id PK
        TEXT codigo_empleado UK
        TEXT nombre
        TEXT nit
        TEXT telefono
        TEXT puesto
    }
    PROVEEDORES {
        INTEGER id PK
        TEXT nit UK
        TEXT nombre
        TEXT direccion
        TEXT telefono
        TEXT correo
    }
    USUARIOS {
        INTEGER id PK
        TEXT usuario UK
        TEXT nombre
        TEXT clave_hash
        TEXT salt
        TEXT rol
        INTEGER activo
    }
    FACTURAS {
        INTEGER id PK
        TEXT numero_factura UK
        TEXT nit
        TEXT cliente
        TEXT fecha
        REAL total
    }
    FACTURA_DETALLES {
        INTEGER id PK
        INTEGER factura_id FK
        TEXT producto
        INTEGER cantidad
        REAL precio
        REAL subtotal
    }
    COMPRAS {
        INTEGER id PK
        TEXT numero_compra UK
        INTEGER proveedor_id FK
        INTEGER usuario_id FK
        TEXT fecha
        REAL total
    }
    COMPRA_DETALLES {
        INTEGER id PK
        INTEGER compra_id FK
        INTEGER producto_id FK
        INTEGER cantidad
        REAL costo_unitario
        REAL subtotal
    }
```

## Reglas de las llaves foráneas

| Relación | Al borrar el registro padre |
|---|---|
| `productos.categoria_id → categorias.id` | `SET NULL`: el producto queda sin categoría |
| `compras.proveedor_id → proveedores.id` | Se impide: no se borra un proveedor con compras |
| `compras.usuario_id → usuarios.id` | `SET NULL`: la compra se conserva sin usuario |
| `compra_detalles.compra_id → compras.id` | `CASCADE`: se borran los detalles con la compra |
| `compra_detalles.producto_id → productos.id` | Se impide: no se borra un producto con compras |
| `factura_detalles.factura_id → facturas.id` | `CASCADE`: se borran los detalles con la factura |

## Notas

- `facturas` guarda el NIT y el nombre del cliente como texto, y `factura_detalles` guarda el nombre del producto. Es el diseño original de la tarea 3 y sirve como "foto" de la venta. Por eso **clientes** y **empleados** no tienen llave foránea hacia las facturas.
- `usuarios.clave_hash` guarda un hash PBKDF2-SHA256 (120 000 iteraciones), cada uno con su propia sal (`salt`). Las contraseñas nunca se guardan en texto plano.
- `usuarios.rol` solo admite `ADMINISTRADOR` o `VENDEDOR` (restricción `CHECK`).
- Si una base se creó con una versión anterior, se actualiza sola al abrir el sistema: se crean las tablas nuevas y la columna `productos.categoria_id`, sin perder datos.
