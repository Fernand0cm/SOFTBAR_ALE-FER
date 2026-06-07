---
tags: [pantalla]
---
# Comanda

![[comanda.png]]

Catalogo con filtro por categorias (chips) y lineas del pedido. Permite anadir,
ajustar cantidades y personalizar lineas.

Con lineas anadidas:

![[comanda_con_lineas.png]]

**Se llega desde:** [[Mesas]]
**Navega a:** [[Cobro]]

## Errores y correcciones

| Fecha | Severidad | Error | Estado | Correccion |
|---|---|---|---|---|
| 2026-06-07 | Media | Catalogo con pocos productos antiguos (E3) | Resuelto | 285 productos sembrados |
| 2026-06-07 | Baja | Catalogo sin reciclar y "Cobrar" enterrado (E4) | Resuelto | Catalogo en `RecyclerView` + barra inferior fija |
