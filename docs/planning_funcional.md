# Planning funcional de SOFTBAR

Este documento recoge el plan de trabajo recomendado para convertir la app en un TPV funcional minimo defendible para el TFG.

## Diagnostico actual

La app ya cubre una demo avanzada del flujo principal:

- Login con Firebase Auth.
- Sala con mesas en Firestore.
- Catalogo de productos.
- Comandas asociadas a mesa.
- Cobro basico.
- Venta y factura en Firestore.
- Ticket con QR Verifactu.
- Caja e informes con datos reales.

El punto critico es que algunas reglas de negocio todavia estan simplificadas. Para una demo de TFG es una base fuerte; para un uso real faltan turnos, caja cerrada, permisos, pagos completos, facturacion mas robusta, stock y control de errores.

## Objetivo funcional minimo

El objetivo principal debe ser cerrar este flujo sin incoherencias:

```text
login -> turno -> mesa/barra -> comanda -> cobro -> venta -> factura -> ticket -> caja -> informe
```

Cada venta debe quedar asociada a:

- Usuario autenticado.
- Turno activo.
- Mesa o barra.
- Lineas vendidas.
- Metodo o desglose de pago.
- Factura/ticket generado.

## Fase 1. Flujo minimo real

Prioridad alta.

Tareas:

- [x] Guardar lineas reales dentro de `ventas`.
- [x] Guardar lineas reales dentro de `facturas`.
- [x] Pasar `ventaId` y `facturaId` al ticket.
- [x] Mostrar en ticket la venta exacta, no datos de ejemplo.
- Confirmar que mesa, comanda, venta y factura quedan sincronizadas.
- [x] Evitar dobles cobros con boton deshabilitado y control de estado.
- [x] Mostrar error claro si falla Firestore durante el cobro.

Resultado esperado:

- Se puede abrir una mesa, anadir productos, cobrar y ver un ticket coherente.
- Firestore conserva toda la informacion necesaria para auditar la venta.

## Fase 2. Turnos y caja

Prioridad alta.

Tareas:

- [x] Implementar apertura de turno con importe inicial.
- [x] Guardar `turnoId`, usuario y fecha de apertura.
- [x] Bloquear cobros si no hay turno activo.
- [x] Asociar cada venta al turno activo.
- [x] Registrar entradas, retiradas y ajustes de caja.
- [x] Implementar cierre de turno con efectivo contado.
- [x] Calcular diferencia entre efectivo esperado y contado.
- [x] Guardar cierre historico.

Resultado esperado:

- La pantalla de caja deja de ser solo resumen diario y pasa a representar el turno real.
- El cierre de caja puede usarse como prueba funcional en la defensa.

## Fase 3. Cobro completo

Prioridad alta.

Tareas:

- [x] Introducir importe recibido en efectivo.
- [x] Calcular cambio real.
- [x] Implementar pago mixto con desglose:
  - efectivo
  - tarjeta
- [x] Validar que el importe pagado cubre el total.
- [x] Guardar desglose de pagos en la venta.
- [x] Mostrar metodo y desglose en ticket.

Resultado esperado:

- El cobro refleja casos reales de barra y sala.
- Caja e informes pueden separar efectivo, tarjeta y mixto.

## Fase 4. Catalogo y comanda

Prioridad media-alta.

Tareas:

- [x] Permitir alta manual de producto sin escaner.
- [x] Editar productos existentes.
- [x] Desactivar productos sin borrarlos.
- Crear categorias:
  - cafes
  - bebidas
  - tapas
  - bocadillos
- [x] Definir IVA por producto (10%, 21%, 4%); la factura calcula la cuota por tipo.
- Modificar cantidades desde la comanda.
- Anadir notas por linea.
- Preparar modificadores:
  - sin hielo
  - extra queso
  - pan sin gluten
  - punto de carne

Resultado esperado:

- La comanda se parece mas a la operativa real de hosteleria.
- La barra rapida puede organizarse por categorias.

## Fase 5. Informes

Prioridad media.

Tareas:

- Filtro por fecha.
- Filtro por turno.
- Filtro por metodo de pago.
- Ventas por producto.
- Productos mas vendidos.
- Comparativa por dias.
- Mostrar base imponible, IVA y total.
- Exportar resumen en formato compartible.

