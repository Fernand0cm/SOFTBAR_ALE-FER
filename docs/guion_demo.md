# Guion de demo para la defensa

Recorrido reproducible del TPV que enseña el **flujo completo** y los **puntos
tecnicos fuertes**. Pensado para **5-7 minutos** en ruta corta y **8-10** en ruta
completa. Ensayarlo al menos una vez.

> **Plan B grabado.** Existe un video de respaldo de la demo (`docs/demo/`) por si
> falla la red o el proyector del aula. Si algo no responde en vivo, se pasa al video.

---

## 0. Preparacion previa (antes de entrar al aula)

- App instalada en **emulador o dispositivo** con conexion a internet.
- Sesion de prueba **administrador** (ve todos los modulos):
  - Firebase Auth: `fer@softbar.com` / `123456`.
  - Firestore: `usuarios/{uid}` con `rol = administrador`.
- Datos minimos (ya en la nube, compartidos entre dispositivos):
  - Catalogo con varias categorias y algun producto al 21 % y con control de stock.
  - Coleccion `mesas` (se siembra sola con 8-10 mesas).
- Comprobaciones rapidas: hay **turno** preparado para abrir, hay **conexion**, y
  el **volumen/brillo** del equipo estan bien para el video de respaldo.

**Reparto sugerido (grupo de 2):** uno conduce la app y narra la operativa
(secciones 1-6); el otro remata con los **puntos tecnicos** (seccion 8) y responde
en la ronda de preguntas. Repartir el tiempo a partes iguales.

---

## 1. Acceso y arranque · 1 min

**Objetivo:** mostrar el acceso seguro y el control por rol.

1. Abrir la app: **splash** y, si no hay sesion, **login**.
2. Iniciar sesion. En **Home** se ven el email, el **rol** y el **indicador de
   conexion** (online / sincronizando / sin conexion).

> *Decir:* "Home filtra los modulos segun el rol; como administrador se ven todos.
> El acceso lo gestiona Firebase Authentication y el rol se carga del perfil."

**Resaltar:** seguridad desde el acceso; un camarero no veria caja ni informes.

---

## 2. Catalogo y stock · 1-2 min

**Objetivo:** alta agil del catalogo, IVA por producto y stock opcional.

1. **Configuracion** → **Anadir producto manualmente** (sin escaner).
2. Rellenar nombre, precio, **tipo de IVA** (10/21/4 %), **categoria** y, si
   procede, **control de stock** con minimo. Guardar.
3. **Editar** uno existente para mostrar que se puede modificar y **desactivar**
   (no se borra del catalogo).
4. (Si el equipo tiene camara) ensenar el boton **Escanear** del catalogo.

> *Decir:* "El stock es opcional por producto: en un bar solo tiene sentido contar
> lo contable, no cada cafe. El IVA va por producto y su cuota se calcula en la
> factura."

**Resaltar:** las reglas del servidor **no permiten borrar** productos (se desactivan).

---

## 3. Turno · 30 s

**Objetivo:** todo ingreso queda asociado a un turno.

1. Home → **Turno** → abrir turno con **importe inicial**.

> *Decir:* "Sin turno abierto no se puede cobrar; cada venta y cada movimiento de
> caja quedan asociados al turno para el arqueo."

---

## 4. Comanda de mesa · 2 min

**Objetivo:** la operativa real de sala, en tiempo real.

1. Home → **Mesas**. Ensenar los **estados por color** (libre, ocupada, cobro).
2. **Abrir una mesa libre** y, antes de pedir, **salir**: la mesa **vuelve a
   libre** automaticamente.
   > *Decir:* "Si se abre una mesa y no se pide nada, al cerrar vuelve a quedar
   > libre; no se queda ocupada por error."
3. Abrir la mesa otra vez y **filtrar el catalogo por categoria** (chips).
4. Anadir productos; en una linea ajustar **cantidad** (+/-) y abrir la
   **personalizacion** (modificadores + nota). El **total se recalcula** solo.
5. Pulsar **Cobrar**.

**Resaltar:** todo sale de Firestore en **tiempo real**; la mesa se enlaza con su
comanda activa.

---

## 5. Cobro, ticket Veri*factu y acciones · 2 min

**Objetivo:** el nucleo: cobro transaccional y comprobante fiscal.

1. Elegir metodo; probar **mixto** (efectivo + tarjeta) y ver el **cambio**.
2. Confirmar.
   > *Decir:* "El cobro es una **transaccion atomica**: venta, factura,
   > numeracion correlativa, liberacion de mesa y descuento de stock van juntos o
   > no van. Por eso requiere conexion: asi se evita numeracion duplicada."
3. En el **ticket**: numero de factura, **base imponible**, **IVA**, desglose de
   pago, **QR Veri*factu** y hash encadenado.
