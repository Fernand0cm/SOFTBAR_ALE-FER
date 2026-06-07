package com.SOFTBAR_F_A.data.repository;

import com.SOFTBAR_F_A.data.Turno;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Acceso a los turnos cerrados para la consulta de cierres historicos.
 *
 * Consulta por estado y ordena en cliente por fecha de cierre (descendente)
 * para no exigir un indice compuesto de Firestore.
 */
public class CierresRepository {

    public interface CierresListener {
        void onCierres(List<Turno> cierres);
        void onError(String mensaje);
    }

    private final FirebaseFirestore db;

    public CierresRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public CierresRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public ListenerRegistration escucharCierres(CierresListener listener) {
        return db.collection(FirestoreSchema.Collections.TURNOS)
                .whereEqualTo(FirestoreSchema.Fields.ESTADO, Turno.CERRADO)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        listener.onError(error.getLocalizedMessage());
                        return;
                    }
                    if (snap == null) return;
                    List<Turno> cierres = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        cierres.add(doc.toObject(Turno.class));
                    }
                    Collections.sort(cierres, new Comparator<Turno>() {
                        @Override
                        public int compare(Turno a, Turno b) {
                            Timestamp fa = a.getFechaCierre();
                            Timestamp fb = b.getFechaCierre();
                            if (fa == null && fb == null) return 0;
                            if (fa == null) return 1;
                            if (fb == null) return -1;
                            return fb.compareTo(fa);
                        }
                    });
                    listener.onCierres(cierres);
                });
    }
}
