package com.SOFTBAR_F_A.data.verifactu;

import java.util.Locale;

/**
 * Formato del numero de factura y de su identificador de documento.
 *
 * El numero visible sigue el patron {@code SERIE-0001/ANYO} (correlativo de 4
 * digitos por anio). El id de documento sustituye la barra por guion para poder
 * usarlo como id en Firestore. Logica pura: testeable con JUnit.
 */
public final class NumeroFactura {

    private NumeroFactura() { }

    public static String generar(String serie, int siguiente, String anyo) {
        return String.format(Locale.ROOT, "%s-%04d/%s", serie, siguiente, anyo);
    }

    /** Convierte "A-0001/2026" en "A-0001-2026" para usarlo como id de documento. */
    public static String aIdDocumento(String numero) {
        return numero != null ? numero.replace("/", "-") : null;
    }
}