Resultado esperado:

- Informes deja de ser solo resumen del dia y sirve para analizar actividad.

## Fase 6. Usuarios y permisos

Prioridad media.

Tareas:

- Crear modelo de usuario interno.
- Asignar roles:
  - administrador
  - camarero
  - caja
  - cocina
- Guardar usuario responsable en ventas, movimientos y cierres.
- Limitar anulaciones y cambios sensibles a perfiles autorizados.
- Refinar reglas Firestore por coleccion y rol.

Resultado esperado:

- La app puede defender un modelo de seguridad mas realista.

## Fase 7. Stock

Prioridad media-baja para el flujo principal, alta si se quiere destacar gestion interna.

Tareas:

- Crear modelo de articulo de stock.
- Registrar entradas y salidas.
- Definir stock minimo.
- Mostrar alertas.
- Relacionar productos de venta con articulos de inventario.
- Descontar stock por venta cuando aplique.

Resultado esperado:

- Stock deja de ser placeholder y se integra con ventas.

## Fase 8. Facturacion y Verifactu

Prioridad alta para defensa tecnica, con alcance controlado.

Tareas:

- [x] Separar configuracion fiscal del codigo.
- [x] Guardar datos fiscales del negocio en Firestore.
- [x] Gestionar series por anio.
- Preparar anulacion o rectificacion.
- Revisar formato de QR y hash contra especificacion oficial.
- [x] Documentar limitaciones del prototipo.

Resultado esperado:

- La parte fiscal queda presentada como prototipo tecnico serio, sin prometer cumplimiento completo si no esta validado.

## Fase 9. Offline y sincronizacion

Prioridad media.

Tareas:

- Mostrar estado pendiente de sincronizar.
- Definir operaciones permitidas sin conexion.
- Evitar numeracion duplicada de facturas offline.
- Avisar de errores de sincronizacion.
- Probar corte de red en emulador o dispositivo.

Resultado esperado:

- La app aprovecha la persistencia offline de Firestore de forma controlada.

## Fase 10. Pruebas y defensa

Prioridad alta antes de entregar.

Tareas:

- Crear guion de demo:
  - login
  - apertura de turno
  - alta de productos
  - mesa
  - comanda
  - cobro
  - ticket
  - caja
  - informes
- Anadir pruebas unitarias de pagos, caja y turnos.
- Anadir pruebas de numeracion de factura.
- Probar flujo completo manualmente.
- Preparar capturas de pantalla finales.
- Documentar arquitectura y colecciones Firebase.

Resultado esperado:

- La defensa puede seguir un recorrido claro y repetible.

## Orden recomendado

1. Turno activo.
2. Venta y factura con lineas reales.
3. Ticket asociado a venta exacta.
4. Caja por turno.
5. Pago efectivo/tarjeta/mixto real.
6. Informes por fecha y turno.
7. Roles basicos.
8. Catalogo editable.
9. Modificadores.
10. Stock.

## Ideas de mejora con buena relacion esfuerzo-impacto

- Boton para crear productos manualmente.
- Datos de demo precargados para la defensa.
- [x] Vista de historial de tickets.
- Compartir ticket como texto o PDF.
- Pantalla de cierre de caja imprimible.
- Vista de cocina con comandas pendientes.
- Barra rapida por categorias.
- [x] Confirmaciones antes de acciones sensibles (cierre de turno, cierre de sesion).
- Exportacion de informes.

## Riesgos principales

- Facturas duplicadas si se cobra sin transaccion o sin control offline.
- Datos incompletos si ventas no guardan lineas.
- Caja incorrecta si las ventas no se asocian a turno.
- Seguridad insuficiente si todos los usuarios pueden hacer todo.
- Defensa confusa si no hay guion y datos de prueba preparados.

## Criterio de app funcional minima

La app puede considerarse funcional para el TFG cuando permita:

- Iniciar sesion.
- Abrir turno.
- Crear o seleccionar productos.
- Abrir mesa o venta de barra.
- Anadir productos a una comanda.
- Cobrar con metodo de pago.
- Generar venta, factura y ticket.
- Ver caja del turno.
- Consultar informes basicos.
- Cerrar turno.
