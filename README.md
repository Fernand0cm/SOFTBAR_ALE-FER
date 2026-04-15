# SOFTBAR

SOFTBAR es la base de una aplicacion Android para un TPV de bar conectado con Supabase. La idea del proyecto es cubrir la operativa real de un negocio de hosteleria: barra, sala, comandas, cocina, cobros, caja, stock y administracion basica.

Este README nace como documento vivo. La app esta en una fase muy inicial, asi que aqui queda descrito tanto lo que ya existe como la direccion funcional y tecnica que debe seguir.

## Estado actual

- Proyecto Android nativo con modulo `app`.
- Base visual inicial del TPV para arrancar la aplicacion.
- Sin integracion real con Supabase todavia.
- Sin flujo de login, persistencia, sincronizacion ni modulos de negocio implementados.

## Vision del producto

El objetivo es construir un software de gestion para bar o cafeteria que permita trabajar desde una tablet o dispositivo Android con una experiencia rapida, clara y orientada a operacion real.

Casos de uso principales:

- Abrir mesa o pedido rapido en barra.
- Anadir productos y modificadores a la comanda.
- Enviar pedidos a cocina o preparacion.
- Cobrar con efectivo, tarjeta u otros metodos.
- Gestionar caja y cierres.
- Consultar ventas, stock y productos.
- Mantener sincronizacion con Supabase como backend central.

## Modulos base del TPV

### 1. Operacion de sala y barra

- Apertura y gestion de mesas.
- Pedido rapido para barra o takeaway.
- Cambio de mesa, union de mesas y division de cuenta.
- Estado visual de mesas: libre, ocupada, pendiente de cobro, cerrada.

### 2. Catalogo y productos

- Categorias de productos.
- Productos activos e inactivos.
- Extras, suplementos y observaciones.
- Precios, impuestos y control de disponibilidad.

### 3. Comandas

- Alta de pedido.
- Lineas de pedido con cantidad, notas y extras.
- Envio a cocina o cola de preparacion.
- Historial y reimpresion futura.

### 4. Cobros y caja

- Pago mixto o completo.
- Control de caja por turno.
- Apertura y cierre de caja.
- Registro de movimientos manuales.

### 5. Gestion del negocio

- Usuarios y roles.
- Resumen de ventas.
- Productos mas vendidos.
- Stock basico y movimientos.

## Supabase como backend

Supabase sera la pieza central para:

- Autenticacion de usuarios.
- Base de datos principal del negocio.
- Sincronizacion de pedidos y mesas.
- Reglas de acceso por rol.
- Almacenamiento de configuraciones y, mas adelante, documentos o tickets si hace falta.

### Modelo de datos inicial propuesto

Tablas candidatas para la primera iteracion:

- `profiles`
- `business_settings`
- `product_categories`
- `products`
- `product_modifiers`
- `room_tables`
- `orders`
- `order_items`
- `payments`
- `cash_shifts`
- `stock_movements`

## Arquitectura prevista

Base tecnica actual:

- Android nativo.
- Java 11.
- Material Components.
- `minSdk 28`.

Direccion recomendada para la siguiente fase:

- Capa `ui` para pantallas y estado visual.
- Capa `data` para acceso a Supabase y almacenamiento local.
- Capa `domain` para reglas de negocio del TPV.
- Configuracion segura de credenciales fuera del codigo.
- Posible soporte offline con sincronizacion diferida.

## Roadmap inicial

### Fase 1. Arranque del proyecto

- Base Android operativa.
- Pantalla inicial del TPV.
- README funcional y tecnico.

### Fase 2. Conexion con backend

- Integracion con Supabase.
- Login de empleados.
- Carga de catalogo y mesas.

### Fase 3. Flujo real de venta

- Creacion de comandas.
- Gestion de lineas de pedido.
- Cambio de estado de pedidos.
- Cobro y cierre de pedido.

### Fase 4. Gestion y control

- Caja por turno.
- Informes basicos.
- Stock.
- Roles y permisos.

## Estructura actual del proyecto

```text
TFG_SOFTBAR/
|- app/
|- gradle/
|- build.gradle.kts
|- settings.gradle.kts
|- README.md
```

## Como ejecutar la app

1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Ejecutar el modulo `app` en un emulador o dispositivo Android.

## Pendiente de mejora

- Definir el flujo de login y sesiones.
- Integrar Supabase de forma real.
- Separar capas de la app conforme crezca el proyecto.
- Disenar impresiones, cocina y tickets.
- Anadir pruebas de negocio y navegacion.
- Mejorar soporte offline.
- Definir configuracion por negocio o local.

## Enfoque de trabajo

La idea es que este README vaya creciendo junto al desarrollo. Cada vez que se cierre una parte importante del TPV, conviene actualizar:

- Estado actual.
- Modulos ya implementados.
- Dependencias clave.
- Decisiones tecnicas.
- Pendientes reales.

Asi el repositorio seguira siendo util tanto para desarrollo como para presentacion del proyecto.
