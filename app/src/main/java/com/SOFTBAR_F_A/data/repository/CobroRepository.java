package com.SOFTBAR_F_A.data.repository;

import androidx.annotation.Nullable;

import com.SOFTBAR_F_A.data.CalculoIva;
import com.SOFTBAR_F_A.data.Comanda;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.SOFTBAR_F_A.data.Mesa;
import com.SOFTBAR_F_A.data.Turno;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.data.verifactu.ConfiguracionFiscal;
import com.SOFTBAR_F_A.data.verifactu.Factura;
import com.SOFTBAR_F_A.data.verifactu.GeneradorQrVerifactu;
import com.SOFTBAR_F_A.data.verifactu.HashVerifactu;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Encapsula toda la logica de negocio y de acceso a Firestore del cobro:
 * busqueda del turno activo, numeracion fiscal, calculo de la factura
 * (cuota de IVA, hash encadenado y QR Verifactu) y la transaccion atomica que
 * crea venta + factura + contador y libera la mesa.
 *
 * La capa de UI ({@code CobroActivity}) solo recoge datos y reacciona a los
 * callbacks: aqui no hay ninguna dependencia de Android salvo el SDK de
 * Firebase, lo que mantiene la regla de negocio aislada y reutilizable.
 */
public class CobroRepository {

    /** Resultado del cobro: identificadores para abrir el ticket. */
    public interface CobroCallback {
        void onExito(String ventaId, String facturaId);
        void onSinTurno();
        void onError(@Nullable String mensaje);
    }

    /** Datos de entrada del cobro recogidos en la UI. */
    public static class SolicitudCobro {
        public double total;
        public String comandaId;
        public String mesaId;
        public List<LineaComanda> lineasBarra;
        public String metodo;
        public double pagoEfectivo;
        public double pagoTarjeta;
        public double importeRecibido;
        public double cambio;
    }

    private final FirebaseFirestore db;

