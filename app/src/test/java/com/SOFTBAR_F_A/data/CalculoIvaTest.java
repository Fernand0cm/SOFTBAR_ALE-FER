package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalculoIvaTest {

    private static final double DELTA = 0.0;

    @Test
    public void cuotaTotal_listaNula_devuelveCero() {
        assertEquals(0.0, CalculoIva.cuotaTotal(null), DELTA);
    }

    @Test
    public void cuotaTotal_listaVacia_devuelveCero() {
        assertEquals(0.0, CalculoIva.cuotaTotal(new ArrayList<>()), DELTA);
    }

    @Test
    public void cuotaTotal_unaLineaAlDiezPorCiento() {
        // 11.00 con IVA al 10%: cuota 1.00.
        List<LineaComanda> lineas = Arrays.asList(
                new LineaComanda("1", "Menu", 11.00, 1, 0.10));
        assertEquals(1.00, CalculoIva.cuotaTotal(lineas), DELTA);
    }

    @Test
    public void cuotaTotal_tiposMixtos_sumaCadaCuota() {
        // Comida 11.00 al 10% -> 1.00; cerveza 2.42 al 21% -> 0.42.
        List<LineaComanda> lineas = Arrays.asList(
                new LineaComanda("1", "Menu", 11.00, 1, 0.10),
                new LineaComanda("2", "Cerveza", 2.42, 1, 0.21));
        assertEquals(1.42, CalculoIva.cuotaTotal(lineas), DELTA);
    }

    @Test
    public void cuotaTotal_respetaCantidadDeLaLinea() {
        // 2 cervezas de 2.42 al 21%: subtotal 4.84, cuota 0.84.
        List<LineaComanda> lineas = Arrays.asList(
                new LineaComanda("2", "Cerveza", 2.42, 2, 0.21));
        assertEquals(0.84, CalculoIva.cuotaTotal(lineas), DELTA);
    }
}
