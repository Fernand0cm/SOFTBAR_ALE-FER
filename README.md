# SOFTBAR

SOFTBAR es una aplicacion Android nativa para un TPV de hosteleria orientado a bares y cafeterias. El objetivo del TFG es demostrar un flujo operativo completo: acceso de usuario, apertura de turno, sala y mesas, barra rapida, comanda, cobro, factura, ticket, caja, informes y configuracion del catalogo.

La app esta conectada a Firebase y mantiene la mayor parte de los datos operativos en Cloud Firestore.

## Estado Actual

- Proyecto Android nativo con modulo `app`.
- Java 11, `minSdk 28`, `targetSdk 36` y `compileSdk 36`.
- Interfaz basada en AppCompat y Material Components.
- Firebase Authentication para acceso con email y contrasena.
- Cloud Firestore como base de datos principal.
- Persistencia offline de Firestore activada al arrancar la app.
- Reglas e indices Firestore versionados en el repositorio.
- Indicador online/offline en la pantalla principal.
- Lector de codigos de barras con ML Kit / Google Code Scanner.
- Generacion de QR con ZXing para el bloque Verifactu.
- Graficas de informes con MPAndroidChart.
- Pruebas unitarias para modelos, calculos, caja, comandas, ventas, turnos y utilidades fiscales.

## Navegacion

### Mapa Principal

![Navegacion principal](docs/images/navigation-main.svg)

### Flujo De Venta

![Flujo de venta](docs/images/navigation-sale-flow.svg)

### Pantallas Y Firebase

![Pantallas y Firebase](docs/images/navigation-firebase.svg)

## Flujo Funcional Principal

El flujo minimo defendible queda asi:

```text
splash -> login -> home -> turno -> sala/barra -> comanda -> cobro -> venta -> factura -> ticket -> caja -> cierre
```

Cada venta queda asociada a:

- Usuario autenticado.
- Turno activo.
- Mesa o barra.
- Lineas vendidas.
- Metodo y desglose de pago.
- Factura generada.
- Ticket visible.

## Modulos Implementados

### Autenticacion Y Arranque

- Splash inicial con sesion activa o redireccion a login.
- Login con Firebase Authentication.
- Cierre de sesion desde Home.
- Email del usuario autenticado visible en la pantalla principal.
- Fondo de splash configurable desde `splash_backgrounds`.

### Home

Home permite entrar a:

- Apertura de turno.
- Sala y mesas.
- Barra rapida.
- Caja.
- Informes.
- Stock.
- Configuracion.

Tambien muestra el estado de conexion del dispositivo.

### Turnos

- Apertura de turno con importe inicial.
- Registro del usuario, fecha e importe inicial.
- Creacion automatica del movimiento de apertura.
- Bloqueo del cobro si no hay turno abierto.
- Cierre desde Caja con arqueo real.

### Sala Y Mesas

- Coleccion `mesas` en Firestore.
- Siembra inicial de 8 mesas si la coleccion esta vacia.
- Escucha en tiempo real de mesas ordenadas por numero.
- Estados: `libre`, `ocupada`, `cobro`, `cerrada`.
- Enlace de mesa con `comandaActivaId`.
- Al tocar una mesa se abre o recupera su comanda activa.

### Comanda

- Comanda persistida en Firestore.
- Catalogo de productos real cargado desde `productos`.
- Anadir productos desde grid.
- Agrupacion de productos repetidos aumentando cantidad.
- Quitar unidades desde la linea.
- Calculo de total desde lineas reales.
- Paso a Cobro con `comandaId`, mesa y total.

### Barra Rapida

- Venta directa sin mesa.
- Catalogo real desde Firestore.
- Lineas en memoria para pedido puntual.
- Calculo de total.
- Paso a Cobro con lineas reales.
- Generacion posterior de venta, factura y ticket igual que sala.

### Cobro

- Metodos disponibles:
  - Efectivo.
  - Tarjeta.
  - Mixto.
