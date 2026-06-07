package com.SOFTBAR_F_A.data.repository;

import androidx.annotation.Nullable;

import com.SOFTBAR_F_A.data.LineaComanda;
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
 * Emite una factura rectificativa que anula a una factura previa. Siguiendo el
 * principio de Verifactu, no se borra ni modifica la original: se crea una nueva
 * factura encadenada con importes en negativo, su venta rectificativa (para que
 * caja e informes cuadren) y se devuelve el stock descontado.
 */
public class RectificacionRepository {

    public interface RectificacionCallback {
        void onExito(String ventaId, String facturaId);
        void onYaRectificada();
        void onSinTurno();
        void onError(@Nullable String mensaje);
    }

    private final FirebaseFirestore db;

    public RectificacionRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public RectificacionRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public void rectificar(Factura original, FirebaseUser user,
                           RectificacionCallback callback) {
        // 1. Evitar rectificar dos veces la misma factura.
        db.collection(FirestoreSchema.Collections.FACTURAS)
                .whereEqualTo(FirestoreSchema.Fields.FACTURA_RECTIFICADA_NUMERO,
                        original.getNumero())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap != null && !snap.isEmpty()) {
                        callback.onYaRectificada();
                        return;
                    }
                    buscarTurnoYRectificar(original, user, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    private void buscarTurnoYRectificar(Factura original, FirebaseUser user,
                                        RectificacionCallback callback) {
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
                    ejecutar(original, user, snap.getDocuments().get(0).getId(), callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    private void ejecutar(Factura original, FirebaseUser user, String turnoId,
                          RectificacionCallback callback) {
        Date ahora = new Date();
        Timestamp ts = new Timestamp(ahora);
        String anyo = new SimpleDateFormat("yyyy", Locale.ROOT).format(ahora);

        DocumentReference contadorRef = db.collection(FirestoreSchema.Collections.CONTADORES)
                .document(FirestoreSchema.Documents.CONTADOR_FACTURAS + "_" + anyo);
        DocumentReference configRef = db.collection(FirestoreSchema.Collections.CONFIGURACION)
                .document(FirestoreSchema.Documents.CONFIG_FISCAL);
        DocumentReference ventaRef = db.collection(FirestoreSchema.Collections.VENTAS).document();

        db.runTransaction(transaction -> {
                    DocumentSnapshot configDoc = transaction.get(configRef);
                    ConfiguracionFiscal config = configDoc.exists()
                            ? configDoc.toObject(ConfiguracionFiscal.class)
                            : new ConfiguracionFiscal();
                    if (config == null) config = new ConfiguracionFiscal();

                    DocumentSnapshot contador = transaction.get(contadorRef);
                    long ultimo = contador.exists()
                            && contador.getLong(FirestoreSchema.Fields.ULTIMO) != null
                            ? contador.getLong(FirestoreSchema.Fields.ULTIMO) : 0L;
                    String hashAnterior = contador.exists()
                            ? contador.getString(FirestoreSchema.Fields.HASH_ULTIMO) : "";
                    if (hashAnterior == null) hashAnterior = "";
                    int siguiente = (int) ultimo + 1;

                    List<LineaComanda> lineas = original.getLineas();

                    // Devolver stock de los productos controlados.
                    Map<DocumentReference, Long> stockDevuelto = new HashMap<>();
                    if (lineas != null) {
                        for (LineaComanda l : lineas) {
                            if (l.getCodigoBarras() == null) continue;
                            DocumentReference pRef = db
                                    .collection(FirestoreSchema.Collections.PRODUCTOS)
                                    .document(l.getCodigoBarras());
                            if (stockDevuelto.containsKey(pRef)) continue;
                            DocumentSnapshot pDoc = transaction.get(pRef);
                            if (pDoc.exists()
                                    && Boolean.TRUE.equals(pDoc.getBoolean(
                                        FirestoreSchema.Fields.CONTROLAR_STOCK))) {
                                Long actual = pDoc.getLong(FirestoreSchema.Fields.STOCK);
                                long base = actual != null ? actual : 0L;
                                stockDevuelto.put(pRef, base + l.getCantidad());
                            }
                        }
                    }

                    double total = -original.getTotal();
                    double cuotaIva = -original.getCuotaIva();
                    String numero = String.format(Locale.ROOT, "%s-%04d/%s",
                            config.getSerie(), siguiente,
                            new SimpleDateFormat("yyyy", Locale.ROOT).format(ahora));
                    String fechaIso = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(ahora);
                    String fechaQr = new SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).format(ahora);

                    String hashActual = HashVerifactu.calcular(
                            numero, fechaIso, config.getNifEmisor(), total, cuotaIva, hashAnterior);
                    String urlValidacion = GeneradorQrVerifactu.construirUrl(
                            config.getNifEmisor(), numero, fechaQr, total);

                    Factura rect = new Factura(numero, ts, config.getNifEmisor(),
                            total, cuotaIva, hashAnterior, hashActual, urlValidacion);
                    rect.setTipo(Factura.TIPO_RECTIFICATIVA);
                    rect.setFacturaRectificadaNumero(original.getNumero());
                    rect.setMetodo(original.getMetodo());
                    rect.setMesaId(original.getMesaId());
                    rect.setMesaNumero(original.getMesaNumero());
                    rect.setLineas(lineas);
                    rect.setPagoEfectivo(-original.getPagoEfectivo());
                    rect.setPagoTarjeta(-original.getPagoTarjeta());

                    String facturaId = numero.replace("/", "-");
                    DocumentReference facturaRef = db
                            .collection(FirestoreSchema.Collections.FACTURAS).document(facturaId);

                    Venta venta = new Venta(ts, total, original.getMetodo());
                    venta.setFacturaId(facturaId);
                    venta.setMesaId(original.getMesaId());
                    venta.setMesaNumero(original.getMesaNumero());
                    venta.setLineas(lineas);
                    venta.setTurnoId(turnoId);
                    venta.setUsuarioUid(user.getUid());
                    venta.setUsuarioEmail(user.getEmail());
                    venta.setPagoEfectivo(-original.getPagoEfectivo());
                    venta.setPagoTarjeta(-original.getPagoTarjeta());
                    venta.setTipo(Venta.TIPO_RECTIFICATIVA);
                    venta.setFacturaRectificadaId(original.getNumero().replace("/", "-"));

                    Map<String, Object> contadorData = new HashMap<>();
                    contadorData.put(FirestoreSchema.Fields.ULTIMO, siguiente);
                    contadorData.put(FirestoreSchema.Fields.HASH_ULTIMO, hashActual);

                    transaction.set(contadorRef, contadorData);
                    transaction.set(ventaRef, venta);
                    transaction.set(facturaRef, rect);
                    for (Map.Entry<DocumentReference, Long> e : stockDevuelto.entrySet()) {
                        transaction.update(e.getKey(),
                                FirestoreSchema.Fields.STOCK, e.getValue());
                    }

                    return new String[]{ventaRef.getId(), facturaId};
                })
                .addOnSuccessListener(ids -> callback.onExito(ids[0], ids[1]))
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }
}
