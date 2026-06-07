package com.SOFTBAR_F_A.data.repository;

import com.SOFTBAR_F_A.data.Usuario;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Acceso al perfil del usuario interno. Carga el documento de la coleccion
 * {@code usuarios} y, si no existe, lo crea con el rol por defecto (camarero).
 * La asignacion de roles superiores la realiza un administrador.
 */
public class UsuarioRepository {

    public interface UsuarioCallback {
        void onUsuario(Usuario usuario);
        void onError(String mensaje);
    }

    private final FirebaseFirestore db;

    public UsuarioRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public UsuarioRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public void cargarOCrear(FirebaseUser user, UsuarioCallback callback) {
        DocumentReference ref = db.collection(FirestoreSchema.Collections.USUARIOS)
                .document(user.getUid());

        ref.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        callback.onUsuario(usuario != null ? usuario : porDefecto(user));
                        return;
                    }
                    Usuario nuevo = porDefecto(user);
                    ref.set(nuevo)
                            .addOnSuccessListener(x -> callback.onUsuario(nuevo))
                            .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    private Usuario porDefecto(FirebaseUser user) {
        String email = user.getEmail() != null ? user.getEmail() : "";
        String nombre = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        return new Usuario(email, nombre, Usuario.ROL_POR_DEFECTO);
    }
}
