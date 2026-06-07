---
tags: [pantalla]
---
# Configuracion

![[config.png]]

Catalogo de productos: alta (escaner o manual), edicion, IVA, categoria,
control de stock y desactivacion.

**Se llega desde:** [[Home]]

## Errores y correcciones

| Fecha | Severidad | Error | Estado | Correccion |
|---|---|---|---|---|
| 2026-06-07 | Media | Catalogo con datos antiguos (E3) | Resuelto | 285 productos; los viejos quedan "(inactivo)" |
| 2026-06-07 | Baja | Lista sin `RecyclerView` (E4) | Resuelto | Migrada a `RecyclerView` (`ProductoConfigAdapter`) |
