/**
 * Siembra de datos de prueba para SOFTBAR.
 *
 * - Borra la actividad anterior (comandas, ventas, facturas, movimientos, turnos,
 *   contadores y productos) y reinicia las mesas.
 * - Crea ~285 productos reales repartidos en categorias, con IVA por categoria y
 *   control de stock en lo contable.
 * - Reinicia configuracion fiscal, mesas (8) y el usuario administrador de demo.
 *
 * USO (necesita un service account de Firebase con permisos de admin):
 *   1) Descarga la clave: Firebase Console > Configuracion del proyecto >
 *      Cuentas de servicio > Generar nueva clave privada.
 *   2) Guardala como tools/seed/serviceAccount.json (esta en .gitignore).
 *   3) cd tools/seed && npm install && npm run seed
 *
 * El admin SDK ignora las reglas de seguridad: por eso puede borrar y sembrar.
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");
const { CATEGORIAS, generarProductos } = require("./generarProductos");

if (!process.argv.includes("--yes")) {
  console.error(
    "Esto BORRA la actividad anterior. Ejecuta con --yes para confirmar:\n" +
      "  node seed.js --yes"
  );
  process.exit(1);
}

// Credenciales: serviceAccount.json local o GOOGLE_APPLICATION_CREDENTIALS.
const cuentaLocal = path.join(__dirname, "serviceAccount.json");
if (fs.existsSync(cuentaLocal)) {
  admin.initializeApp({ credential: admin.credential.cert(require(cuentaLocal)) });
} else if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  admin.initializeApp({ credential: admin.credential.applicationDefault() });
} else {
  console.error(
    "No hay credenciales. Crea tools/seed/serviceAccount.json o exporta " +
      "GOOGLE_APPLICATION_CREDENTIALS."
  );
  process.exit(1);
}

const db = admin.firestore();

const EMAIL_ADMIN = "fer@softbar.com";
const NIF_EMISOR = "B12345678";
const SERIE = "A";
const NUM_MESAS = 8;


async function borrarColeccion(nombre, lote = 300) {
  const ref = db.collection(nombre);
  let total = 0;
  while (true) {
    const snap = await ref.limit(lote).get();
    if (snap.empty) break;
    const batch = db.batch();
    snap.docs.forEach((d) => batch.delete(d.ref));
    await batch.commit();
    total += snap.size;
  }
  return total;
}

async function main() {
  console.log("Borrando actividad anterior...");
  for (const c of ["comandas", "ventas", "facturas", "movimientos_caja",
    "turnos", "contadores", "productos", "mesas"]) {
    const n = await borrarColeccion(c);
    console.log(`  - ${c}: ${n} documentos borrados`);
  }

  console.log("Sembrando productos...");
  const productos = generarProductos();
  let batch = db.batch();
  let pendientes = 0;
  for (const p of productos) {
    batch.set(db.collection("productos").doc(p.codigoBarras), p);
    if (++pendientes === 400) { await batch.commit(); batch = db.batch(); pendientes = 0; }
  }
  if (pendientes > 0) await batch.commit();
  console.log(`  - productos: ${productos.length} creados`);

  console.log("Creando mesas...");
  const batchMesas = db.batch();
  for (let i = 1; i <= NUM_MESAS; i++) {
    batchMesas.set(db.collection("mesas").doc(String(i)),
      { numero: i, estado: "libre", comandaActivaId: null });
  }
  await batchMesas.commit();
  console.log(`  - mesas: ${NUM_MESAS} creadas`);

  console.log("Configuracion fiscal...");
  await db.doc("configuracion/fiscal").set({ nifEmisor: NIF_EMISOR, serie: SERIE });

  console.log("Usuario administrador...");
  try {
    const user = await admin.auth().getUserByEmail(EMAIL_ADMIN);
    await db.collection("usuarios").doc(user.uid).set({
      email: EMAIL_ADMIN, nombre: "Fernando", rol: "administrador",
    });
    console.log(`  - usuarios/${user.uid} = administrador`);
  } catch (e) {
    console.warn(`  ! No se pudo asignar admin a ${EMAIL_ADMIN}: ${e.message}`);
  }

  console.log("\nResumen por categoria:");
  for (const cat of CATEGORIAS) {
    const n = productos.filter((p) => p.categoria === cat.nombre).length;
    console.log(`  ${cat.nombre.padEnd(12)} ${n}  (IVA ${cat.iva * 100}%, stock ${cat.stock})`);
  }
  console.log(`\nTotal productos: ${productos.length}`);
  console.log("Listo. La actividad (ventas/turnos/...) queda limpia para la demo.");
}

main().then(() => process.exit(0)).catch((e) => { console.error(e); process.exit(1); });
