package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DineroTest {

    private static final double DELTA = 0.0;

    @Test
    public void redondear_aDosDecimalesHalfUp() {
        assertEquals(2.35, Dinero.redondear(2.345), DELTA);
        assertEquals(2.34, Dinero.redondear(2.344), DELTA);
    }

    @Test
    public void sumar_evitaErrorDeComaFlotante() {
        // 0.1 + 0.2 con double da 0.30000000000000004; Dinero debe dar 0.30.
        assertEquals(0.30, Dinero.sumar(0.1, 0.2), DELTA);
    }

    @Test
    public void sumar_variosImportes() {
        assertEquals(6.00, Dinero.sumar(1.00, 2.00, 3.00), DELTA);
    }

    @Test
    public void restar_redondeaAResultadoLimpio() {
        assertEquals(0.10, Dinero.restar(0.30, 0.20), DELTA);
    }

    @Test
    public void multiplicar_precioPorCantidad() {
        assertEquals(3.30, Dinero.multiplicar(1.10, 3), DELTA);
    }

    @Test
    public void cuotaIvaIncluido_diez_porciento() {
        // Total 11.00 con IVA incluido al 10%: base 10.00, cuota 1.00.
        assertEquals(1.00, Dinero.cuotaIvaIncluido(11.00, 0.10), DELTA);
    }

    @Test
    public void cuotaIvaIncluido_redondeaADosDecimales() {
        // Total 12.50 al 10%: base 11.3636..., cuota 1.1363... -> 1.14.
        assertEquals(1.14, Dinero.cuotaIvaIncluido(12.50, 0.10), DELTA);
    }
}
