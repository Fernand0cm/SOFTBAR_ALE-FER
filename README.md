# SOFTBAR

SOFTBAR es una aplicacion Android nativa para un TPV de hosteleria. El objetivo del TFG es cubrir la operativa real de un bar o cafeteria: acceso de empleados, sala y mesas, comandas, cobro, ticket/factura, caja, catalogo, informes, stock y configuracion del negocio.

El proyecto empezo como una base de TPV y actualmente ya ha evolucionado hacia una app conectada con Firebase. La rama `Fernando` ya esta integrada en `origin/main`, por lo que el avance descrito aqui corresponde al estado actual del repositorio.

## Estado actual

- Proyecto Android nativo con modulo `app`.
- Java 11, `minSdk 28`, `targetSdk 36` y `compileSdk 36`.
- Interfaz basada en AppCompat y Material Components.
- Firebase Auth para acceso con email y contrasena.
- Cloud Firestore como base de datos principal.
- Persistencia offline de Firestore activada al arrancar la app.
- Reglas Firestore versionadas en el repositorio.
- Indicador de conexion online/offline en la pantalla principal.
- Lector de codigos de barras con ML Kit / Google Code Scanner.
- Generacion de QR con ZXing para la parte Verifactu.
- Graficas de informes con MPAndroidChart.
- Pruebas unitarias de modelos, calculos e utilidades Verifactu.

## Avances realizados

### Autenticacion y arranque

- Pantalla de splash que decide si abrir login o home segun la sesion activa de Firebase.
- Pantalla de login con validacion basica de email y contrasena.
- Cierre de sesion desde home.
- Visualizacion del email del usuario autenticado.

### Navegacion principal

- Home con acceso a los modulos principales:
  - Apertura de turno.
  - Sala y mesas.
  - Barra rapida.
  - Caja.
  - Informes.
  - Stock.
  - Configuracion.
- Cabecera reutilizable con titulo, subtitulo opcional y boton de volver.
- Redisenio visual con paleta, estilos, iconos y pantallas mas consistentes.

### Sala y mesas

- Coleccion Firestore `mesas`.
- Siembra inicial de 8 mesas cuando la coleccion esta vacia.
- Escucha en tiempo real de las mesas ordenadas por numero.
- Estados base de mesa:
  - `libre`
  - `ocupada`
  - `cobro`
  - `cerrada`
- Mapeo de estados a colores mediante `EstadoMesaColor`.
- Al pulsar una mesa libre se marca como ocupada y se abre la comanda.

### Comanda

- Pantalla de comanda asociada a mesa mediante extras de `Intent`.
- Visualizacion del numero de mesa.
- Navegacion hacia modificadores/extras.
- Navegacion hacia cobro.
- La UI ya contiene estructura de lineas y total, pero todavia no esta conectada a productos reales.

### Cobro y venta

- Pantalla de cobro con seleccion de metodo:
  - Efectivo.
  - Tarjeta.
  - Mixto.
- Entrada real de efectivo y tarjeta, con cambio calculado.
- Registro de venta con desglose de pago en la coleccion Firestore `ventas`.
- Generacion de una factura simplificada en la coleccion `facturas`.
- Encadenado de hash SHA-256 con la factura anterior.
- Calculo basico de cuota de IVA al 10%.
- Generacion de URL de validacion Verifactu en entorno de pruebas de AEAT.
- Configuracion fiscal leida desde Firestore cuando existe.

### Ticket y Verifactu

- Pantalla de ticket posterior al cobro.
- Carga de la venta y factura exactas del cobro.
- Visualizacion de:
  - Numero de factura.
  - Lineas vendidas.
  - Total, metodo y desglose de pago.
  - Hash parcial.
  - QR Verifactu generado como bitmap.
- Botones de imprimir y email presentes, actualmente pendientes de implementacion real.

### Caja

- Pantalla visual de caja del turno.
- Resumen y movimientos representados en UI.
- Movimientos manuales de entrada y retirada asociados al turno.
- Cierre de turno con efectivo contado, efectivo esperado y diferencia de caja.

### Informes

