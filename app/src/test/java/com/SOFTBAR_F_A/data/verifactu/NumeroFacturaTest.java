package com.SOFTBAR_F_A.data.verifactu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumeroFacturaTest {

    @Test
    public void generar_rellenaA4Digitos() {
        assertEquals("A-0001/2026", NumeroFactura.generar("A", 1, "2026"));
    }

    @Test
    public void generar_mantieneNumerosLargos() {
        assertEquals("A-1234/2026", NumeroFactura.generar("A", 1234, "2026"));
    }

    @Test
    public void generar_respetaLaSerie() {
        assertEquals("B-0007/2025", NumeroFactura.generar("B", 7, "2025"));
    }

    @Test
    public void aIdDocumento_sustituyeBarraPorGuion() {
        assertEquals("A-0001-2026", NumeroFactura.aIdDocumento("A-0001/2026"));
    }

    @Test
    public void aIdDocumento_nuloDevuelveNulo() {
        assertEquals(null, NumeroFactura.aIdDocumento(null));
    }
}
