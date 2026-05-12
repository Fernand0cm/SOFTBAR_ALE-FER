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
- Desglose de pago.
- Hash encadenado.
- QR de validacion en entorno de pruebas.

No se presenta como validacion legal completa. Antes de un uso real habria que revisar el formato exacto de registros, eventos de anulacion o rectificacion, firma, envio y requisitos vigentes.
