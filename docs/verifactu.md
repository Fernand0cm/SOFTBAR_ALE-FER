# Facturacion y Verifactu

La app genera una factura simplificada al confirmar cada cobro. El flujo usa una transaccion de Firestore para:

- Leer la configuracion fiscal.
- Leer el contador anual de facturas.
- Crear la venta.
- Crear la factura.
- Actualizar el contador y el ultimo hash.
- Marcar comanda y mesa como pagadas cuando aplica.

## Configuracion fiscal

Documento recomendado:

```text
configuracion/fiscal
```

Campos:

- `nifEmisor`: NIF/CIF del emisor.
- `serie`: serie visible de facturacion, por ejemplo `A`.

Si el documento no existe, la app usa valores por defecto para que la demo siga funcionando.

## Numeracion

La numeracion se separa por anio mediante documentos de contador:

```text
contadores/facturas_2026
```

El numero visible sigue el formato:

```text
SERIE-0001/2026
```

## Alcance del prototipo

La implementacion actual es adecuada para demostrar el flujo tecnico del TFG:

- Factura asociada a una venta real.
- Lineas reales de venta.
- Cuota de IVA calculada por tipo (10%, 21%, 4%) sumando la de cada linea.
- Desglose de pago.
- Hash encadenado.
- QR de validacion en entorno de pruebas.
- Rectificacion de facturas (factura rectificativa encadenada).

No se presenta como validacion legal completa. Antes de un uso real habria que revisar el formato exacto de registros, firma, envio y requisitos vigentes.

## Rectificacion / anulacion

Siguiendo el principio de Verifactu, **una factura no se borra ni se modifica**.
Para anular una venta se emite una **factura rectificativa**:

- Se crea como una factura mas en la cadena (numero correlativo y hash encadenado).
- Lleva `tipo = "rectificativa"` y `facturaRectificadaNumero` con el numero de la
  original.
- Sus importes (total, cuota, pagos) van en **negativo**, de modo que neutralizan
  la original en caja e informes.
- Se crea ademas una **venta rectificativa** (`tipo = "rectificativa"`, total
  negativo) asociada al turno activo.
- Si los productos controlan stock, las unidades se **devuelven** al inventario.
- Se evita rectificar dos veces la misma factura.

La accion esta disponible desde el ticket para los roles administrador y caja.
Las reglas de Firestore permiten importes negativos **solo** cuando el registro
es de tipo rectificativa.

## Revision del formato (QR y hash)

Estado del formato actual frente a la especificacion oficial de la AEAT, para
dejar claro que es un prototipo:

- **Cadena de hash**: se usa `numero|fechaIso|nifEmisor|total|cuotaIva|hashAnterior`
  con SHA-256 y encadenado. La especificacion oficial define un conjunto de
  campos y un formato de concatenacion propios; aqui se usa una version
  simplificada y estable para demostrar el encadenamiento.
- **QR**: apunta al endpoint de pruebas `prewww2.aeat.es/.../ValidarQR` con
  `nif`, `numserie`, `fecha` e `importe`. Los nombres y el conjunto de
  parametros podrian diferir del formato definitivo de produccion.
- **Pendiente para uso real**: validar el formato exacto de la huella y del QR
  contra la especificacion vigente, anadir firma electronica y el envio de los
  registros a la AEAT.
