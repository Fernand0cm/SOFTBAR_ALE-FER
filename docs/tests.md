# Pruebas de SOFTBAR

Este documento describe las pruebas automatizadas del proyecto, donde viven y como ejecutarlas.

## Como lanzar las pruebas

Desde la raiz del proyecto:

```bash
./gradlew testDebugUnitTest
```

El reporte HTML queda en:

```
app/build/reports/tests/testDebugUnitTest/index.html
```

Y los resultados XML (formato JUnit) en:

```
app/build/test-results/testDebugUnitTest/
```

## Estrategia

Se prueban las clases de la capa `data` que contienen logica pura, sin
dependencias de Android ni de Firebase. Las Activities y la integracion con
Firestore quedan fuera del alcance de estas pruebas porque requieren
instrumentacion (Espresso, Robolectric o emulador) y se validan manualmente.

## Pruebas existentes

### `data/ProductoTest`

POJO del catalogo de productos.

| Prueba | Que verifica |
|---|---|
| `constructorVacio_dejaCamposPorDefecto` | El constructor sin argumentos (necesario para Firestore) deja los campos a null o cero. |
| `constructorConDatos_asignaTodo` | El constructor completo asigna codigo de barras, nombre y precio. |
| `setters_actualizanCampos` | Los setters individuales modifican el estado. |

### `data/VentaTest`

POJO de las ventas registradas en Firestore.

| Prueba | Que verifica |
|---|---|
| `constructorConDatos_asignaCampos` | Constructor completo asigna fecha, total y metodo. |
| `setters_actualizanCampos` | Los setters individuales actualizan los campos. |

### `data/MesaTest`

POJO de mesa para Firestore.

| Prueba | Que verifica |
|---|---|
| `constructorVacio_dejaCamposPorDefecto` | El constructor sin argumentos deja numero a 0 y resto a null. |
| `constructorConDatos_asignaNumeroYEstado` | Constructor (numero, estado) asigna ambos. |
| `setters_actualizanCampos` | Setters individuales actualizan numero, estado y comandaActivaId. |
| `constantesEstado_sonValoresEsperados` | Las constantes LIBRE/OCUPADA/COBRO/CERRADA tienen los strings esperados. |

### `data/EstadoMesaColorTest`

Mapeo del estado textual de una mesa al recurso de color de la paleta.

| Prueba | Que verifica |
|---|---|
| `libre_devuelveColorLibre` | "libre" -> R.color.mesa_libre. |
| `ocupada_devuelveColorOcupada` | "ocupada" -> R.color.mesa_ocupada. |
| `cobro_devuelveColorCobro` | "cobro" -> R.color.mesa_cobro. |
| `cerrada_devuelveColorCerrada` | "cerrada" -> R.color.mesa_cerrada. |
| `estadoNuloODesconocido_caeAColorCerrada` | null o estado raro -> color cerrada (fallback seguro). |

### `data/LineaComandaTest`

POJO de cada linea individual dentro de una comanda.

| Prueba | Que verifica |
|---|---|
| `constructorVacio_dejaCamposPorDefecto` | Constructor sin argumentos deja campos a null y numericos a 0. |
| `constructorConDatos_asignaTodo` | Constructor completo asigna codigo, nombre, precio y cantidad. |
| `subtotal_multiplicaPrecioPorCantidad` | El subtotal es precio x cantidad. |
| `subtotal_conCantidadCero_devuelveCero` | Cantidad 0 da subtotal 0 sin lanzar excepcion. |

### `data/ComandaTest`

POJO de la comanda asociada a una mesa.

| Prueba | Que verifica |
|---|---|
| `constructorVacio_dejaCamposPorDefecto` | Constructor por defecto deja lista de lineas vacia y campos a null. |
| `constructorConMesa_creaComandaAbierta` | Constructor (mesaId, numero) crea comanda en estado ABIERTA con fecha. |
| `setLineas_nulo_dejaListaVaciaEnVezDeNull` | Pasar null a setLineas mantiene una lista vacia (defensa contra NPE). |
| `constantesEstado_sonValoresEsperados` | Las constantes ABIERTA y PAGADA tienen los valores esperados. |

### `data/CalculoTotalComandaTest`

Calculos puros sobre las lineas de una comanda.

