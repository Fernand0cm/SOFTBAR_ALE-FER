---
tags: [tfg, memoria, conclusiones]
capitulo: 13
---
# 13. Resultados y conclusiones

[[12 - Despliegue y operacion|← Anterior]] · siguiente: [[14 - Glosario y bibliografia]]

## 13.1 Cumplimiento de objetivos

| Objetivo | Resultado |
|---|---|
| O1 Flujo de venta E2E | ✅ verificado en demo |
| O2 Caja por turno + arqueo | ✅ |
| O3 Catálogo + IVA + categorías | ✅ |
| O4 Stock con descuento automático | ✅ |
| O5 Informes con filtros + comparativa | ✅ |
| O6 Roles + seguridad en servidor | ✅ |
| O7 Veri*factu (hash, QR, rectificativa) | ✅ (prototipo) |
| O8 Pruebas + CI/CD | ✅ 110 pruebas, CI verde |

## 13.2 Demostración

Demo completa de extremo a extremo con 12 evidencias y verdictos por capacidad en
`docs/demo/RESULTADO_DEMO.md`. Ejemplos verificados: venta con personalización,
pago por tarjeta, ticket con QR, **rectificativa −13,00 €**, caja con arqueo,
informe filtrado por método.

## 13.3 Métricas finales

| Métrica | Valor |
|---|---|
| Pantallas | 14 |
| Colecciones | 11 |
| Pruebas | 85 unitarias + 25 reglas |
| LOC Java | ~6.700 |
| Commits | proceso granular, `main` protegida, `v1.0.0` |

## 13.4 Conclusiones

> [!success] Balance
> SOFTBAR cubre el **ciclo completo de un TPV de hostelería** y la **gestión del
> negocio** con **integridad transaccional**, **seguridad en servidor**, base
> **Veri*factu** y **calidad continua**. Es un prototipo **defendible** y con
> **recorrido comercial**.

## 13.5 Lecciones aprendidas

- La **seguridad en servidor** (reglas + pruebas) cambia el nivel del proyecto.
- Las **transacciones** son la clave de la integridad fiscal.
- La **carga cognitiva baja** (módulos profundos) facilita mantener y crecer.

## 13.6 Líneas futuras

- App Check y endurecimiento de la API key.
- Multiplataforma (KMP/Flutter) reutilizando el dominio.
- Cumplimiento Veri*factu completo (firma + envío AEAT) vía Cloud Functions.
- Datáfono e impresión ESC/POS; vista de cocina; inventario por ingredientes.
