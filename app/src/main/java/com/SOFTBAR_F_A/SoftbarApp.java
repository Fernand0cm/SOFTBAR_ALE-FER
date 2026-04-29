package com.SOFTBAR_F_A;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

public class SoftbarApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Cache local persistente: la app sigue funcionando sin red
        // y las escrituras se sincronizan cuando vuelve la conexion.
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }
}
