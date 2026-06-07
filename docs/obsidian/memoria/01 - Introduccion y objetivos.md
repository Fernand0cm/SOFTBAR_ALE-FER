---
tags: [tfg, memoria]
capitulo: 1
---
# 1. Introducción y objetivos

[[Inicio|← Índice]] · siguiente: [[02 - Estado del arte y justificacion]]

## 1.1 Introducción

La hostelería necesita herramientas de **punto de venta (TPV)** rápidas,
fiables y asequibles. SOFTBAR es una aplicación **Android nativa** que digitaliza
la operativa de un bar o cafetería: desde que el camarero abre el turno hasta que
se cierra la caja, pasando por la toma de comandas, el cobro y la emisión de la
factura simplificada conforme al sistema **Veri*factu** de la AEAT.

El proyecto se desarrolla como **Trabajo de Fin de Grado** del ciclo de
**Desarrollo de Aplicaciones Multiplataforma (DAM)**, con vocación de prototipo
con **salida comercial real**.

## 1.2 Motivación

> [!info] Por qué este proyecto
> - La normativa **Veri*factu** (facturación verificable) es de plena actualidad
>   en España: un TPV que la contemple aporta valor diferencial.
> - Permite demostrar un **flujo de negocio completo** y no un CRUD trivial.
> - Integra **backend en la nube** (Firebase) con **seguridad por reglas**,
>   transacciones y modo offline.

## 1.3 Objetivos

### Objetivo general
Construir un TPV de hostelería funcional, seguro y mantenible que cubra el ciclo
operativo completo y la gestión del negocio.

### Objetivos específicos

- [x] **O1.** Flujo de venta de extremo a extremo: login → turno → comanda →
  cobro → factura/ticket.
- [x] **O2.** Gestión de **caja** por turno con arqueo y cierres históricos.
- [x] **O3.** **Catálogo** editable con IVA por producto y categorías.
- [x] **O4.** **Stock** opcional con descuento automático al vender.
- [x] **O5.** **Informes** con filtros (fecha, turno, método) y comparativas.
- [x] **O6.** **Roles** de usuario y seguridad en servidor (reglas Firestore).
- [x] **O7.** **Veri*factu**: numeración, hash encadenado, QR y rectificativas.
- [x] **O8.** **Calidad**: pruebas automatizadas y **CI/CD**.

## 1.4 Alcance

| Dentro del alcance | Fuera del alcance (líneas futuras) |
|---|---|
| TPV operativo Android + Firebase | App multiplataforma (KMP/Flutter) |
| Factura simplificada Veri*factu (prototipo) | Cumplimiento Veri*factu certificado y envío AEAT |
| Pagos efectivo/tarjeta/mixto (registro) | Integración con datáfono / pasarela real |
| Stock por producto | Inventario por ingredientes/escandallos |
| Roles y permisos por módulo | Gestión avanzada de RRHH |

## 1.5 Competencias del título que se demuestran

- Desarrollo de aplicaciones móviles nativas.
- Persistencia y servicios en la nube.
- Programación segura y control de acceso.
- Pruebas, integración continua y control de versiones.
- Análisis y diseño orientado a objetos.
