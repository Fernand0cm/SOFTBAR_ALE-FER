---
tags: [tfg, esquema, uml]
---
# Esquema — Casos de uso

[[Inicio|Índice]] · relacionado: [[04 - Requisitos y casos de uso]]

```mermaid
flowchart LR
    Admin([Administrador])
    Cam([Camarero])
    Caja([Caja])
    Coc([Cocina])

    subgraph Sistema SOFTBAR
      U1((Iniciar sesion))
      U2((Abrir/cerrar turno))
      U3((Tomar comanda))
      U4((Cobrar))
      U5((Emitir/ver ticket))
      U6((Rectificar factura))
      U7((Gestionar caja))
      U8((Gestionar catalogo))
      U9((Gestionar stock))
      U10((Ver informes))
      U11((Gestionar usuarios/roles))
    end

    Cam --- U1 & U3 & U4 & U5
    Caja --- U1 & U2 & U4 & U7 & U9 & U10
    Coc --- U1 & U3
    Admin --- U1 & U2 & U3 & U4 & U5 & U6 & U7 & U8 & U9 & U10 & U11
```

> [!note] El acceso a cada caso de uso lo controlan los **permisos por rol**
> (UI) y, en el servidor, las **reglas de Firestore**.
