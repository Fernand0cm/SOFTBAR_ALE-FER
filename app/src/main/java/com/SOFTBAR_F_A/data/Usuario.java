package com.SOFTBAR_F_A.data;

/**
 * Usuario interno de la aplicacion, con su rol. Se guarda en la coleccion
 * {@code usuarios} de Firestore, usando el UID de Firebase Auth como id.
 */
public class Usuario {

    public static final String ADMIN = "administrador";
    public static final String CAMARERO = "camarero";
    public static final String CAJA = "caja";
    public static final String COCINA = "cocina";

    /** Rol que se asigna a un usuario nuevo que se registra por si mismo. */
    public static final String ROL_POR_DEFECTO = CAMARERO;

    private String email;
    private String nombre;
    private String rol;

    public Usuario() {
        // Necesario para Firestore
    }

    public Usuario(String email, String nombre, String rol) {
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() {
        return rol != null && !rol.trim().isEmpty() ? rol.trim() : ROL_POR_DEFECTO;
    }

    public void setRol(String rol) { this.rol = rol; }
}
