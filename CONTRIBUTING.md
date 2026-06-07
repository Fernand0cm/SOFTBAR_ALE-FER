# Guia de contribucion

Gracias por contribuir a SOFTBAR. Esta guia resume como trabajar en el proyecto.

## Requisitos

- Android Studio (JDK 17) y Android SDK.
- Node 20+ para las pruebas de reglas de Firestore.
- Un `app/google-services.json` valido (el del repo apunta al proyecto de demo).

## Compilar y probar

```bash
# Pruebas unitarias (logica pura, sin emulador)
./gradlew testDebugUnitTest

# Generar el APK de depuracion
./gradlew assembleDebug

# Pruebas de las reglas de Firestore (emulador)
cd firestore-tests
npm ci
npm test
```

La integracion continua (GitHub Actions) ejecuta ambas suites en cada push y
pull request. No fusiones a `main` sin la CI en verde.

## Estilo de commits

- Mensajes en imperativo y en castellano, una idea por commit.
  Ejemplo: `Anade control de stock opcional por producto`.
- Commits pequenos y coherentes: facilitan la revision y el historial.

## Ramas

- `main`: rama estable y publicable (protegida; exige CI en verde).
- Ramas de trabajo por funcionalidad; se integran en `main` por Pull Request.

## Arquitectura

Antes de tocar codigo, revisa `docs/arquitectura.md`. Regla general:

- La UI (Activities/ViewModel) no accede a Firestore directamente: usa los
  repositorios de `data/repository`.
- La logica de negocio pura (`Dinero`, `CalculoIva`, `Permisos`...) no depende
  de Android y debe tener pruebas unitarias.
- Los nombres de campo y coleccion van en `FirestoreSchema`, no como literales.

## Seguridad

Cualquier cambio en `firestore.rules` debe acompanarse de pruebas en
`firestore-tests/rules.test.js`.
