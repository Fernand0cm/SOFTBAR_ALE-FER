# Actualizaciones de los ultimos 14 dias

Periodo revisado: del 29/04/2026 al 13/05/2026.

Este documento resume los cambios mas relevantes subidos al proyecto durante las dos ultimas semanas.

## Resumen general

Durante este periodo SOFTBAR ha pasado de ser una base visual de TPV a una app mucho mas conectada y defendible para el TFG. El flujo principal ya cubre:

```text
login -> turno -> sala/barra -> comanda -> cobro -> venta -> factura -> ticket -> caja -> cierre
```

## Cambios principales

### 1. Rediseno visual y base de UI

Commit relevante:

- `9bd28b2` - Rediseno visual completo con sistema de estilos y Material Components.

Puntos destacados:

- Pantallas principales redisenadas.
- Estilos comunes para tarjetas, botones, colores y cabeceras.
- Iconografia de modulos.
- Pantalla Home mas clara.
- Layouts mas consistentes para login, home, mesas, comanda, cobro, ticket, caja e informes.

### 2. Modelos, informes y Verifactu inicial

Commits relevantes:

- `0c0dfa8` - Tutoria 29/04.
- `f0fea3a` - Documentacion del progreso con Firebase.

Puntos destacados:

- Modelos base: `Mesa`, `Venta`, `Factura`.
- Utilidades de Verifactu:
  - hash SHA-256 encadenado.
  - generacion de URL QR.
  - generacion de bitmap QR.
- Indicadores de ventas:
  - total vendido.
  - numero de tickets.
  - ticket medio.
  - distribucion por hora.
- Primeras pruebas unitarias.
- Documentacion de tests.

### 3. Splash con fondo desde Firestore

Commit relevante:

- `3810af4` - Splash con fondo aleatorio desde Firestore.

Puntos destacados:

- Lectura de `splash_backgrounds`.
- Soporte para imagenes externas por URL.
- Fallback visual si no hay fondos activos.
- Preparado para mejorar la primera impresion de la demo.

### 4. Comanda real

Commit relevante:

- `a9fc84e` - Comanda real con catalogo de productos y cobro encadenado.

Puntos destacados:

- Modelo `Comanda`.
- Modelo `LineaComanda`.
- Calculo real de total.
- Catalogo de productos cargado desde Firestore.
- Anadir productos a la comanda.
- Aumentar cantidad si el producto ya existe.
- Quitar unidades.
- Persistencia de lineas en Firestore.
- Paso a cobro con total real.

### 5. Barra rapida real

Commit relevante:

- `db4cf48` - Barra rapida real con catalogo de productos en grid.

Puntos destacados:

- Catalogo real en barra.
- Pedido directo sin mesa.
- Lineas mantenidas en memoria.
- Cobro directo.
- Venta y factura generadas despues del cobro.

### 6. Caja real inicial

Commit relevante:

- `83c25cd` - Caja real con ventas y movimientos del turno desde Firestore.

Puntos destacados:

- Modelo `MovimientoCaja`.
- Modelo `ResumenCaja`.
- Movimientos manuales:
  - apertura.
  - entrada.
  - retirada.
- Resumen de caja desde datos reales.
- Pruebas unitarias para caja.
- UI de movimientos.

### 7. Firebase reforzado y planning funcional

Commit relevante:

- `c80b18d` - Documenta plan funcional y refuerza Firebase.

Puntos destacados:

- Anadir `.firebaserc`.
- Anadir `firebase.json`.
- Anadir `firestore.rules`.
- Anadir `firestore.indexes.json`.
- Centralizar nombres de colecciones y campos en `FirestoreSchema`.
- Crear `docs/firebase.md`.
- Crear `docs/planning_funcional.md`.
- Reglas Firestore con validaciones por coleccion.
- Indices necesarios para consultas compuestas.

### 8. Ticket con venta real

Commit relevante:

- `2241ef2` - Completa ticket con venta real.

Puntos destacados:

- `Venta` guarda lineas reales.
- `Factura` guarda lineas reales.
- Cobro pasa `ventaId` y `facturaId` al ticket.
- Ticket carga la venta exacta.
- Ticket deja de depender de datos de ejemplo.
- Se muestra:
  - numero de factura.
  - mesa.
  - lineas.
  - total.
  - metodo de pago.
  - QR Verifactu.

### 9. Turnos y caja por turno

Commit relevante:

- `168b966` - Implementa turnos y caja por turno.

Puntos destacados:

- Modelo `Turno`.
- Apertura de turno con importe inicial.
- Guardado de usuario y fecha.
- Movimiento automatico de apertura.
- Bloqueo de cobros si no hay turno abierto.
- Asociar ventas al turno activo.
- Caja filtrada por `turnoId`.
- Indices Firestore para ventas y movimientos por turno.

### 10. Cierre de caja

Commit relevante:

- `19b2d73` - Completa cierre de caja.

Puntos destacados:

- Cierre con efectivo contado.
- Calculo de efectivo esperado.
- Calculo de diferencia de caja.
- Guardado de cierre historico en `turnos`.
- La pantalla de turno redirige a caja para cerrar con arqueo.
- Reglas Firestore actualizadas.

### 11. Cobro completo y facturacion

Commit relevante:

- `fb70fb4` - Completa cobro y facturacion.

Puntos destacados:

- Cobro en efectivo.
- Cobro con tarjeta.
- Cobro mixto.
- Entrada real de importes.
- Validacion de que el pago cubre el total.
- Calculo de cambio.
- Guardado de desglose:
  - `pagoEfectivo`.
  - `pagoTarjeta`.
  - `importeRecibido`.
  - `cambio`.
- Ticket muestra desglose de pago.
- Caja separa efectivo y tarjeta usando el desglose real.
- Configuracion fiscal desde Firestore.
- Numeracion anual por serie.
- Documento `docs/verifactu.md`.

### 12. README completo e imagenes de navegacion

Commits relevantes:

- `c6e287c` - Actualiza README del proyecto.
- `21055d7` - Corrige diagrama de flujo de venta.

Puntos destacados:

- README actualizado con estado real del proyecto.
- Documentacion del flujo funcional.
- Documentacion de Firebase, pruebas, estructura y pendientes.
- Imagenes SVG de navegacion:
  - `docs/images/navigation-main.svg`
  - `docs/images/navigation-sale-flow.svg`
  - `docs/images/navigation-firebase.svg`
- Correccion visual del diagrama de flujo de venta para evitar solapamientos.

## Estado funcional alcanzado

La app ya permite defender un flujo completo de TPV:

- Iniciar sesion.
- Abrir turno.
- Crear productos desde configuracion.
- Abrir mesa.
- Crear comanda.
- Anadir productos.
- Cobrar con efectivo, tarjeta o mixto.
- Generar venta.
- Generar factura.
- Ver ticket con QR.
- Consultar caja del turno.
- Cerrar caja con arqueo.

## Pendiente principal

Queda por mejorar:

- Alta manual de producto sin escaner.
- Edicion y desactivacion de productos.
- Categorias e IVA por producto.
- Notas y modificadores en comanda.
- Historial de tickets.
- Consulta historica de cierres.
- Filtros avanzados de informes.
- Roles de usuario.
- Stock real.
- Impresion/exportacion.
- Anulacion o rectificacion de facturas.
- Pruebas instrumentadas de navegacion.