- Coleccion Firestore `ventas` como fuente.
- KPIs diarios:
  - Total vendido.
  - Numero de tickets.
  - Ticket medio.
- Grafica de ventas por hora para el rango 08:00-23:00.
- Calculos aislados en `IndicadoresVentas` para poder probarlos con JUnit.

### Catalogo y configuracion

- Pantalla de configuracion centrada en catalogo de productos.
- Escaneo de codigo de barras.
- Dialogo para introducir nombre y precio.
- Guardado de productos en Firestore usando el codigo de barras como identificador.
- Listado en tiempo real de productos ordenados por nombre.

### Pantallas placeholder

Existen pantallas ya navegables pero pendientes de desarrollo funcional:

- Apertura de turno.
- Barra rapida.
- Modificadores.
- Stock.

## Modelo de datos actual

Clases principales en `app/src/main/java/com/SOFTBAR_F_A/data`:

- `Mesa`: numero, estado y `comandaActivaId`.
- `Producto`: codigo de barras, nombre y precio.
- `Venta`: fecha, total, metodo, desglose de pago, turno, usuario y lineas.
- `IndicadoresVentas`: calculos de total, numero de tickets, ticket medio y distribucion por hora.
- `EstadoMesaColor`: mapeo de estados de mesa a colores.

Clases Verifactu en `data/verifactu`:

- `Factura`: numero, fecha, NIF emisor, total, cuota IVA, desglose de pago, hash anterior, hash actual y URL de validacion.
- `HashVerifactu`: hash SHA-256 encadenado.
- `GeneradorQrVerifactu`: URL de validacion y bitmap QR.

## Colecciones Firestore usadas

- `mesas`: estado visual y operativo de la sala.
- `productos`: catalogo basico escaneado por codigo de barras.
- `ventas`: ventas registradas al confirmar cobro.
- `facturas`: facturas simplificadas con hash y QR Verifactu.
- `configuracion/fiscal`: NIF/CIF y serie de facturacion.
- `contadores`: numeracion e hash de la ultima factura emitida.
- `movimientos_caja`: entradas, retiradas y aperturas manuales de caja.
- `turnos`: apertura y cierre del turno activo.
- `splash_backgrounds`: imagenes opcionales para el splash.

## Pruebas

La documentacion detallada de pruebas esta en `docs/tests.md`.

Comando principal:

```bash
./gradlew testDebugUnitTest
```

Estado validado:

- Las pruebas unitarias pasan correctamente.
- Hay cobertura sobre modelos de datos, colores de mesa, indicadores de ventas, hash Verifactu y URL de QR Verifactu.
- Quedan fuera de estas pruebas las Activities, UI, integracion real con Firestore y flujos de navegacion.

## Pendiente prioritario

### Documentacion y coherencia del proyecto

- [ ] Mantener este README actualizado conforme avance el TFG.
- [ ] Documentar la arquitectura real por capas: `ui`, `data`, futuras capas `domain`/`repository`.
- [x] Documentar configuracion de Firebase para poder levantar el proyecto en otro equipo.
- [x] Documentar estructura de colecciones Firestore y campos esperados.
- [x] Documentar reglas de seguridad de Firestore cuando existan.
- [ ] Crear un guion de demo del TFG: login, mesas, comanda, cobro, ticket QR, informes y catalogo.
- [ ] Decidir si el backend definitivo sera Firebase o Supabase. El codigo actual usa Firebase; cualquier referencia antigua a Supabase debe considerarse obsoleta salvo decision contraria.

### Seguridad y configuracion

- [ ] Revisar si `app/google-services.json` debe permanecer versionado o gestionarse por entorno.
- [ ] Crear reglas de seguridad Firestore por usuario, negocio y rol.
- [x] Evitar que usuarios no autenticados lean o escriban datos de negocio.
- [ ] Incorporar roles: administrador, camarero, caja y cocina.
- [ ] Mover datos fiscales y configuracion del negocio fuera del codigo.
- [ ] Sustituir el NIF emisor hardcodeado por configuracion editable.
- [ ] Validar datos de entrada de forma consistente en todas las pantallas.

