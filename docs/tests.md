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

## Resumen actual

| Suite | Tests | Estado |
|---|---|---|
| ProductoTest | 3 | OK |
| VentaTest | 2 | OK |
| MesaTest | 4 | OK |
| EstadoMesaColorTest | 5 | OK |
| IndicadoresVentasTest | 7 | OK |
| HashVerifactuTest | 5 | OK |
| GeneradorQrVerifactuTest | 2 | OK |
| **Total** | **28** | **OK** |

(El proyecto incluye ademas el `ExampleUnitTest` autogenerado que suma 1 test mas.)

Validacion reciente:

- `testDebugUnitTest` ejecutado correctamente.
- Las pruebas unitarias actuales pasan.
- La primera ejecucion puede requerir acceso a la cache local de Gradle del usuario si el wrapper necesita bloquear o descargar la distribucion.

## Pendiente / fuera de alcance

- Pruebas instrumentadas de navegacion entre Activities (Espresso).
- Pruebas de integracion contra el emulador de Firestore.
- Pruebas de UI del dashboard (verificar que pinta la grafica correctamente).
- Generacion de bitmap del QR (requiere Android, no se prueba en JUnit puro).
- Pruebas del flujo completo de TPV: login, mesa, comanda, cobro, ticket e informes.
- Pruebas de concurrencia para la numeracion de facturas.
- Pruebas de errores de red y comportamiento offline.
