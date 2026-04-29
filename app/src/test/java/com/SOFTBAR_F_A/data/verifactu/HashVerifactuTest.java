package com.SOFTBAR_F_A.data.verifactu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HashVerifactuTest {

    @Test
    public void calcular_devuelveHashHexadecimalDe64Caracteres() {
        String h = HashVerifactu.calcular("0001/2026", "2026-04-28",
                "B12345678", 14.50, 1.32, "");
        assertEquals(64, h.length());
        assertTrue(h.matches("[0-9a-f]+"));
    }

    @Test
    public void calcular_esDeterminista() {
        String h1 = HashVerifactu.calcular("0001/2026", "2026-04-28",
                "B12345678", 14.50, 1.32, "");
        String h2 = HashVerifactu.calcular("0001/2026", "2026-04-28",
                "B12345678", 14.50, 1.32, "");
        assertEquals(h1, h2);
    }

    @Test
    public void calcular_cambiaSiCambiaUnImporte() {
        String h1 = HashVerifactu.calcular("0001/2026", "2026-04-28",
                "B12345678", 14.50, 1.32, "");
        String h2 = HashVerifactu.calcular("0001/2026", "2026-04-28",
                "B12345678", 14.51, 1.32, "");
        assertNotEquals(h1, h2);
    }

    @Test
    public void calcular_encadenaConHashAnterior() {
        String h1 = HashVerifactu.calcular("0001/2026", "2026-04-28",
                "B12345678", 14.50, 1.32, "");
        String h2SinCadena = HashVerifactu.calcular("0002/2026", "2026-04-28",
                "B12345678", 20.00, 1.82, "");
        String h2Encadenado = HashVerifactu.calcular("0002/2026", "2026-04-28",
                "B12345678", 20.00, 1.82, h1);
        assertNotEquals(h2SinCadena, h2Encadenado);
    }

    @Test
    public void formatoImporte_redondeaA2Decimales() {
        assertEquals("14.50", HashVerifactu.formatoImporte(14.5));
        assertEquals("0.10", HashVerifactu.formatoImporte(0.1));
        assertEquals("1.33", HashVerifactu.formatoImporte(1.333));
    }
}
