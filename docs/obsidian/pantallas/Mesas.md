---
tags: [pantalla]
---
# Mesas

![[mesas.png]]

Sala con el grid de mesas por estado (libre/ocupada/cobro/cerrada). Al tocar una
mesa se abre o recupera su comanda.

**Se llega desde:** [[Home]]
**Navega a:** [[Comanda]]

## Errores y correcciones

| Fecha | Severidad | Error | Estado | Correccion |
|---|---|---|---|---|
| 2026-06-07 | Media | Datos antiguos: falta la mesa 2 y varias salen ocupadas sin comanda real (ver E2) | Abierto | Ejecutar `tools/seed` |