- Entrada real de pago en efectivo y tarjeta.
- Calculo de cambio.
- Validacion de que el importe pagado cubre el total.
- Bloqueo de doble pulsacion durante el guardado.
- Transaccion Firestore para crear venta, factura, contador y liberar mesa.
- Guardado de desglose:
  - `pagoEfectivo`
  - `pagoTarjeta`
  - `importeRecibido`
  - `cambio`

### Ticket Y Facturacion

- Ticket posterior al cobro.
- Carga de la venta y factura exactas por `ventaId` y `facturaId`.
- Visualizacion de:
  - Numero de factura.
  - Mesa, si aplica.
  - Lineas vendidas.
  - Total.
  - Metodo de pago.
  - Desglose efectivo/tarjeta/cambio.
  - QR Verifactu.
  - Hash parcial.
- Botones de imprimir y email presentes como acciones pendientes.

### Caja

- Caja asociada al turno activo.
- Resumen del turno:
  - Apertura.
  - Ventas en efectivo.
  - Ventas con tarjeta.
  - Retiradas.
  - Efectivo esperado.
- Movimientos manuales:
  - Apertura.
  - Entrada.
  - Retirada.
- Cierre de turno con:
  - Efectivo contado.
  - Efectivo esperado.
  - Diferencia de caja.
- Historico del cierre guardado en `turnos`.

### Informes

- Fuente principal: coleccion `ventas`.
- KPIs diarios:
  - Total vendido.
  - Numero de tickets.
  - Ticket medio.
- Grafica de ventas por hora.
- Calculos aislados en `IndicadoresVentas` para pruebas unitarias.

### Configuracion Y Catalogo

- Catalogo de productos en Firestore.
- Escaneo de codigo de barras.
- Dialogo de alta de producto con nombre y precio.
- Guardado usando el codigo de barras como identificador.
- Listado en tiempo real ordenado por nombre.

### Stock

- Pantalla navegable.
- Pendiente de implementar como modulo funcional completo.

## Firebase

### Servicios Usados

- Firebase Authentication.
- Cloud Firestore.
- Persistencia offline de Firestore.

No se usan actualmente:

- Firebase Storage.
- Cloud Functions.
- Hosting.
- Crashlytics.
- Analytics.

### Colecciones Principales

- `mesas`: estado operativo de la sala.
- `productos`: catalogo de productos.
- `comandas`: pedidos de mesa abiertos o pagados.
- `ventas`: ventas confirmadas.
- `facturas`: facturas simplificadas con hash y QR.
- `contadores`: numeracion anual y ultimo hash.
- `turnos`: apertura, cierre y arqueo.
- `movimientos_caja`: entradas, retiradas y aperturas.
- `configuracion/fiscal`: NIF/CIF y serie de facturacion.
- `splash_backgrounds`: imagenes opcionales del splash.

Mas detalle en:

- `docs/firebase.md`
- `docs/verifactu.md`
- `firestore.rules`
- `firestore.indexes.json`

## Facturacion Y Verifactu

La app genera una factura simplificada al confirmar cada cobro.

Incluye:

- Numeracion transaccional por anio.
- Serie fiscal configurable.
- NIF/CIF configurable en Firestore.
- Hash SHA-256 encadenado.
- QR de validacion en entorno de pruebas.
- Lineas reales de venta.
- Desglose de pago.

Documento recomendado de configuracion:

```text
configuracion/fiscal
```

Campos:

- `nifEmisor`
- `serie`

Si no existe, se usan valores por defecto para mantener la demo operativa.

## Modelo De Datos

Clases principales:

- `Mesa`
- `Producto`
- `Comanda`
- `LineaComanda`
- `Venta`
- `Turno`
- `MovimientoCaja`
- `ResumenCaja`
- `IndicadoresVentas`
- `EstadoMesaColor`

Clases fiscales:

- `Factura`
- `ConfiguracionFiscal`
- `HashVerifactu`
- `GeneradorQrVerifactu`

## Reglas Firestore

Las reglas actuales:

