---
tags: [tfg, memoria, fiscal, verifactu]
capitulo: 10
---
# 10. Facturación y Veri*factu

[[09 - Seguridad|← Anterior]] · siguiente: [[11 - Pruebas y CICD]]

## 10.1 Concepto

Al confirmar cada cobro se genera una **factura simplificada** dentro de la misma
transacción que la venta. Cada factura **encadena** su huella con la anterior,
formando una cadena verificable.

```mermaid
flowchart LR
    F1[Factura n-1\nhashActual] --> F2[Factura n\nhashAnterior]
    F2 --> H[SHA-256\nnumero|fecha|nif|total|cuota|hashAnterior]
    H --> F2h[hashActual]
    F2 --> QR[QR validacion AEAT]
```

## 10.2 Elementos implementados

- **Numeración** correlativa por año: `SERIE-0001/AAAA` (clase `NumeroFactura`).
- **Hash SHA‑256 encadenado** (`HashVerifactu`).
- **QR** de validación en entorno de pruebas (`GeneradorQrVerifactu`, ZXing).
- **Cuota de IVA por tipo** (10/21/4 %) sumando cada línea (`CalculoIva`).
- **Inmutabilidad** de la factura y **contador monótono** (reglas).

## 10.3 Rectificación (anulación)

Siguiendo el principio de Veri*factu, **no se borra**: se emite una **factura
rectificativa** encadenada con importes **en negativo**, que neutraliza la
original en caja e informes y **devuelve el stock**.

> [!example] Evidencia
> En la demo se rectificó una factura de 13,00 € → rectificativa con total
> **−13,00 €** (base −11,82 / IVA −1,18). Ver `docs/demo/RESULTADO_DEMO.md`.

## 10.4 Alcance y honestidad

> [!important] Prototipo técnico, no cumplimiento certificado
> Se demuestran los **conceptos** (huella encadenada, QR, rectificativa,
> numeración transaccional). Para uso real faltarían: formato exacto de registros
> y huella según especificación vigente, **firma electrónica** y **envío a la
> AEAT**. Se documenta con transparencia (ver `docs/verifactu.md`).
