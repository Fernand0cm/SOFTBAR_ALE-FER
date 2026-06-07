/**
 * Sembrado en vivo usando el usuario de demo (sin service account).
 * Respeta las reglas: crea productos y mesas, y desactiva (no borra) el
 * catalogo antiguo. Soluciona E2 (mesas) y E3 (catalogo) contra la BBDD real.
 *
 * Uso: cd firestore-tests && node seed-cliente.js
 */
const { initializeApp } = require("firebase/app");
const { getAuth, signInWithEmailAndPassword } = require("firebase/auth");
const {
  getFirestore, collection, doc, getDocs, writeBatch,
} = require("firebase/firestore");
const { generarProductos } = require("../tools/seed/generarProductos");

const firebaseConfig = {
  apiKey: "AIzaSyBTk9Ie7BfSkZmSkEEvM_-7_PcTCR5tBUw",
  authDomain: "tfg-softba.firebaseapp.com",
  projectId: "tfg-softba",
};

const EMAIL = "fer@softbar.com";
const PASS = "123456";

async function main() {
  const app = initializeApp(firebaseConfig);
  const auth = getAuth(app);
  const db = getFirestore(app);

  await signInWithEmailAndPassword(auth, EMAIL, PASS);
  console.log("Autenticado como", EMAIL);

  // 1) Desactivar catalogo antiguo (no se puede borrar por reglas).
  const actuales = await getDocs(collection(db, "productos"));
  let b = writeBatch(db);
  let n = 0;
  actuales.forEach((d) => {
    b.set(doc(db, "productos", d.id), { ...d.data(), activo: false }, { merge: true });
    if (++n % 400 === 0) { /* flush mas abajo */ }
  });
  if (n > 0) await b.commit();
  console.log("Catalogo antiguo desactivado:", n);

  // 2) Crear catalogo nuevo (~285 productos activos).
  const productos = generarProductos();
  let batch = writeBatch(db);
  let pend = 0, total = 0;
  for (const p of productos) {
    batch.set(doc(db, "productos", p.codigoBarras), p);
    total++;
    if (++pend === 400) { await batch.commit(); batch = writeBatch(db); pend = 0; }
  }
  if (pend > 0) await batch.commit();
  console.log("Productos creados:", total);

  // 3) Reiniciar mesas 1..10 a libre (crea la que falte, p. ej. la 2).
  const mb = writeBatch(db);
  for (let i = 1; i <= 10; i++) {
    mb.set(doc(db, "mesas", String(i)),
      { numero: i, estado: "libre", comandaActivaId: null });
  }
  await mb.commit();
  console.log("Mesas reiniciadas: 10 (libres)");

  console.log("Hecho. Catalogo y mesas listos para las capturas.");
}

main().then(() => process.exit(0)).catch((e) => { console.error(e); process.exit(1); });
