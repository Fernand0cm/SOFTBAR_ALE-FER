---
tags: [tfg, memoria, arquitectura]
capitulo: 5
---
# 5. Arquitectura del sistema

[[04 - Requisitos y casos de uso|← Anterior]] · siguiente: [[06 - Modelo de datos]]

## 5.1 Visión por capas

Separación en **UI**, **repositorios (acceso a datos)** y **lógica pura**. La
dependencia va siempre hacia abajo; la lógica de negocio no depende de Android
ni de Firebase (por eso es testeable con JUnit).

```mermaid
flowchart TD
    subgraph UI["Capa UI (Android)"]
        ACT["Activities (Home, Mesas, Comanda, Cobro, Ticket, Caja, Informes, Stock...)"]
        VM["InformesViewModel (LiveData)"]
        HELP["Helpers: CatalogoCategorias, ProductoAdapter, PersonalizacionLinea"]
    end
    subgraph REPO["Repositorios"]
        R["CobroRepository · RectificacionRepository · InformesRepository · StockRepository · UsuarioRepository · HistorialRepository · CierresRepository"]
    end
    subgraph CORE["Lógica pura + modelos (sin Android)"]
        L["Dinero · CalculoIva · VentasPorProducto · ComparativaDias · Permisos · ResumenCaja · Verifactu(Hash/QR/Numero)"]
    end
    subgraph FB["Firebase"]
        AUTH[Auth]
        FS["Firestore + Security Rules"]
    end
    ACT --> VM --> REPO
    ACT --> HELP
    ACT --> REPO --> FB
    REPO --> CORE
    ACT --> AUTH
```

## 5.2 Patrón y principios

- **Repositorio**: aísla Firestore de la UI (interfaz simple, módulo profundo).
- **MVVM** en Informes (`ViewModel` + `LiveData` + estado de UI inmutable).
- **Lógica pura** extraída a clases sin dependencias → 85 pruebas unitarias.
- **`FirestoreSchema`** centraliza nombres de colección/campo (sin *magic strings*).

## 5.3 Flujo de una venta (secuencia)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant C as CobroActivity
    participant R as CobroRepository
    participant F as Firestore (transacción)
    U->>C: Confirmar cobro
    C->>C: Validar pago
    C->>R: registrarCobro(solicitud, usuario)
    R->>F: leer turno, contador, config, stock
    R->>F: crear venta + factura, +1 contador, liberar mesa, descontar stock
    F-->>R: OK (ids)
    R-->>C: onExito(ventaId, facturaId)
    C->>U: Ticket con QR Verifactu
```

## 5.4 Modo offline

La persistencia de Firestore permite operar sin red (comandas, mesas, productos,
movimientos se **encolan**). El **cobro y la rectificación** usan **transacción**,
que requiere conexión: así se evita numeración duplicada offline. El indicador de
Home refleja **Online / Sincronizando / Sin conexión**.

## 5.5 Despliegue

Ver [[Esquema - Despliegue]]: app Android (dispositivo/emulador) ↔ Firebase
(Auth, Firestore con reglas) en la nube; CI en GitHub Actions.
