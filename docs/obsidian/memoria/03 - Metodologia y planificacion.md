---
tags: [tfg, memoria]
capitulo: 3
---
# 3. Metodología y planificación

[[02 - Estado del arte y justificacion|← Anterior]] · siguiente: [[04 - Requisitos y casos de uso]]

## 3.1 Metodología

Desarrollo **iterativo e incremental** por fases funcionales, cada una con su
commit cerrado y verificable. Filosofía de **carga cognitiva mínima** (módulos
profundos con interfaces simples) y **calidad continua** (pruebas + CI).

```mermaid
flowchart LR
    R[Requisitos] --> D[Diseno]
    D --> I[Implementacion]
    I --> P[Pruebas]
    P --> CI[Integracion continua]
    CI --> Rev[Revision]
    Rev --> R
```

## 3.2 Herramientas de proceso

| Área | Herramienta |
|---|---|
| Control de versiones | Git + GitHub |
| Integración continua | GitHub Actions |
| Gestión de la documentación | Obsidian (este vault) |
| Pruebas | JUnit + Firebase Emulator Suite |
| IDE | Android Studio |

## 3.3 Planificación temporal

```mermaid
gantt
    title Planificacion SOFTBAR
    dateFormat  YYYY-MM-DD
    axisFormat  %d/%m
    section Base
    Andamiaje + Auth + UI        :a1, 2026-03-19, 40d
    section Nucleo
    Flujo real (comanda/cobro)   :a2, 2026-05-02, 12d
    Turnos y caja                :a3, 2026-05-12, 2d
    section Cierre v1.0
    Hardening seguridad/pruebas  :a4, 2026-06-06, 1d
    Funcionalidad avanzada       :a5, 2026-06-07, 1d
    Release v1.0.0 + CI + docs   :a6, 2026-06-07, 1d
```

## 3.4 Gestión de riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Numeración de factura duplicada | Alto | Transacción Firestore + contador monótono |
| Pérdida de datos sin conexión | Medio | Persistencia offline + cobro requiere transacción |
| Escalada de privilegios | Alto | Reglas: sin auto-ascenso de rol |
| Errores de redondeo monetario | Medio | `BigDecimal` y céntimos |
| Demo dependiente del escáner | Bajo | Alta manual de producto |

## 3.5 Control de versiones

Historial **granular** (un commit por capacidad), mensajes en imperativo, rama
`main` **protegida** (exige CI en verde) y **etiqueta `v1.0.0`**. Ver
[[12 - Despliegue y operacion]] y [[11 - Pruebas y CICD]].
