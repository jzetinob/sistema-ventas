# Estado del proyecto

> Actualizado el 2026-09-26. Es la foto de "dónde estamos". El porqué de cada fase está en [historial-de-desarrollo.md](historial-de-desarrollo.md).

## Resumen

El proyecto **cumple todo lo que pide la guía del curso**:
- App de escritorio completa.
- Base de datos con 10 entidades, en la nube (Supabase / PostgreSQL) o local (SQLite).
- App móvil Android conectada a la misma base.
- Manuales de usuario y técnico.

Todo está en GitHub, en la rama `main`.

| Pieza | Estado | Dónde |
|---|---|---|
| App de escritorio | ✅ Terminada (fases 1–14) | `sistema-ventas/` |
| Base en la nube | ✅ Funcionando (proyecto Supabase `sistema-ventas`) | `sistema-ventas/src/main/resources/bd/esquema-postgresql.sql` |
| App móvil | ✅ Probada en un teléfono real | `movil/`, APK en `entregables/VentasMovil.apk` |
| Manuales | ✅ | `entregables/manual-usuario.pdf`, `entregables/manual-tecnico.pdf` |
| Diagrama ER | ✅ Al día | [`../diagramas/diagrama-entidad-relacion.md`](../diagramas/diagrama-entidad-relacion.md) |
| Diagrama de clases | ⚠️ Refleja la tarea 5/6; faltan las clases de las fases 12–15 | [`../diagramas/diagrama-clases.md`](../diagramas/diagrama-clases.md) |

## Configuración de esta PC

| Elemento | Valor |
|---|---|
| Supabase | Cuenta **jzetinob** (universidad), organización `sistema-ventas-umg` (plan Free), proyecto `sistema-ventas` (ref `nlgvnwonkokdhbgpuoqe`, región us-east-1) |
| Contraseña de la base | Solo en `sistema-ventas/config/bd.properties`, archivo local que Git ignora |
| Herramientas | JDK 25, NetBeans 25, Android Studio (SDK con API 37), Supabase CLI (`npx supabase`) con la sesión de jzetinob |

## Pendiente (a cargo del estudiante)

1. Cargar datos de ejemplo para la presentación.
2. Generar el respaldo con datos reales (*Administración → Respaldar base de datos*) para entregarlo.
3. Entregar en la plataforma del curso: enlace de GitHub, los PDF, el APK y el `.db` del respaldo.
4. Opcional: actualizar el diagrama de clases con las clases nuevas.

## Avisos

- **Supabase Free se pausa tras 7 días sin uso.** Se reactiva en supabase.com con *Restore project*. Conviene abrir la app unos días antes de presentar.
- Si la app de escritorio se cierra de golpe y deja un archivo `hs_err_pid*.log`, fue un fallo interno de Java o del driver de video (en `awt.dll`), no del código. Git ignora esos archivos.
