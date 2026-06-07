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

test("un producto con tipoIva valido se acepta", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertSucceeds(
    setDoc(doc(db, "productos/p3"), {
      codigoBarras: "333",
      nombre: "Cerveza",
      precio: 2.5,
      tipoIva: 0.21,
    })
  );
});

test("un producto con tipoIva fuera de rango es rechazado", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(
    setDoc(doc(db, "productos/p4"), {
      codigoBarras: "444",
      nombre: "Erroneo",
      precio: 1.0,
      tipoIva: 1.5,
    })
  );
});

test("un producto con campo activo se acepta", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertSucceeds(
    setDoc(doc(db, "productos/p5"), {
      codigoBarras: "555",
      nombre: "Tostada",
      precio: 1.8,
      tipoIva: 0.1,
      activo: false,
    })
  );
});

test("un producto con categoria se acepta", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertSucceeds(
    setDoc(doc(db, "productos/p7"), {
      codigoBarras: "777",
      nombre: "Cortado",
      precio: 1.3,
      tipoIva: 0.1,
      activo: true,
      categoria: "Cafes",
    })
  );
});

test("un producto con campos de stock validos se acepta", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertSucceeds(
    setDoc(doc(db, "productos/p8"), {
      codigoBarras: "888",
      nombre: "Botellin",
      precio: 1.5,
      controlarStock: true,
      stock: 24,
      stockMinimo: 6,
    })
  );
});

test("un producto con stock negativo es rechazado", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(
    setDoc(doc(db, "productos/p9"), {
      codigoBarras: "999",
      nombre: "Botellin",
      precio: 1.5,
      controlarStock: true,
      stock: -3,
      stockMinimo: 6,
    })
  );
});

test("un producto no se puede borrar (solo se desactiva)", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "productos/p6"), {
      codigoBarras: "666",
      nombre: "Cafe con leche",
      precio: 1.5,
    });
  });
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(deleteDoc(doc(db, "productos/p6")));
});

test("un usuario puede registrarse a si mismo como camarero", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertSucceeds(
    setDoc(doc(db, "usuarios/" + UID_A), {
      email: "a@softbar.com",
      nombre: "A",
      rol: "camarero",
    })
  );
});

test("un usuario NO puede registrarse a si mismo como administrador", async () => {
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(
    setDoc(doc(db, "usuarios/" + UID_A), {
      email: "a@softbar.com",
      nombre: "A",
      rol: "administrador",
    })
  );
});

test("un usuario NO puede ascenderse a si mismo (cambiar su rol)", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "usuarios/" + UID_A), {
      email: "a@softbar.com",
      nombre: "A",
      rol: "camarero",
    });
  });
  const db = testEnv.authenticatedContext(UID_A).firestore();
  await assertFails(
    setDoc(doc(db, "usuarios/" + UID_A), {
      email: "a@softbar.com",
      nombre: "A",
      rol: "administrador",
    })
  );
});

test("un administrador puede crear usuarios con cualquier rol", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "usuarios/jefe"), {
      email: "jefe@softbar.com",
      nombre: "Jefe",
      rol: "administrador",
    });
  });
  const db = testEnv.authenticatedContext("jefe").firestore();
  await assertSucceeds(
    setDoc(doc(db, "usuarios/" + UID_B), {
      email: "b@softbar.com",
      nombre: "B",
      rol: "caja",
    })
  );
});
