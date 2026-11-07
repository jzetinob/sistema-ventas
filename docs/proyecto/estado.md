# Estado del proyecto

> Actualizado el 2026-11-06. Es la foto de "dónde estamos". El porqué de cada fase está en [historial-de-desarrollo.md](historial-de-desarrollo.md).

## Resumen

El proyecto **cumple todo lo que pide la guía del curso**:
- App de escritorio completa.
- Base de datos con 13 entidades, en la nube (Supabase / PostgreSQL) o local (SQLite).
- App móvil Android conectada a la misma base por la API REST de Supabase; crea órdenes de venta (tarea final).
- Manuales de usuario y técnico.

Todo está en GitHub, en la rama `main`.

| Pieza | Estado | Dónde |
|---|---|---|
| App de escritorio | ✅ Terminada (fases 1–14, 17: Resumen con gráficos, 18: varios teléfonos por cliente y 19: órdenes de venta) | `sistema-ventas/` |
| API REST | ✅ Funciones de Supabase; endpoint de la tarea final `app_registrar_orden_venta` | `api/` |
| Base en la nube | ✅ Funcionando (proyecto Supabase `sistema-ventas`) | `sistema-ventas/src/main/resources/bd/esquema-postgresql.sql` |
| App móvil | ✅ Resumen con gráficos, factura, detalle, sin conexión, modo oscuro (fase 17) y órdenes de venta (fase 19). La versión actual se instaló y probó en un teléfono real el 2026-11-06 | `movil/`, APK en `entregables/VentasMovil.apk` |
| Manuales | ✅ | `entregables/manual-usuario.pdf`, `entregables/manual-tecnico.pdf` |
| Diagrama ER | ✅ Al día | [`../diagramas/diagrama-entidad-relacion.md`](../diagramas/diagrama-entidad-relacion.md) |
| Diagrama de clases | ✅ Al día (5 vistas por capa, en PNG) | [`../diagramas/diagrama-clases.md`](../diagramas/diagrama-clases.md) |

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

## Supabase activo (plan Free)

El plan gratuito de Supabase no vence, pero **pausa el proyecto si pasa 7 días sin actividad**. Mientras está pausado, la API REST no responde: la app móvil no puede iniciar sesión y la app de escritorio no se conecta en modo nube (sí funciona en modo local, con SQLite). Para que todo esté disponible cuando el docente revise, el proyecto se usa al menos una vez por semana y se anota aquí:

| Fecha | Uso |
|---|---|
| 2026-10-24 | Capturas de la app móvil en el emulador (tarea 8) |
| 2026-10-31 | Tarea final: orden de venta OV-0001 creada desde Android por la API REST |
| 2026-11-06 | APK actual instalado en un teléfono Android real; inicio de sesión desde el teléfono registrado en la base a las 23:22 |

**Próximo uso:** antes del 2026-11-13. Si al revisar el proyecto aparece pausado, se reactiva en supabase.com con *Restore project* y vuelve en uno o dos minutos, con todos los datos.

## Avisos

- Si la app de escritorio se cierra de golpe y deja un archivo `hs_err_pid*.log`, fue un fallo interno de Java o del driver de video (en `awt.dll`), no del código. Git ignora esos archivos.