### Arquitectura y mantenibilidad

- [ ] Separar acceso a Firestore en repositorios.
- [ ] Evitar consultas y escrituras directas desde Activities cuando el flujo crezca.
- [ ] Introducir una capa de dominio para reglas de negocio: cobro, cierre de mesa, caja, stock y facturacion.
- [x] Centralizar nombres de colecciones y campos Firestore.
- [ ] Definir modelos para comanda, linea de comanda, pago, turno, movimiento de caja y stock.
- [ ] Mejorar gestion de errores: ahora algunos listeners ignoran `error`.
- [ ] Anadir estados de carga, vacio y error en pantallas con datos remotos.

### Sala, mesas y comandas

- [x] Crear comandas reales en Firestore.
- [x] Enlazar `Mesa.comandaActivaId` con la comanda abierta.
- [ ] Cargar productos reales en la comanda.
- [ ] Permitir anadir, quitar y modificar cantidades de productos.
- [ ] Permitir notas por linea.
- [ ] Permitir extras/modificadores por producto.
- [ ] Calcular subtotal, impuestos y total desde las lineas reales.
- [ ] Persistir lineas de comanda.
- [ ] Recuperar una comanda abierta al volver a entrar en una mesa ocupada.
- [ ] Gestionar estados de mesa durante todo el flujo: libre, ocupada, pendiente de cobro y cerrada.
- [ ] Liberar mesa al cerrar ticket o finalizar comanda.
- [ ] Implementar cambio de mesa.
- [ ] Implementar union de mesas.
- [ ] Implementar division de cuenta.
- [ ] Implementar anulacion/cancelacion de comanda con permisos.

### Barra rapida

- [ ] Implementar flujo de venta sin mesa.
- [ ] Reutilizar catalogo y lineas de comanda.
- [ ] Permitir cobro directo.
- [ ] Registrar venta como barra/takeaway para informes.
- [ ] Diferenciar pedidos de barra y sala en el modelo.

### Cobro, ticket y facturacion

- [ ] Sustituir el total mock por el total real de la comanda.
- [x] Pasar `ventaId` y `facturaId` al ticket.
- [x] Esperar a que venta y factura se guarden correctamente antes de abrir ticket.
- [x] Controlar errores durante el cobro y evitar dobles cobros por doble pulsacion.
- [x] Implementar pagos parciales y mixtos reales.
- [x] Calcular cambio para efectivo.
- [x] Guardar desglose de pagos por metodo.
- [x] Guardar lineas reales en el ticket/factura.
- [ ] Implementar impresion real.
- [ ] Implementar envio por email o compartir ticket.
- [ ] Generar PDF o imagen del ticket si se necesita para entrega o uso real.
- [ ] Revisar Verifactu contra especificacion oficial completa.
- [x] Usar transacciones para numeracion de facturas y evitar duplicados.
- [ ] Gestionar series de factura por anio o configuracion del negocio.
- [ ] Preparar entorno de pruebas y produccion para QR/validacion.

### Caja y turnos

- [x] Implementar apertura de turno con importe inicial.
- [x] Asociar ventas al turno activo.
- [x] Registrar movimientos manuales: entradas, salidas, retiradas y ajustes.
- [x] Calcular ventas por metodo de pago dentro del turno.
- [x] Implementar cierre de turno y arqueo.
- [x] Registrar diferencia de caja.
- [x] Bloquear cobros si no hay turno abierto, si esta regla aplica al negocio.
- [ ] Permitir consulta de cierres historicos.
- [ ] Exportar o imprimir resumen de cierre.

### Informes

- [ ] Anadir filtros por fecha.
- [ ] Anadir filtros por turno.
- [ ] Anadir filtros por usuario/camarero.
- [ ] Anadir filtros por metodo de pago.
- [ ] Anadir ventas por producto y categoria.
- [ ] Anadir productos mas vendidos.
- [ ] Anadir comparativa por dias.
- [ ] Mostrar impuestos y bases imponibles.
- [ ] Gestionar zonas horarias y limites de dia de forma explicita.
- [ ] Anadir estados de error si Firestore falla.

