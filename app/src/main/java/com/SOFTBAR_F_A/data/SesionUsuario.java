package com.SOFTBAR_F_A.data;

/**
 * Sesion del usuario actual en memoria. Guarda el {@link Usuario} cargado tras
 * el login para consultar su rol y permisos desde cualquier pantalla.
 */
public final class SesionUsuario {

    private static Usuario actual;

    private SesionUsuario() { }

    public static void establecer(Usuario usuario) {
        actual = usuario;
    }

    public static Usuario actual() {
        return actual;
    }

    public static boolean cargada() {
        return actual != null;
    }

    public static String rol() {
        return actual != null ? actual.getRol() : Usuario.ROL_POR_DEFECTO;
    }

    public static boolean puede(String modulo) {
        return Permisos.puede(rol(), modulo);
    }

    public static void limpiar() {
        actual = null;
    }
}
