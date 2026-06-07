# Changelog

Todos los cambios relevantes de SOFTBAR. El formato sigue
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado
[SemVer](https://semver.org/lang/es/).

## [1.0.0] - 2026-06-07

Primera version completa del TPV, con el flujo operativo cerrado y endurecido.

### Anadido

- Roles de usuario (administrador, camarero, caja, cocina) con permisos por
  modulo y reglas Firestore anti-escalada de privilegios.
- IVA por producto (10/21/4%) con calculo de cuota por tipo en la factura.
- Comanda avanzada: cantidades, notas y modificadores por linea.
- Categorias de catalogo con filtro por chips en comanda y barra.
- Alta manual, edicion y desactivacion de productos (sin borrarlos).
- Control de stock opcional por producto, con descuento automatico al vender,
  devolucion al rectificar y alertas de bajo stock.
- Informes: filtros por fecha, turno y metodo; ventas por producto y mas
  vendidos; comparativa por dias; desglose base/IVA; exportacion del resumen.
- Historial de tickets con reapertura y consulta de cierres de caja.
- Rectificacion de facturas (factura rectificativa encadenada) segun el
  principio de Verifactu de no borrar ni modificar la original.
- Gestion del modo offline: estado de sincronizacion y bloqueo de cobro y
  rectificacion sin conexion para evitar numeracion duplicada.
- Integracion continua (GitHub Actions) con pruebas unitarias y de reglas.

### Cambiado

- Arquitectura: capa de repositorios para el acceso a datos y pantalla de
  informes en MVVM (ViewModel + LiveData + estados de UI).
- Calculo monetario con `BigDecimal` y redondeo a centimos.
- Reglas Firestore endurecidas: facturas, ventas y movimientos inmutables,
  `usuarioUid` ligado al usuario autenticado y contador de facturas monotono.

### Seguridad

- Pruebas de reglas con Firebase Emulator Suite (25 casos).
- Cobertura de logica pura con pruebas unitarias (80 casos).
