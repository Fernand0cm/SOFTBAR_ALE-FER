# Demo completa del TPV - Resultado

Recorrido end-to-end ejecutado en emulador (Android 14) con `fer@softbar.com`
(administrador), en linea y con datos reales (~285 productos, mesas libres,
turno abierto). Capturas en esta carpeta (`docs/demo/`) y en
`docs/obsidian/assets/`.

## Capacidades como TPV (operativa de venta)

| Capacidad | Verificado | Evidencia |
|---|---|---|
| Sesion y rol; Home filtra modulos por permisos | OK | `obsidian/assets/home.png` (admin, Online) |
| Sala y mesas con estados (libre/ocupada/cobro/cerrada) | OK | `01_mesas.png` |
| Comanda: catalogo (285) + filtro por categorias + cantidades | OK | `02_comanda_add.png` |
| Personalizacion de linea (modificadores + nota) | OK | `03_personalizar.png` |
| Barra rapida (venta sin mesa) | OK | `obsidian/assets/barra.png` |
| Cobro efectivo / tarjeta / mixto, con cambio | OK | `05_cobro_mixto.png` |
| Ticket: nº factura, base imponible, IVA, desglose, QR Verifactu | OK | `06_ticket_tarjeta.png` |
| Rectificacion: factura rectificativa encadenada (importes en negativo) | OK | `07_ticket_rectificativa.png` (-13.00 / base -11.82 / IVA -1.18) |

## Capacidades de gestion

| Capacidad | Verificado | Evidencia |
|---|---|---|
| Apertura de turno e importe inicial | OK | `obsidian/assets/turno.png` |
| Caja: resumen del turno (apertura, ventas por metodo, retiradas, total esperado) | OK | `08_caja_resumen.png` |
| Caja: registrar movimiento (entrada/retirada) | OK | `09_movimiento_dialog.png`, `10_caja_tras_retirada.png` |
| Cierre de turno con arqueo y consulta de cierres | OK | `obsidian/assets/cierres.png` |
| Stock: control opcional por producto, alertas y reposicion; descuento al vender | OK | `obsidian/assets/stock.png` |
| Catalogo: alta (escaner/manual), edicion, IVA, categoria, desactivacion | OK | `obsidian/assets/config.png` |
| Informes: KPIs, base/IVA, top productos, comparativa por dias | OK | `11_informes.png` |
| Informes: filtros por fecha, turno y metodo de pago | OK | `12_informes_efectivo.png` (Efectivo: 9.90 / 3 tickets vs Todas 17.30 / 4) |
| Historial de tickets + reapertura | OK | `obsidian/assets/historial.png` |
| Exportar resumen (compartir) | OK (boton) | `11_informes.png` |

## Integridad y robustez (verificado en el flujo)

- **Cobro transaccional atomico**: cada venta crea venta + factura + numeracion
  y libera la mesa de forma atomica; el stock se descuenta en la misma
  transaccion.
- **Verifactu**: numeracion correlativa, hash encadenado, QR y factura
  rectificativa (sin borrar la original).
- **Seguridad**: reglas endurecidas desplegadas en produccion; los roles se
  asignan por un administrador (no hay auto-ascenso).
- **Conexion**: indicador en Home (Online/Sincronizando/Sin conexion); el cobro
  ya no se bloquea por falsos negativos de conectividad.
- **Rendimiento**: catalogo, configuracion y stock con `RecyclerView`.
- **Calidad**: 85 pruebas unitarias + 25 de reglas; CI (GitHub Actions) en verde.

## Conclusion

El sistema cubre el ciclo completo de un TPV de hosteleria (acceso, turno,
sala/barra, comanda, cobro, factura/ticket, rectificacion) y la gestion del
negocio (caja y arqueo, stock, catalogo, informes con filtros e historial),
con integridad transaccional, base Verifactu y seguridad por roles.

Pendiente menor: captura de la pantalla de Login (requiere cerrar sesion).
