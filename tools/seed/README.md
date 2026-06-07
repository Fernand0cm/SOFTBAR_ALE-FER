# Siembra de datos de prueba

Script para dejar la base de datos lista para las **capturas** de la defensa:
borra la actividad anterior y crea un catalogo realista (~285 productos en
categorias), mesas, configuracion fiscal y el usuario administrador.

## Por que necesita un service account

Las reglas de Firestore impiden borrar `productos`, `ventas`, `facturas`, etc.
desde el cliente (es lo correcto para seguridad). El **Admin SDK** ignora las
reglas, por eso el borrado y la siembra se hacen con una cuenta de servicio.

## Pasos

1. En Firebase Console del proyecto `tfg-softba`:
   **Configuracion del proyecto > Cuentas de servicio > Generar nueva clave
   privada**. Descarga el JSON.
2. Guardalo como `tools/seed/serviceAccount.json` (ya esta en `.gitignore`, no
   se sube al repo).
3. Ejecuta:

   ```bash
   cd tools/seed
   npm install
   npm run seed
   ```

## Que hace exactamente

- **Borra**: `comandas`, `ventas`, `facturas`, `movimientos_caja`, `turnos`,
  `contadores`, `productos` y `mesas`.
- **Crea**:
  - ~285 productos en: Cafes, Infusiones, Refrescos, Cervezas, Vinos, Licores,
    Desayunos, Tapas, Raciones, Bocadillos, Montaditos, Postres.
  - IVA por categoria (21% en alcohol; 10% en el resto).
  - Control de stock en lo contable (refrescos, cervezas, vinos, licores).
  - 8 mesas libres.
  - `configuracion/fiscal` (NIF y serie).
  - `usuarios/{uid}` del usuario `fer@softbar.com` con rol `administrador`.

## Despues de sembrar

1. Inicia sesion en la app con `fer@softbar.com`.
2. Sigue `docs/guion_demo.md` para generar una venta de ejemplo y tomar las
   capturas (mesas, comanda con categorias, cobro, ticket, informes, stock...).

> Aviso: el borrado es irreversible. Hazlo solo sobre el proyecto de demo.
