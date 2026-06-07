# Guion de demo para la defensa

Recorrido reproducible de ~8-10 minutos que enseña el flujo completo del TPV y
los puntos tecnicos fuertes. Ensayarlo al menos una vez antes de la defensa.

## 0. Preparacion previa (antes de entrar)

- App instalada en emulador o dispositivo con conexion.
- Usuario de prueba **administrador** (para ver todos los modulos):
  - En Firebase Auth: `fer@softbar.com` / `123456`.
  - En Firestore: documento `usuarios/{uid}` con `rol = administrador`.
- Datos minimos precargados (o se crean en vivo en el paso 2):
  - 4-5 productos, con al menos uno al 21% (alcohol) y uno con control de stock.
  - Colecciones `mesas` (se siembran solas con 8 mesas).
- Tener a mano un **plan B**: un video o capturas del flujo por si falla la red
  del aula.

## 1. Acceso y arranque (1 min)

1. Abrir la app: **splash** y, si no hay sesion, **login**.
2. Iniciar sesion. En **Home** se ve el email, el **rol** y el indicador de
   conexion.
   - *Decir*: "Home filtra los modulos segun el rol; como administrador se ven
     todos."

## 2. Catalogo y stock (1-2 min)

1. Entrar en **Configuracion**.
2. Crear un producto **manualmente** (sin escaner): nombre, precio, **IVA**
   (10/21/4%), **categoria** y, si aplica, **control de stock** con minimo.
3. Editar uno existente para mostrar que se puede modificar y **desactivar**
   (no se borra).
   - *Decir*: "El stock es opcional por producto: en un bar solo tiene sentido
     contar lo contable."

## 3. Turno (30 s)

1. Volver a Home → **Turno** → abrir turno con importe inicial.
   - *Decir*: "Sin turno abierto no se puede cobrar; cada venta queda asociada al
     turno."

## 4. Comanda de mesa (2 min)

1. Home → **Mesas** → abrir una mesa libre.
2. Filtrar el catalogo por **categoria** (chips) y anadir productos.
3. En una linea, ajustar **cantidad** (+/-) y abrir la **personalizacion**
   (modificadores + nota).
4. Pulsar **Cobrar**.

## 5. Cobro y ticket (1-2 min)

1. Elegir metodo (probar **mixto**: efectivo + tarjeta), ver el **cambio**.
2. Confirmar.
   - *Decir*: "El cobro es una transaccion atomica: venta, factura, numeracion y
     liberacion de mesa van juntas o no van."
3. En el **ticket**: numero de factura, **base imponible**, **IVA**, desglose de
   pago, **QR Verifactu** y hash.
4. (Como admin/caja) pulsar **Rectificar factura** y mostrar la **rectificativa**
   encadenada en negativo.
   - *Decir*: "En Verifactu no se borra: se emite una factura rectificativa."

## 6. Caja, informes e historial (2 min)

1. Home → **Caja**: resumen del turno y **cierre** con arqueo (confirmacion
   incluida). Ver **cierres anteriores**.
2. Home → **Informes**: filtros por **fecha/turno/metodo**, KPIs, **base/IVA**,
   **productos mas vendidos**, **comparativa por dias** y **exportar resumen**.
3. Home → **Historial**: reabrir un ticket anterior.

## 7. Offline (cierre tecnico, 1 min)

1. Activar **modo avion**.
2. Abrir una mesa y anadir productos: el indicador pasa a **"Sincronizando..."**.
3. Intentar **cobrar**: aparece el aviso de que no se puede sin conexion.
   - *Decir*: "Asi se evita duplicar la numeracion de facturas offline."
4. Quitar modo avion: los cambios se sincronizan.

## Puntos tecnicos a destacar

- Arquitectura por capas con repositorios y una pantalla en MVVM
  (`docs/arquitectura.md`).
- Calculo monetario con `BigDecimal`; IVA por tipo.
- Reglas Firestore endurecidas (inmutabilidad, autor, anti-escalada) probadas
  con el emulador.
- 80 pruebas unitarias + 25 de reglas, ejecutadas ademas en CI (GitHub Actions).

## Preguntas frecuentes del tribunal

- *"Es multiplataforma?"* Nativo Android por profundidad; la logica de negocio
  es agnostica y portable (KMP/Flutter).
- *"Cumple Verifactu legalmente?"* Es un prototipo tecnico serio (hash + QR +
  rectificativa); no se vende como cumplimiento certificado.
- *"Quien puede escribir en la base de datos?"* Solo usuarios autenticados, con
  validacion por coleccion, registros inmutables y sin auto-escalada de rol.

## Checklist final (marcar antes de defender)

- [ ] Flujo completo probado de principio a fin.
- [ ] Datos de demo estables cargados.
- [ ] Capturas finales de pantallas reales tomadas.
- [ ] Plan B (video/capturas) preparado.
- [ ] `./gradlew testDebugUnitTest` y pruebas de reglas en verde.