| Prueba | Que verifica |
|---|---|
| `total_listaVacia_devuelveCero` | El total sobre lista vacia es 0. |
| `total_listaNula_devuelveCero` | El total sobre null es 0 (no lanza). |
| `total_sumaSubtotalesCorrectamente` | Suma exacta de varios subtotales con cantidades distintas. |
| `numeroArticulos_sumaCantidades` | El conteo de articulos suma las cantidades de cada linea. |
| `numeroArticulos_listaNula_devuelveCero` | Conteo sobre null devuelve 0. |

### `data/MovimientoCajaTest`

POJO de los movimientos manuales de caja del turno.

| Prueba | Que verifica |
|---|---|
| `constructorVacio_dejaCamposPorDefecto` | Constructor por defecto deja campos a null/0. |
| `constructorConDatos_asignaTodo` | Constructor (fecha, tipo, importe, descripcion) asigna correctamente. |
| `constantesTipo_sonValoresEsperados` | APERTURA, RETIRADA y ENTRADA tienen los valores esperados. |

### `data/ResumenCajaTest`

Calculos del estado de la caja a partir de ventas y movimientos del turno.

| Prueba | Que verifica |
|---|---|
| `totalEsperado_aperturaMasVentasMenosRetiradas` | La formula global del total esperado es correcta. |
| `calcular_listasNulas_devuelveCero` | Listas nulas no rompen el calculo y devuelven 0. |
| `calcular_separaEfectivoYTarjeta` | Separa correctamente las ventas por metodo de pago. |
| `calcular_mixtoCuentaComoEfectivo` | Los pagos "Mixto" u otros se agrupan como efectivo en la caja. |
| `calcular_sumaMovimientosPorTipo` | Apertura, entradas y retiradas se acumulan en los campos correctos. |
| `calcular_retiradaConImporteNegativo_seToma_enValorAbsoluto` | Una retirada negativa se interpreta correctamente. |

### `data/IndicadoresVentasTest`

Calculos del dashboard de informes (suma, ticket medio, distribucion horaria).

| Prueba | Que verifica |
|---|---|
| `total_listaVacia_devuelveCero` | La suma sobre lista vacia es 0. |
| `total_sumaCorrectamente` | Suma exacta de varios totales. |
| `numeroTickets_cuentaElementos` | Cuenta correcta de elementos. |
| `ticketMedio_listaVacia_devuelveCero` | No divide por cero con lista vacia. |
| `ticketMedio_calculaPromedio` | Promedio correcto sobre 2 ventas. |
| `ventasPorHora_distribuyeEnIndicesCorrectos` | El array de 24 posiciones acumula importes en la hora correcta. |
| `ventasPorHora_ignoraVentasSinFecha` | Las ventas sin fecha no rompen el calculo. |

### `data/verifactu/HashVerifactuTest`

Calculo del hash SHA-256 encadenado de las facturas Verifactu.

| Prueba | Que verifica |
|---|---|
| `calcular_devuelveHashHexadecimalDe64Caracteres` | El hash tiene la longitud y formato esperados de SHA-256. |
| `calcular_esDeterminista` | Misma entrada produce mismo hash. |
| `calcular_cambiaSiCambiaUnImporte` | Cualquier alteracion de los datos cambia el hash. |
| `calcular_encadenaConHashAnterior` | Incluir el hash de la factura previa cambia el resultado (cadena). |
| `formatoImporte_redondeaA2Decimales` | Los importes se serializan con 2 decimales fijos. |

### `data/verifactu/GeneradorQrVerifactuTest`

Construccion de la URL del QR Verifactu de la AEAT.

| Prueba | Que verifica |
|---|---|
| `construirUrl_contieneEndpointYParametrosClave` | URL apunta al endpoint correcto e incluye nif, fecha e importe. |
| `construirUrl_codificaCaracteresEspeciales` | El "/" del numero de serie se URL-encodea como `%2F`. |

### `data/DineroTest`

Calculo monetario con BigDecimal y redondeo a centimos.

| Prueba | Que verifica |
|---|---|
| `redondear_aDosDecimalesHalfUp` | Redondeo HALF_UP a 2 decimales. |
| `sumar_evitaErrorDeComaFlotante` | `0.1 + 0.2` da exactamente `0.30`. |
| `sumar_variosImportes` | Suma de varios importes. |
| `restar_redondeaAResultadoLimpio` | Resta sin deriva de coma flotante. |
| `multiplicar_precioPorCantidad` | Precio por cantidad redondeado. |
| `cuotaIvaIncluido_diez_porciento` | Cuota de IVA contenida en un total. |
| `cuotaIvaIncluido_redondeaADosDecimales` | La cuota se redondea a 2 decimales. |

