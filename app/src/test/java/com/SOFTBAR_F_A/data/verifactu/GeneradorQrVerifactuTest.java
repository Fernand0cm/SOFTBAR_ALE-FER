package com.SOFTBAR_F_A.data.verifactu;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeneradorQrVerifactuTest {

    @Test
    public void construirUrl_contieneEndpointYParametrosClave() {
        String url = GeneradorQrVerifactu.construirUrl(
                "B12345678", "0001/2026", "28-04-2026", 14.50);

        assertTrue(url.startsWith("https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR"));
        assertTrue(url.contains("nif=B12345678"));
        assertTrue(url.contains("fecha=28-04-2026"));
        assertTrue(url.contains("importe=14.50"));
    }

    @Test
    public void construirUrl_codificaCaracteresEspeciales() {
        String url = GeneradorQrVerifactu.construirUrl(
                "B12345678", "0001/2026", "28-04-2026", 14.50);
        // La barra "/" del numero se codifica como %2F
        assertTrue(url.contains("numserie=0001%2F2026"));
    }
}