4. **Imprimir**: abre el marco de impresion del sistema → **Guardar como PDF** o
   enviar a una impresora.
5. **Enviar por email / compartir**: manda el **ticket en texto con separadores**
   y la **URL de validacion** por email, WhatsApp, etc.
6. (Como admin/caja) **Rectificar factura** → mostrar la **rectificativa**
   encadenada en negativo.
   > *Decir:* "En Veri*factu no se borra ni se edita una factura: se emite una
   > **rectificativa** enlazada, con importes en negativo."

**Resaltar:** hash SHA-256 encadenado + QR a la URL de validacion (entorno de
pruebas de la AEAT); inmutabilidad garantizada por las reglas del servidor.

---

## 6. Caja, informes e historial · 2 min

**Objetivo:** la parte de gestion y datos para decidir.

1. Home → **Caja**: resumen del turno (apertura, ventas por metodo, retiradas,
   efectivo esperado). Registrar un **movimiento** (retirada). **Cerrar turno**
   con arqueo (efectivo contado vs esperado y **diferencia**), con confirmacion.
   Ensenar **cierres anteriores**.
2. Home → **Informes**: filtros por **fecha / turno / metodo**, KPIs (total,
   nº tickets, ticket medio), **base/IVA**, **productos mas vendidos**,
   **comparativa por dias** y **exportar resumen**.
3. Home → **Historial**: **reabrir** un ticket anterior (y desde ahi se puede
   reimprimir o compartir).

---

## 7. Cierre tecnico: modo offline · 1 min

**Objetivo:** demostrar la decision de diseno sobre la integridad fiscal.

1. Activar **modo avion**.
2. Abrir una mesa y anadir productos: la operativa **sigue** (cache local) y el
   indicador pasa a **"Sin conexion / sincronizando"**.
3. Intentar **cobrar**: aparece el aviso **"El cobro necesita red para garantizar
   la numeracion fiscal"**.
   > *Decir:* "La sala y la comanda funcionan offline, pero el cobro es una
   > transaccion: preferimos **rechazarlo sin red** antes que arriesgar numeracion
   > duplicada. Es una decision deliberada."
4. Quitar modo avion: los cambios pendientes se **sincronizan** solos.

---

## 8. Puntos tecnicos a destacar (remate)

- **Arquitectura por capas** con repositorios y una pantalla en **MVVM**
  (`docs/arquitectura.md`).
- **Logica pura aislada** (sin Android): dinero, IVA, hash y numeracion, testeable.
- Calculo monetario con **`BigDecimal`**; IVA por tipo.
- **Reglas Firestore endurecidas**: inmutabilidad de ventas/facturas, validacion
  por coleccion y **anti-escalada de rol**, probadas con el **emulador**.
- **90 pruebas unitarias + 25 de reglas**, ejecutadas ademas en **CI (GitHub
  Actions)** en cada cambio.
- **Veri*factu**: hash encadenado, QR de validacion y rectificativas.
- **Carga cognitiva** baja por diseno (modulos profundos, soluciones simples).

---

## Preguntas frecuentes del tribunal

- *"Es multiplataforma?"* Nativo Android por profundidad y rendimiento; la **logica
  de negocio es agnostica y portable** (KMP/Flutter como trabajo futuro).
- *"Cumple Veri*factu legalmente?"* Es un **prototipo tecnico serio** (hash + QR +
  rectificativa, formato de la AEAT); no se presenta como cumplimiento certificado.
  El envio real a la AEAT con certificado de empresa queda fuera de alcance.
- *"Quien puede escribir en la base de datos?"* Solo usuarios **autenticados**, con
  **validacion por coleccion**, registros **inmutables** y **sin auto-escalada de rol**.
- *"Funciona sin conexion?"* La consulta y la comanda si; el **cobro requiere red**
  por la transaccion (decision de diseno para no duplicar numeracion).
- *"Por que no imprime en impresora termica?"* Imprime via el marco de Android
  (PDF o impresora estandar); la **termica fisica** con ESC/POS es trabajo futuro.

---

## Ruta corta (si el tiempo aprieta, ~4 min)

Login → abrir turno → Mesas → comanda con una personalizacion → **cobro mixto** →
**ticket con QR** (imprimir/compartir) → **Informes** (KPIs) → cierre con el
**aviso offline**. El resto se menciona de palabra apoyandose en las diapositivas.

---

## Checklist final (marcar antes de defender)

- [ ] Flujo completo probado de principio a fin el dia anterior.
- [ ] Datos de demo estables (catalogo, mesas, algun ticket previo).
- [ ] **Video de respaldo** disponible y probado en el equipo del aula.
- [ ] Capturas finales en `docs/demo/`.
- [ ] `./gradlew testDebugUnitTest` y pruebas de reglas en verde.
- [ ] Equipo con bateria/cargador y el cable del dispositivo si se usa fisico.
