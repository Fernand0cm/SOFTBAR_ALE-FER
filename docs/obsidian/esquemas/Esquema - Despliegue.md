---
tags: [tfg, esquema, devops]
---
# Esquema — Despliegue

[[Inicio|Índice]] · relacionado: [[12 - Despliegue y operacion]]

```mermaid
flowchart TB
    subgraph Dispositivo["Dispositivo / Emulador Android"]
        APP[App SOFTBAR\nAPK debug/release]
        CACHE[(Cache offline\nFirestore)]
    end
    subgraph Firebase["Firebase (nube)"]
        AUTH[Authentication]
        FS[(Cloud Firestore)]
        RULES{{Security Rules}}
    end
    subgraph GitHub["GitHub"]
        REPO[(Repositorio)]
        ACTIONS[GitHub Actions CI]
    end

    APP <-->|Auth| AUTH
    APP <-->|lectura/escritura| FS
    FS --- RULES
    APP --- CACHE
    REPO --> ACTIONS
    ACTIONS -->|deploy reglas| RULES
```

> [!tip] El cliente trabaja contra Firestore con **persistencia offline**; las
> **reglas** se versionan en el repo y se despliegan con `firebase deploy`.
