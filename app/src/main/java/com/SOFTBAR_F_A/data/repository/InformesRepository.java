package com.SOFTBAR_F_A.data.repository;

import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.List;

/**
 * Acceso a las ventas para la pantalla de informes. Aisla la consulta a
 * Firestore de la UI: el ViewModel solo recibe la lista de ventas o un error.
 */
public class InformesRepository {

    public interface VentasListener {
        void onVentas(List<Venta> ventas);
        void onError(String mensaje);
    }

    private final FirebaseFirestore db;

    public InformesRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public InformesRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Escucha en tiempo real las ventas desde el inicio del dia actual.
     * Devuelve el registro para poder cancelar la suscripcion.
     */
    public ListenerRegistration escucharVentasDelDia(VentasListener listener) {
        Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);

        return db.collection(FirestoreSchema.Collections.VENTAS)
                .whereGreaterThanOrEqualTo(
                        FirestoreSchema.Fields.FECHA, new Timestamp(inicio.getTime()))
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        listener.onError(error.getLocalizedMessage());
                        return;
                    }
                    if (snap == null) return;
                    listener.onVentas(snap.toObjects(Venta.class));
                });
    }
}
