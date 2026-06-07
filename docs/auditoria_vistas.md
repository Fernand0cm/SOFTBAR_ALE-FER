# Auditoria de vistas y colecciones (para las capturas)

Revision de que muestra cada pantalla con los datos sembrados (~285 productos,
8 mesas, config fiscal, usuario admin) y de que necesita cada coleccion. Hecha a
nivel de codigo; las capturas se toman tras ejecutar `tools/seed`.

## Revision de colecciones (tablas)

| Coleccion | Tras el seed | Coincide con reglas/esquema |
|---|---|---|
| `productos` | ~285 docs con `codigoBarras, nombre, precio, tipoIva, activo, categoria, controlarStock, stock, stockMinimo` | Si (`validProducto`) |
| `mesas` | 8 docs `{numero, estado:"libre", comandaActivaId:null}` | Si (`validMesa`) |
| `configuracion/fiscal` | `{nifEmisor, serie}` | Si (`validConfigFiscal`) |
| `usuarios/{uid}` | admin de `fer@softbar.com` `{email, nombre, rol:"administrador"}` | Si (`validUsuario`) |
| `comandas`, `ventas`, `facturas`, `movimientos_caja`, `turnos`, `contadores` | **vacias** (actividad anterior borrada) | N/A hasta operar |
| `splash_backgrounds` | sin tocar | lectura publica |

> Importante: las pantallas que dependen de ventas/turnos (Informes, Caja,
> Historial, Cierres) **apareceran vacias** justo despues del seed. Para
> capturarlas con datos hay que **abrir turno y hacer 2-3 ventas** primero
> (paso 3-5 del `guion_demo.md`).

## Revision por pantalla

| Pantalla | Lee | Con el seed se ve | Veredicto |
|---|---|---|---|
| Splash / Login | Auth | Login; con sesion va a Home | OK |
| Home | `usuarios/{uid}` + red/sync | Email + rol "administrador"; todos los modulos visibles; indicador de conexion | OK |
| Mesas | `mesas` | 8 mesas en verde (libres) | OK |
| Comanda | `productos` (activos) + `comandas` | Catalogo con chips de **12 categorias**; al elegir una se ven ~15-30 productos | OK (ver nota de rendimiento) |
| Barra | `productos` (activos) | Igual que comanda, sin mesa | OK (ver nota) |
| Cobro | total + lineas | Metodos efectivo/tarjeta/mixto, cambio, IVA por linea | OK |
| Ticket | `ventas` + `facturas` | Numero, base/IVA, pago, QR Verifactu; boton Rectificar (admin/caja) | OK |
| Caja | `turnos` + `ventas` + `movimientos_caja` | Vacia sin turno; con turno, resumen y arqueo | OK (requiere turno) |
| Cierres | `turnos` cerrados | Vacia hasta cerrar un turno | OK |
| Informes | `ventas` del dia + ultimos 7 dias | Vacio sin ventas; con ventas: KPIs, base/IVA, top productos, comparativa | OK (requiere ventas) |
| Historial | `ventas` (50 ult.) | Vacio hasta vender; luego lista con reapertura | OK (requiere ventas) |
| Stock | `productos` con `controlarStock` | ~110 productos (refrescos, cervezas, vinos, licores) con stock y alerta de bajo | OK (ver nota) |
| Config | `productos` (todos) | Lista de 285 con codigo, categoria, precio+IVA; alta/edicion/desactivar | OK (ver nota) |

## Nota de rendimiento (importante)

Comanda, Barra, Config y Stock pintan sus listas inflando **todas** las vistas
dentro de un `ScrollView` (no usan `RecyclerView`). Con ~285 productos:

- En **Config** y en el catalogo con el filtro **"Todas"** se inflan cientos de
  vistas de golpe: puede haber un pequeno tiron al abrir.
- **Mitigacion para la demo**: en comanda/barra, selecciona una **categoria**
  (muestra 15-30 productos, fluido). Es ademas el flujo natural de uso.
- **Mejora futura** (documentada, no critica para el TFG): migrar estas listas a
  `RecyclerView` para reciclar vistas.

## Checklist para las capturas

1. Ejecutar `tools/seed` (deja catalogo + mesas, y limpia ventas/turnos).
2. Login con `fer@softbar.com`.
3. Capturas "estaticas" ya disponibles: Home, Mesas, Comanda (con categorias),
   Stock, Config.
4. Abrir turno y hacer 2-3 ventas (efectivo, tarjeta y mixto) para poblar:
   Ticket, Caja, Informes, Historial.
5. Cerrar un turno para capturar Cierres.
6. Rectificar una factura para capturar la rectificativa.
