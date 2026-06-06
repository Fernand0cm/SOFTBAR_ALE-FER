/**
 * Pruebas de las reglas de seguridad de Firestore de SOFTBAR.
 *
 * Se ejecutan contra el emulador de Firestore. La forma recomendada es:
 *
 *   npm test
 *
 * que arranca el emulador con `firebase emulators:exec` y corre este archivo
 * con el test runner nativo de Node (`node --test`).
 *
 * Cubren las garantias criticas del modelo de datos fiscal:
 *  - Solo usuarios autenticados acceden a datos de negocio.
 *  - Cada venta/turno/movimiento queda atado a su autor (usuarioUid == auth.uid).
 *  - Ventas, facturas y movimientos son inmutables una vez creados.
 *  - El contador de facturas solo puede avanzar (numeracion monotona).
 */

const fs = require("fs");
const path = require("path");
const test = require("node:test");
const assert = require("node:assert");
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require("@firebase/rules-unit-testing");
const {
  doc,
  setDoc,
  getDoc,
  updateDoc,
  deleteDoc,
  Timestamp,
} = require("firebase/firestore");

const PROJECT_ID = "softbar-rules-test";
const UID_A = "camarero_a";
const UID_B = "camarero_b";

let testEnv;

function ventaValida(uid) {
  return {
    fecha: Timestamp.now(),
    total: 12.5,
    metodo: "Efectivo",
    facturaId: "A-0001-2026",
    comandaId: null,
    mesaId: null,
    mesaNumero: 0,
    lineas: [],
    turnoId: "turno1",
    usuarioUid: uid,
    usuarioEmail: "a@softbar.com",
    pagoEfectivo: 12.5,
    pagoTarjeta: 0,
    importeRecibido: 15,
    cambio: 2.5,
  };
}

function facturaValida() {
  return {
    numero: "A-0001/2026",
    fecha: Timestamp.now(),
    nifEmisor: "B12345678",
    total: 12.5,
    cuotaIva: 1.14,
    hashAnterior: "",
    hashActual: "abc123",
    urlValidacion: "https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR?nif=B12345678",
    metodo: "Efectivo",
    mesaId: null,
    mesaNumero: 0,
    lineas: [],
    pagoEfectivo: 12.5,
    pagoTarjeta: 0,
    importeRecibido: 15,
    cambio: 2.5,
  };
}

test.before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync(path.resolve(__dirname, "../firestore.rules"), "utf8"),
    },
  });
});

test.after(async () => {
  if (testEnv) await testEnv.cleanup();
});

test.beforeEach(async () => {
  await testEnv.clearFirestore();
});

test("usuario no autenticado no puede leer ventas", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(db, "ventas/v1")));
});

test("usuario no autenticado no puede crear productos", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    setDoc(doc(db, "productos/p1"), {
      codigoBarras: "111",
      nombre: "Cafe",
      precio: 1.2,
    })
  );
});

test("splash_backgrounds es de lectura publica", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "splash_backgrounds/s1"), { active: true });
  });
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(getDoc(doc(db, "splash_backgrounds/s1")));
});

test("un usuario puede crear una venta con su propio uid", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertSucceeds(setDoc(doc(db, "ventas/v1"), ventaValida(UID_A)));
});

test("un usuario NO puede crear una venta atribuida a otro uid", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(setDoc(doc(db, "ventas/v2"), ventaValida(UID_B)));
});

test("una venta no se puede modificar una vez creada", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "ventas/v3"), ventaValida(UID_A));
  });
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(updateDoc(doc(db, "ventas/v3"), { total: 9999 }));
});

test("una venta no se puede borrar", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "ventas/v4"), ventaValida(UID_A));
  });
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(deleteDoc(doc(db, "ventas/v4")));
});

test("una factura es inmutable: no admite update", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "facturas/f1"), facturaValida());
  });
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(updateDoc(doc(db, "facturas/f1"), { total: 0.01 }));
});

test("una factura valida se puede crear", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertSucceeds(setDoc(doc(db, "facturas/f2"), facturaValida()));
});

test("el contador de facturas solo puede avanzar", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "contadores/facturas_2026"), {
      ultimo: 5,
      hashUltimo: "hash5",
    });
  });
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(
    updateDoc(doc(db, "contadores/facturas_2026"), { ultimo: 4, hashUltimo: "x" })
  );
  await assertSucceeds(
    updateDoc(doc(db, "contadores/facturas_2026"), { ultimo: 6, hashUltimo: "hash6" })
  );
});

test("un movimiento de caja no se puede modificar tras crearse", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "movimientos_caja/m1"), {
      fecha: Timestamp.now(),
      tipo: "entrada",
      importe: 50,
      descripcion: "fondo",
      turnoId: "turno1",
      usuarioUid: UID_A,
      usuarioEmail: "a@softbar.com",
    });
  });
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(updateDoc(doc(db, "movimientos_caja/m1"), { importe: 0 }));
});

test("un producto con precio negativo es rechazado", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(
    setDoc(doc(db, "productos/p2"), {
      codigoBarras: "222",
      nombre: "Refresco",
      precio: -1,
    })
  );
});
