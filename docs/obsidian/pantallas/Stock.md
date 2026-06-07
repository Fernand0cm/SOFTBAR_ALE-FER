---
tags: [pantalla]
---
# Stock

![[stock.png]]

Lista de productos con control de stock, alerta de bajo minimo y reposicion
rapida (+/-) o fijando cantidad.

**Se llega desde:** [[Home]]

## Errores y correcciones

| Fecha | Severidad | Error | Estado | Correccion |
|---|---|---|---|---|
| 2026-06-07 | Baja | Lista sin `RecyclerView` (E4) | Resuelto | Migrada a `RecyclerView` (`StockAdapter`) |
| 2026-06-07 | Info | Pocos productos con stock | Resuelto | ~110 productos con control de stock tras el sembrado |