### `data/CalculoIvaTest`

Cuota de IVA de una venta con tipos mixtos por linea.

| Prueba | Que verifica |
|---|---|
| `cuotaTotal_listaNula_devuelveCero` | Lista nula devuelve 0. |
| `cuotaTotal_listaVacia_devuelveCero` | Lista vacia devuelve 0. |
| `cuotaTotal_unaLineaAlDiezPorCiento` | Cuota de una linea al 10%. |
| `cuotaTotal_tiposMixtos_sumaCadaCuota` | Suma cuotas de lineas al 10% y al 21%. |
| `cuotaTotal_respetaCantidadDeLaLinea` | Usa el subtotal (precio x cantidad) de la linea. |

## Resumen actual

| Suite | Tests | Estado |
|---|---|---|
| ProductoTest | 3 | OK |
| VentaTest | 2 | OK |
| MesaTest | 4 | OK |
| EstadoMesaColorTest | 5 | OK |
| LineaComandaTest | 4 | OK |
| ComandaTest | 4 | OK |
| CalculoTotalComandaTest | 5 | OK |
| MovimientoCajaTest | 3 | OK |
| ResumenCajaTest | 6 | OK |
| IndicadoresVentasTest | 7 | OK |
| HashVerifactuTest | 5 | OK |
| GeneradorQrVerifactuTest | 2 | OK |
| DineroTest | 7 | OK |
| CalculoIvaTest | 5 | OK |
| **Total** | **62** | **OK** |

(El proyecto incluye ademas el `ExampleUnitTest` autogenerado que suma 1 test mas.)

Validacion reciente:

- `testDebugUnitTest` ejecutado correctamente.
- Las pruebas unitarias actuales pasan.
- La primera ejecucion puede requerir acceso a la cache local de Gradle del usuario si el wrapper necesita bloquear o descargar la distribucion.

## Pruebas de reglas Firestore (emulador)

Ademas de las pruebas unitarias de Java, el proyecto incluye una suite que valida
las reglas de seguridad de Firestore contra el emulador oficial. Vive en
`firestore-tests/` y usa `@firebase/rules-unit-testing` con el runner nativo de
Node.

Como ejecutarlas:

```bash
cd firestore-tests
npm install
npm test
```

`npm test` arranca el emulador con `firebase emulators:exec --only firestore` y
corre `rules.test.js`. Cubre las garantias criticas del modelo fiscal:

| Prueba | Que verifica |
|---|---|
| Lectura sin auth | Un usuario no autenticado no puede leer ventas. |
| Escritura sin auth | Un usuario no autenticado no puede crear productos. |
| Splash publico | `splash_backgrounds` es de lectura publica. |
| Venta propia | Un usuario puede crear una venta con su propio uid. |
| Venta ajena | Un usuario NO puede crear una venta atribuida a otro uid. |
| Venta inmutable (update) | Una venta no se puede modificar tras crearse. |
| Venta inmutable (delete) | Una venta no se puede borrar. |
| Factura inmutable | Una factura no admite update tras crearse. |
| Factura valida | Una factura con datos correctos se crea. |
| Contador monotono | El contador de facturas solo puede avanzar. |
| Movimiento inmutable | Un movimiento de caja no se puede modificar. |
| Producto invalido | Un producto con precio negativo es rechazado. |
| Producto con IVA valido | Un producto con `tipoIva` correcto se acepta. |
| Producto con IVA invalido | Un `tipoIva` fuera de rango (0..1) es rechazado. |

Estado: **14 pruebas, todas OK** sobre el emulador de Firestore.

## Pendiente / fuera de alcance

- Pruebas instrumentadas de navegacion entre Activities (Espresso).
- Pruebas de UI del dashboard (verificar que pinta la grafica correctamente).
- Generacion de bitmap del QR (requiere Android, no se prueba en JUnit puro).
- Pruebas del flujo completo de TPV: login, mesa, comanda, cobro, ticket e informes.
- Pruebas de concurrencia para la numeracion de facturas.
- Pruebas de errores de red y comportamiento offline.
