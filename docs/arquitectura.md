# Arquitectura de SOFTBAR

SOFTBAR es una app Android nativa (Java) sobre Firebase. La arquitectura separa
tres responsabilidades para reducir la carga cognitiva: **UI**, **acceso a
datos (repositorios)** y **logica pura**. La dependencia siempre va hacia abajo;
la logica de negocio pura no depende de Android ni de Firebase, por eso se puede
probar con JUnit.

## Vista por capas

```mermaid
flowchart TD
    subgraph UI["Capa UI (Android)"]
        ACT["Activities\n(Home, Mesas, Comanda, Barra, Cobro,\nTicket, Caja, Informes, Stock, Historial,\nCierres, Config, Turno, Login, Splash)"]
        VM["InformesViewModel\n(LiveData + estado de UI)"]
        HELP["Helpers de UI\n(CatalogoCategorias, PersonalizacionLinea,\nHeader, ConexionUtil)"]
    end

    subgraph REPO["Capa de datos (repositorios)"]
        R1["CobroRepository"]
        R2["RectificacionRepository"]
        R3["InformesRepository"]
        R4["HistorialRepository"]
        R5["CierresRepository"]
        R6["StockRepository"]
        R7["UsuarioRepository"]
    end

    subgraph CORE["Logica pura + modelos (sin Android)"]
        L1["Dinero, CalculoIva,\nVentasPorProducto, ComparativaDias"]
        L2["ResumenCaja, IndicadoresVentas,\nCalculoTotalComanda, Permisos"]
        L3["Modelos: Producto, Venta, Factura,\nComanda, LineaComanda, Turno, Usuario..."]
        L4["Verifactu: HashVerifactu,\nGeneradorQrVerifactu"]
    end

    subgraph FB["Firebase"]
        AUTH["Firebase Auth"]
        FS["Cloud Firestore\n+ Security Rules"]
    end

    ACT --> VM
    ACT --> HELP
    ACT --> REPO
    VM --> REPO
    REPO --> FB
    REPO --> CORE
    ACT --> CORE
    ACT --> AUTH
```

## Principios aplicados

- **Modulos profundos, interfaz simple**: un repositorio esconde operaciones
  complejas tras una llamada sencilla. Por ejemplo, `CobroRepository.registrarCobro(...)`
  encapsula en una transaccion: numeracion fiscal, hash encadenado, calculo de
  IVA, creacion de venta y factura, liberacion de mesa y descuento de stock.
- **Logica pura aislada**: clases como `Dinero`, `CalculoIva`,
  `VentasPorProducto`, `ComparativaDias`, `Permisos` o `ResumenCaja` no dependen
  de Android, lo que permite 80 pruebas unitarias rapidas.
- **Seguridad en el servidor**: las reglas de Firestore validan cada coleccion,
  hacen inmutables ventas/facturas/movimientos, atan los registros a su autor y
  evitan la escalada de privilegios. Verificadas con 25 pruebas sobre el
  emulador.
- **Una sola forma de hacer las cosas**: el acceso a datos pasa por repositorios
  y los nombres de campo por `FirestoreSchema`, evitando *magic strings*.

## Flujo de una venta (resumen)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant C as CobroActivity
    participant R as CobroRepository
    participant F as Firestore (transaccion)

    U->>C: Confirmar cobro
    C->>C: Validar pago y conexion
    C->>R: registrarCobro(solicitud, usuario)
    R->>F: Leer turno, contador, config, stock
    R->>F: Crear venta + factura, actualizar contador,\nliberar mesa, descontar stock
    F-->>R: OK (ids)
    R-->>C: onExito(ventaId, facturaId)
    C->>U: Abrir ticket con QR Verifactu
```

## Colecciones Firestore

`mesas`, `productos`, `comandas`, `ventas`, `facturas`, `contadores`, `turnos`,
`movimientos_caja`, `usuarios`, `configuracion/fiscal`, `splash_backgrounds`.

Detalle de reglas y modelo en `firestore.rules`, `docs/firebase.md` y
`docs/verifactu.md`.