### Catalogo, productos y modificadores

- [ ] Permitir crear producto manualmente sin escaner.
- [ ] Permitir editar producto existente.
- [ ] Permitir activar/desactivar productos.
- [ ] Permitir borrar productos con confirmacion.
- [ ] Validar codigos duplicados y nombres vacios.
- [ ] Validar precios negativos o con formato incorrecto.
- [ ] Anadir categorias de producto.
- [ ] Anadir IVA por producto o categoria.
- [ ] Anadir disponibilidad.
- [ ] Implementar modificadores/extras con precio opcional.
- [ ] Asociar modificadores a productos o categorias.
- [ ] Preparar catalogo para uso rapido en comanda/barra.

### Stock

- [ ] Definir modelo de stock.
- [ ] Registrar entradas y salidas manuales.
- [ ] Descontar stock por ventas si aplica.
- [ ] Establecer stock minimo y alertas.
- [ ] Mostrar inventario actual.
- [ ] Relacionar productos de venta con articulos de stock cuando no sean equivalentes.

### Offline y sincronizacion

- [ ] Mostrar no solo conexion, sino estado de sincronizacion pendiente.
- [ ] Gestionar conflictos entre dispositivos.
- [ ] Evitar numeracion duplicada de facturas offline.
- [ ] Definir que acciones se permiten sin conexion.
- [ ] Mostrar errores de sincronizacion al usuario.
- [ ] Probar escenarios offline reales en dispositivo/emulador.

### UI y experiencia de uso

- [ ] Revisar pantallas en tablet horizontal, tablet vertical y movil.
- [ ] Asegurar que los botones principales son comodos para uso en barra.
- [ ] Mejorar feedback de seleccion de metodo de pago.
- [ ] Evitar textos cortados en pantallas pequenas.
- [ ] Anadir confirmaciones para acciones destructivas.
- [ ] Mejorar accesibilidad basica: contraste, tamanos y labels.
- [ ] Sustituir placeholders por pantallas funcionales o mensajes de avance para la entrega.

### Pruebas y calidad

- [ ] Anadir pruebas unitarias para nuevos modelos de comanda, pagos, caja y stock.
- [ ] Anadir pruebas de numeracion de factura con casos concurrentes.
- [ ] Anadir pruebas instrumentadas de navegacion entre Activities.
- [ ] Anadir pruebas de integracion con Firebase Emulator Suite.
- [ ] Probar flujo completo: login -> mesa -> comanda -> cobro -> ticket -> informes.
- [ ] Anadir GitHub Actions para ejecutar tests en cada pull request.
- [ ] Revisar warnings de lint y accesibilidad.
- [ ] Preparar una bateria de pruebas manuales para la defensa del TFG.

## Roadmap recomendado

El planning funcional completo esta en `docs/planning_funcional.md`.

### Fase 1. Cerrar flujo minimo real

- Comanda persistida.
- Productos reales en comanda.
- Total real.
- Cobro de comanda.
- Ticket asociado a la venta exacta.
- Mesa liberada al finalizar.

### Fase 2. Caja y turnos

- Apertura de turno.
- Ventas asociadas al turno.
- Movimientos de caja.
- Cierre y arqueo.

### Fase 3. Catalogo completo e informes

- Categorias, edicion de productos y modificadores.
- Informes por fecha, turno y producto.
- Exportacion o vista historica.

### Fase 4. Robustez para entrega

- Reglas Firestore.
- Pruebas instrumentadas.
- Documentacion tecnica.
- Guion de demo.
- Revision Verifactu y facturacion.

## Como ejecutar la app

1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Configurar Firebase si se usa otro proyecto.
4. Ejecutar el modulo `app` en un emulador o dispositivo Android.

## Estructura del proyecto

```text
TFG_SOFTBAR/
|- app/
|  |- src/main/java/com/SOFTBAR_F_A/
|  |  |- data/
|  |  |- data/verifactu/
|  |  |- ui/
|  |- src/main/res/
|  |- src/test/
|- docs/
|- gradle/
|- build.gradle.kts
|- settings.gradle.kts
|- README.md
```
