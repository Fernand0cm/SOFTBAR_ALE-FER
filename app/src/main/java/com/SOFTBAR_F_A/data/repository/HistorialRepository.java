package com.SOFTBAR_F_A.data.repository;

import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a las ventas para el historial de tickets. Aisla la consulta a
 * Firestore: la UI solo recibe la lista de ventas (con su id para abrir el
 * ticket) o un error.
 */
public class HistorialRepository {

    public interface HistorialListener {
        void onVentas(List<VentaItem> ventas);
        void onError(String mensaje);
    }

    /** Venta junto a su identificador de documento, necesario para abrir el ticket. */
    public static class VentaItem {
        public final String id;
        public final Venta venta;

        public VentaItem(String id, Venta venta) {
            this.id = id;
            this.venta = venta;
        }
    }

    private final FirebaseFirestore db;

    public HistorialRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public HistorialRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Escucha en tiempo real las ultimas ventas, mas recientes primero.
     * Devuelve el registro para poder cancelar la suscripcion.
     */
    public ListenerRegistration escucharHistorial(int limite, HistorialListener listener) {
        return db.collection(FirestoreSchema.Collections.VENTAS)
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.DESCENDING)
                .limit(limite)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        listener.onError(error.getLocalizedMessage());
                        return;
                    }
                    if (snap == null) return;
                    List<VentaItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        items.add(new VentaItem(doc.getId(), doc.toObject(Venta.class)));
                    }
                    listener.onVentas(items);
                });
    }
}
