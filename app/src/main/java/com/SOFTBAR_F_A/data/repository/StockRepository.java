package com.SOFTBAR_F_A.data.repository;

import com.SOFTBAR_F_A.data.Producto;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso al stock de productos. Escucha el catalogo y devuelve solo los
 * productos con control de stock activado (filtrado en cliente para no exigir
 * indice compuesto). Permite fijar el stock de un producto.
 */
public class StockRepository {

    public interface StockListener {
        void onProductos(List<Producto> productos);
        void onError(String mensaje);
    }

    private final FirebaseFirestore db;

    public StockRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public StockRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public ListenerRegistration escucharStock(StockListener listener) {
        return db.collection(FirestoreSchema.Collections.PRODUCTOS)
                .orderBy(FirestoreSchema.Fields.NOMBRE, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        listener.onError(error.getLocalizedMessage());
                        return;
                    }
                    if (snap == null) return;
                    List<Producto> conStock = new ArrayList<>();
                    for (Producto p : snap.toObjects(Producto.class)) {
                        if (p.isControlarStock()) conStock.add(p);
                    }
                    listener.onProductos(conStock);
                });
    }

    /** Fija el stock de un producto (nunca por debajo de 0). */
    public void fijarStock(String codigoBarras, int nuevoStock) {
        db.collection(FirestoreSchema.Collections.PRODUCTOS)
                .document(codigoBarras)
                .update(FirestoreSchema.Fields.STOCK, Math.max(0, nuevoStock));
    }
}