    public CobroRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public CobroRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Comprueba que el usuario tiene un turno abierto y, si lo hay, ejecuta el
     * cobro dentro de una transaccion. Notifica el resultado por el callback.
     */
    public void registrarCobro(SolicitudCobro solicitud, FirebaseUser user,
                               CobroCallback callback) {
        db.collection(FirestoreSchema.Collections.TURNOS)
                .whereEqualTo(FirestoreSchema.Fields.ESTADO, Turno.ABIERTO)
                .whereEqualTo(FirestoreSchema.Fields.USUARIO_UID, user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) {
                        callback.onSinTurno();
                        return;
                    }
                    ejecutarTransaccion(
                            solicitud, user, snap.getDocuments().get(0).getId(), callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    private void ejecutarTransaccion(SolicitudCobro solicitud, FirebaseUser user,
                                     String turnoId, CobroCallback callback) {
        Date ahora = new Date();
        Timestamp ts = new Timestamp(ahora);
        String anyo = new SimpleDateFormat("yyyy", Locale.ROOT).format(ahora);

        DocumentReference contadorRef = db.collection(FirestoreSchema.Collections.CONTADORES)
                .document(FirestoreSchema.Documents.CONTADOR_FACTURAS + "_" + anyo);
        DocumentReference configRef = db.collection(FirestoreSchema.Collections.CONFIGURACION)
                .document(FirestoreSchema.Documents.CONFIG_FISCAL);
        DocumentReference ventaRef = db.collection(FirestoreSchema.Collections.VENTAS).document();
        DocumentReference comandaRef = solicitud.comandaId != null
                ? db.collection(FirestoreSchema.Collections.COMANDAS).document(solicitud.comandaId)
                : null;

        db.runTransaction(transaction -> {
                    Comanda comanda = null;
                    if (comandaRef != null) {
                        DocumentSnapshot comandaDoc = transaction.get(comandaRef);
                        if (comandaDoc.exists()) {
                            comanda = comandaDoc.toObject(Comanda.class);
                        }
                    }

                    DocumentSnapshot configDoc = transaction.get(configRef);
                    ConfiguracionFiscal config = configDoc.exists()
                            ? configDoc.toObject(ConfiguracionFiscal.class)
                            : new ConfiguracionFiscal();
                    if (config == null) config = new ConfiguracionFiscal();

                    DocumentSnapshot contador = transaction.get(contadorRef);
                    long ultimo = contador.exists()
                            && contador.getLong(FirestoreSchema.Fields.ULTIMO) != null
                            ? contador.getLong(FirestoreSchema.Fields.ULTIMO)
                            : 0L;
                    String hashAnterior = contador.exists()
                            ? contador.getString(FirestoreSchema.Fields.HASH_ULTIMO)
                            : "";
                    if (hashAnterior == null) hashAnterior = "";
                    int siguiente = (int) ultimo + 1;

                    List<LineaComanda> lineas = comanda != null
                            ? comanda.getLineas() : solicitud.lineasBarra;
                    int mesaNumero = comanda != null ? comanda.getMesaNumero() : 0;

                    // Fase de lectura: stock de los productos que se controlan.
                    Map<DocumentReference, Long> nuevoStock = new HashMap<>();
                    if (lineas != null) {
                        for (LineaComanda l : lineas) {
                            if (l.getCodigoBarras() == null) continue;
                            DocumentReference pRef = db
                                    .collection(FirestoreSchema.Collections.PRODUCTOS)
                                    .document(l.getCodigoBarras());
                            if (nuevoStock.containsKey(pRef)) continue;
                            DocumentSnapshot pDoc = transaction.get(pRef);
                            if (pDoc.exists()
                                    && Boolean.TRUE.equals(pDoc.getBoolean(
                                        FirestoreSchema.Fields.CONTROLAR_STOCK))) {
                                Long actual = pDoc.getLong(FirestoreSchema.Fields.STOCK);
                                long base = actual != null ? actual : 0L;
                                nuevoStock.put(pRef, Math.max(0L, base - l.getCantidad()));
                            }
                        }
                    }

                    Factura factura = construirFactura(
                            ts, ahora, siguiente, hashAnterior, config,
                            solicitud.total, lineas);
                    factura.setMetodo(solicitud.metodo);
                    factura.setMesaId(solicitud.mesaId);
                    factura.setMesaNumero(mesaNumero);
                    factura.setLineas(lineas);
                    factura.setPagoEfectivo(solicitud.pagoEfectivo);
                    factura.setPagoTarjeta(solicitud.pagoTarjeta);
                    factura.setImporteRecibido(solicitud.importeRecibido);
                    factura.setCambio(solicitud.cambio);
                    String facturaId = factura.getNumero().replace("/", "-");
                    DocumentReference facturaRef = db
                            .collection(FirestoreSchema.Collections.FACTURAS)
                            .document(facturaId);

                    Venta venta = new Venta(ts, solicitud.total, solicitud.metodo);
                    venta.setFacturaId(facturaId);
                    venta.setComandaId(solicitud.comandaId);
                    venta.setMesaId(solicitud.mesaId);
                    venta.setMesaNumero(mesaNumero);
                    venta.setLineas(lineas);
                    venta.setTurnoId(turnoId);
                    venta.setUsuarioUid(user.getUid());
                    venta.setUsuarioEmail(user.getEmail());
                    venta.setPagoEfectivo(solicitud.pagoEfectivo);
                    venta.setPagoTarjeta(solicitud.pagoTarjeta);
                    venta.setImporteRecibido(solicitud.importeRecibido);
                    venta.setCambio(solicitud.cambio);

                    Map<String, Object> contadorData = new HashMap<>();
                    contadorData.put(FirestoreSchema.Fields.ULTIMO, siguiente);
                    contadorData.put(FirestoreSchema.Fields.HASH_ULTIMO, factura.getHashActual());

                    transaction.set(contadorRef, contadorData);
                    transaction.set(ventaRef, venta);
                    transaction.set(facturaRef, factura);

                    if (comandaRef != null) {
                        transaction.update(comandaRef,
                                FirestoreSchema.Fields.ESTADO, Comanda.PAGADA);
                    }
                    if (solicitud.mesaId != null) {
                        transaction.update(
                                db.collection(FirestoreSchema.Collections.MESAS)
                                        .document(solicitud.mesaId),
                                FirestoreSchema.Fields.ESTADO, Mesa.LIBRE,
                                FirestoreSchema.Fields.COMANDA_ACTIVA_ID, null);
                    }

                    // Fase de escritura: descontar el stock leido.
                    for (Map.Entry<DocumentReference, Long> e : nuevoStock.entrySet()) {
                        transaction.update(e.getKey(),
                                FirestoreSchema.Fields.STOCK, e.getValue());
                    }

                    return new ResultadoCobro(ventaRef.getId(), facturaId);
                })
                .addOnSuccessListener(resultado ->
                        callback.onExito(resultado.ventaId, resultado.facturaId))
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    private Factura construirFactura(Timestamp ts, Date ahora, int siguiente,
                                     String hashAnterior, ConfiguracionFiscal config,
                                     double total, List<LineaComanda> lineas) {
        SimpleDateFormat anyoFmt = new SimpleDateFormat("yyyy", Locale.ROOT);
        SimpleDateFormat fechaIso = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        SimpleDateFormat fechaQr = new SimpleDateFormat("dd-MM-yyyy", Locale.ROOT);

        String numero = String.format(Locale.ROOT, "%s-%04d/%s",
                config.getSerie(), siguiente, anyoFmt.format(ahora));
        double cuotaIva = CalculoIva.cuotaTotal(lineas);

        String hashActual = HashVerifactu.calcular(
                numero, fechaIso.format(ahora), config.getNifEmisor(),
                total, cuotaIva, hashAnterior);

        String urlValidacion = GeneradorQrVerifactu.construirUrl(
                config.getNifEmisor(), numero, fechaQr.format(ahora), total);

        return new Factura(numero, ts, config.getNifEmisor(),
                total, cuotaIva, hashAnterior, hashActual, urlValidacion);
    }

    private static class ResultadoCobro {
        final String ventaId;
        final String facturaId;

        ResultadoCobro(String ventaId, String facturaId) {
            this.ventaId = ventaId;
            this.facturaId = facturaId;
        }
    }
}
