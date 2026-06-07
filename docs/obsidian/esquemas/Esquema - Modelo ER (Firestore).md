---
tags: [tfg, esquema, datos]
---
# Esquema — Modelo ER (Firestore)

[[Inicio|Índice]] · relacionado: [[06 - Modelo de datos]]

```mermaid
erDiagram
    USUARIO {
      string uid PK
      string email
      string rol
    }
    TURNO {
      string id PK
      timestamp apertura
      timestamp cierre
      double importeInicial
      double diferenciaCaja
      string usuarioUid FK
    }
    MOVIMIENTO_CAJA {
      string id PK
      string tipo
      double importe
      string turnoId FK
      string usuarioUid FK
    }
    MESA {
      int numero PK
      string estado
      string comandaActivaId FK
    }
    COMANDA {
      string id PK
      string mesaId FK
      string estado
    }
    PRODUCTO {
      string codigoBarras PK
      double precio
      double tipoIva
      string categoria
      int stock
    }
    VENTA {
      string id PK
      double total
      string metodo
      string tipo
      string turnoId FK
      string facturaId FK
    }
    FACTURA {
      string numero PK
      double total
      double cuotaIva
      string hashAnterior
      string hashActual
      string tipo
      string facturaRectificadaNumero FK
    }
    CONTADOR {
      string id PK
      int ultimo
      string hashUltimo
    }

    USUARIO ||--o{ TURNO : abre
    USUARIO ||--o{ VENTA : registra
    TURNO ||--o{ VENTA : agrupa
    TURNO ||--o{ MOVIMIENTO_CAJA : contiene
    MESA ||--o| COMANDA : tiene
    COMANDA }o--o{ PRODUCTO : "lineas[]"
    VENTA }o--o{ PRODUCTO : "lineas[]"
    VENTA ||--|| FACTURA : genera
    FACTURA }o--|| CONTADOR : numera
    FACTURA ||--o| FACTURA : rectifica
```

> [!note] En Firestore las **líneas** se almacenan **embebidas** (array de
> objetos) dentro de comanda/venta/factura; aquí se muestran como relación
> lógica con PRODUCTO para claridad conceptual.
