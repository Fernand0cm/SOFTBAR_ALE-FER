---
tags: [pantalla]
---
# Home

![[home.png]]

Pantalla principal tras el login. Muestra email y rol del usuario, el indicador
de conexion y los modulos segun permisos.

**Se llega desde:** [[Login]]
**Navega a:** [[Turno]], [[Mesas]], [[Barra]], [[Caja]], [[Informes]], [[Stock]], [[Historial]], [[Configuracion]]

## Errores y correcciones

| Fecha | Severidad | Error | Estado | Correccion |
|---|---|---|---|---|
| 2026-06-07 | Alta | Indicador "Sin conexion" con internet real (E1) | Resuelto | `registerDefaultNetworkCallback`; ahora muestra "Online" |
| 2026-06-07 | Info | Rol no visible junto al email | Resuelto | Con conexion carga `usuarios/{uid}`; muestra "administrador" |
