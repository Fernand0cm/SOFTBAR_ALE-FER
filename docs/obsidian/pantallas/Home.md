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
| 2026-06-07 | Alta | Indicador "Sin conexion" con internet real (ver E1 del [[Mapa de navegacion]]) | Abierto | Revisar deteccion de conexion |
| 2026-06-07 | Info | No muestra el rol junto al email al estar el perfil sin cargar (offline) | Abierto | Cargar `usuarios/{uid}` o mostrar rol por defecto |
