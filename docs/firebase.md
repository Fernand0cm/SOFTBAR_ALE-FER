# Firebase en SOFTBAR

Este documento resume la configuracion necesaria para levantar el backend Firebase del TFG y la estructura de datos que usa la app Android.

## Servicios usados

- Firebase Authentication: acceso con email y contrasena.
- Cloud Firestore: base de datos principal de mesas, productos, comandas, ventas, facturas y caja.
- Persistencia offline de Firestore: activada en `SoftbarApp`.

## Configuracion inicial

1. Crear o abrir el proyecto Firebase.
2. Registrar una app Android con package name `com.SOFTBAR_F_A`.
3. Descargar `google-services.json` y colocarlo en `app/google-services.json`.
4. Activar Authentication con proveedor Email/Password.
5. Crear al menos un usuario de prueba desde Firebase Console.
6. Activar Cloud Firestore.
7. Publicar las reglas incluidas en `firestore.rules`.

Comandos utiles si se usa Firebase CLI:

```bash
firebase login
firebase use tfg-softba
firebase deploy --only firestore:rules
```

## Seguridad

Las reglas actuales bloquean los datos de negocio a usuarios no autenticados. La unica lectura publica es `splash_backgrounds`, porque se usa antes de iniciar sesion para cargar el fondo del splash.

Limitaciones pendientes:

- Roles reales por usuario: administrador, camarero, caja y cocina.
- Separacion por negocio/local si se quiere convertir SOFTBAR en multiempresa.
- Validacion mas profunda de lineas de comanda y permisos por operacion.

## Colecciones

### `mesas`

Documento recomendado: id numerico como `1`, `2`, `3`.

Campos:

- `numero`: entero visible en la sala.
- `estado`: `libre`, `ocupada`, `cobro` o `cerrada`.
- `comandaActivaId`: id de la comanda abierta, o `null`.

### `productos`

Documento recomendado: codigo de barras.

Campos:

- `codigoBarras`: string.
- `nombre`: string.
- `precio`: numero en EUR.

### `comandas`

Campos:

- `mesaId`: id de mesa.
- `mesaNumero`: numero visible de mesa.
- `estado`: `abierta` o `pagada`.
- `fechaApertura`: timestamp.
- `lineas`: lista de productos anadidos.

### `ventas`

Campos:

- `fecha`: timestamp.
- `total`: numero en EUR.
- `metodo`: `Efectivo`, `Tarjeta` o `Mixto`.
- `facturaId`: id de la factura generada.
- `comandaId`: id de comanda cuando la venta viene de sala.
- `mesaId`: id de mesa cuando aplica.
- `mesaNumero`: numero visible de mesa cuando aplica.
- `turnoId`: id del turno activo.
- `usuarioUid`: usuario que registra la venta.
- `usuarioEmail`: email del usuario.
- `lineas`: productos vendidos.

### `facturas`

Documento recomendado: numero con barra sustituida por guion, por ejemplo `0001-2026`.

Campos:

- `numero`: numero fiscal visible, por ejemplo `0001/2026`.
- `fecha`: timestamp.
- `nifEmisor`: NIF/CIF emisor.
- `total`: total con IVA.
- `cuotaIva`: cuota de IVA calculada.
- `hashAnterior`: hash de la factura anterior.
- `hashActual`: hash de la factura actual.
- `urlValidacion`: URL usada para generar el QR Verifactu.
- `metodo`: metodo de pago.
- `mesaId`: id de mesa cuando aplica.
- `mesaNumero`: numero visible de mesa cuando aplica.
- `lineas`: productos facturados.

### `contadores`

Documento `facturas`:

- `ultimo`: ultimo numero de factura emitido.
- `hashUltimo`: ultimo hash emitido para encadenar la siguiente factura.

La app actualiza este documento dentro de una transaccion durante el cobro.

### `movimientos_caja`

Campos:

- `fecha`: timestamp.
- `tipo`: `apertura`, `entrada` o `retirada`.
- `importe`: numero.
- `descripcion`: texto opcional.
- `turnoId`: id del turno asociado.
- `usuarioUid`: usuario que registra el movimiento.
- `usuarioEmail`: email del usuario.

### `turnos`

Campos:

- `fechaApertura`: timestamp.
- `fechaCierre`: timestamp opcional.
- `estado`: `abierto` o `cerrado`.
- `importeInicial`: efectivo inicial del turno.
- `efectivoContado`: efectivo contado al cierre.
- `efectivoEsperado`: efectivo esperado segun apertura, ventas en efectivo, entradas y retiradas.
- `diferenciaCaja`: diferencia entre efectivo contado y esperado.
- `usuarioUid`: usuario que abre el turno.
- `usuarioEmail`: email del usuario.

### `splash_backgrounds`

Campos:

- `imageUrl`: URL HTTPS de la imagen.
- `active`: booleano.

## Flujo de cobro actual

Al confirmar un cobro, la app ejecuta una transaccion Firestore que:

1. Comprueba que existe un turno abierto para el usuario.
2. Lee `contadores/facturas`.
3. Calcula el siguiente numero de factura.
4. Genera factura, hash y QR Verifactu.
5. Guarda la venta con `facturaId`, `turnoId` y lineas reales.
6. Guarda la factura exacta con lineas reales.
7. Marca la comanda como `pagada`, si existe.
8. Libera la mesa y limpia `comandaActivaId`, si aplica.

Despues abre el ticket pasando `ventaId` y `facturaId`, evitando cargar "la ultima factura global".
