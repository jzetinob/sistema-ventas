# Documentación del Sistema de Ventas

| Si quieres… | Lee |
|---|---|
| Aprender a **usar** el sistema (escritorio y celular) | [manual-usuario.md](manual-usuario.md) · PDF: [`../entregables/manual-usuario.pdf`](../entregables/manual-usuario.pdf) |
| Entender **cómo está construido**: arquitectura, base de datos, seguridad, API, instalación | [manual-tecnico.md](manual-tecnico.md) · PDF: [`../entregables/manual-tecnico.pdf`](../entregables/manual-tecnico.pdf) |
| Ver las **tablas y relaciones** | [diagramas/diagrama-entidad-relacion.md](diagramas/diagrama-entidad-relacion.md) |
| Ver el **diagrama de clases** UML | [diagramas/diagrama-clases.md](diagramas/diagrama-clases.md) (código: [diagrama-clases.mmd](diagramas/diagrama-clases.mmd)) |
| Saber **qué se hizo en cada fase y por qué** | [proyecto/historial-de-desarrollo.md](proyecto/historial-de-desarrollo.md) |
| Saber **dónde está el proyecto hoy** y qué falta | [proyecto/estado.md](proyecto/estado.md) |

## Organización

```
docs/
├── README.md                     ← este índice
├── manual-usuario.md             ← fuente del PDF de usuario
├── manual-tecnico.md             ← fuente del PDF técnico
├── diagramas/
│   ├── diagrama-entidad-relacion.md
│   ├── diagrama-clases.md
│   └── diagrama-clases.mmd
└── proyecto/
    ├── historial-de-desarrollo.md
    └── estado.md
```

Otras carpetas del repositorio:

| Carpeta | Contenido |
|---|---|
| `tareas/NN-…/` | Entregas del semestre: documento fuente (`.md`), presentación (`.pptx`) y PDF |
| `entregables/` | Lo que se entrega del proyecto final: APK y manuales en PDF |
| `notas/` | Notas personales. Git la ignora; no se publica |

**Para regenerar los PDF** después de editar un manual:
1. Convertir el `.md` a HTML, por ejemplo con el paquete `markdown` de Python.
2. Imprimirlo a PDF con Microsoft Edge en modo automático:
   ```
   msedge --headless --no-pdf-header-footer --print-to-pdf=entregables\manual-usuario.pdf file:///ruta/manual-usuario.html
   ```
