package com.SOFTBAR_F_A.data.verifactu;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Calcula el hash SHA-256 de cada factura encadenado con el de la anterior,
 * tal como exige el sistema Verifactu de la AEAT.
 *
 * Formato simplificado de la cadena a hashear:
 *   numero|fechaIso|nifEmisor|total|cuotaIva|hashAnterior
 */
public class HashVerifactu {

    private HashVerifactu() { }

    public static String calcular(String numero,
                                  String fechaIso,
                                  String nifEmisor,
                                  double total,
                                  double cuotaIva,
                                  String hashAnterior) {
        String entrada = numero + "|" + fechaIso + "|" + nifEmisor + "|"
                + formatoImporte(total) + "|" + formatoImporte(cuotaIva) + "|"
                + (hashAnterior == null ? "" : hashAnterior);
        return sha256Hex(entrada);
    }

    static String formatoImporte(double importe) {
        return String.format(Locale.ROOT, "%.2f", importe);
    }

    static String sha256Hex(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