- Bloquean datos de negocio a usuarios no autenticados.
- Permiten lectura publica solo de `splash_backgrounds`.
- Validan campos basicos por coleccion.
- Bloquean borrados sensibles en mesas, comandas, ventas, facturas y turnos.
- Permiten crear y actualizar productos a usuarios autenticados.

Despliegue:

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

## Pruebas

Comando principal:

```bash
./gradlew testDebugUnitTest
```

En Windows:

```powershell
.\gradlew.bat testDebugUnitTest
```

Cobertura actual:

- Modelos de datos.
- Estados y colores de mesa.
- Calculo de total de comanda.
- Resumen de caja.
- Desglose de pagos.
- Indicadores de ventas.
- Hash Verifactu.
- URL de QR Verifactu.
- Configuracion fiscal.

Documentacion detallada:

- `docs/tests.md`

## Como Ejecutar La App

1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Confirmar que existe `app/google-services.json`.
4. Configurar Firebase si se usa otro proyecto.
5. Ejecutar el modulo `app` en emulador o dispositivo Android.

## Configuracion Firebase Rapida

```bash
firebase login
firebase use tfg-softba
firebase deploy --only firestore:rules,firestore:indexes
```

Usuario de prueba recomendado:

```text
fer@softbar.com
123456
```

Datos minimos para demo:

- Un turno abierto.
- 3-5 productos en catalogo.
- Una mesa con comanda.
- Una venta de barra.
- Un cierre de caja.

## Estructura Del Proyecto

```text
TFG_SOFTBAR/
|- app/
|  |- src/main/java/com/SOFTBAR_F_A/
|  |  |- data/
|  |  |- data/firebase/
|  |  |- data/repository/
|  |  |- data/verifactu/
|  |  |- ui/
|  |- src/main/res/
|  |- src/test/
|- docs/
|  |- images/
|- firestore-tests/
|- firebase.json
|- firestore.rules
|- firestore.indexes.json
|- build.gradle.kts
|- settings.gradle.kts
|- README.md
```

## Pendiente Prioritario

### Antes De La Defensa

- Crear guion de demo paso a paso.
- Preparar datos de prueba estables.
- Probar flujo completo manualmente:
  - login
  - abrir turno
  - crear productos
  - abrir mesa
  - anadir productos
  - cobrar
  - revisar ticket
  - revisar caja
  - cerrar turno
  - revisar informes
- Preparar capturas finales de pantallas reales.

### Funcionalidad Pendiente

- [x] Alta manual de producto sin escaner.
- [x] Edicion de productos desde el catalogo.
- Desactivacion de productos sin borrarlos.
- Categorias de catalogo.
- [x] IVA por producto (10%, 21%, 4%) con cuota por tipo en la factura.
- Notas y modificadores por linea.
- Historial de tickets.
- Consulta de cierres historicos.
- Filtros de informes por fecha, turno y metodo de pago.
- Ventas por producto.
- Roles de usuario.
- Stock real.
- Impresion o exportacion de ticket.
- Anulacion o rectificacion de factura.

### Robustez Tecnica

- [x] Separar el cobro en un repositorio (`data/repository/CobroRepository`).
- [x] Pantalla de informes en MVVM (`InformesViewModel` + `InformesRepository`).
- Extender el patron MVVM/repositorio al resto de pantallas (caja, comanda, mesas).
- Introducir capa de dominio para reglas de negocio.
- [x] Calculo monetario con `BigDecimal` y redondeo a centimos (`data/Dinero`).
- [x] Estados de carga, vacio y error en informes (pendiente en el resto).
- Crear pruebas instrumentadas de navegacion.
- [x] Probar las reglas con Firebase Emulator Suite (`firestore-tests/`).
- Revisar formato fiscal contra especificacion vigente antes de cualquier uso real.

## Roadmap Recomendado

1. Catalogo editable completo.
2. Comanda avanzada con notas, cantidades y modificadores.
3. Informes por fecha, turno, metodo y producto.
4. Historial de tickets y cierres.
5. Roles basicos.
6. Stock.
7. Exportacion/impresion.
8. Pruebas instrumentadas.
9. Guion y capturas finales de defensa.
