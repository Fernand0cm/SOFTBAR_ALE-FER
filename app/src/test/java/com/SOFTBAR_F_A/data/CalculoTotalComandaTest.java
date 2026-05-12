package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CalculoTotalComandaTest {

    @Test
    public void total_listaVacia_devuelveCero() {
        assertEquals(0.0, CalculoTotalComanda.total(Collections.emptyList()), 0.0001);
    }

    @Test
    public void total_listaNula_devuelveCero() {
        assertEquals(0.0, CalculoTotalComanda.total(null), 0.0001);
    }

    @Test
    public void total_sumaSubtotalesCorrectamente() {
        List<LineaComanda> lineas = Arrays.asList(
                new LineaComanda("1", "Cana", 2.5, 2),
                new LineaComanda("2", "Tortilla", 3.5, 1),
                new LineaComanda("3", "Cafe", 1.8, 1)
        );
        // 5.0 + 3.5 + 1.8 = 10.3
        assertEquals(10.3, CalculoTotalComanda.total(lineas), 0.0001);
    }

    @Test
    public void numeroArticulos_sumaCantidades() {
        List<LineaComanda> lineas = Arrays.asList(
                new LineaComanda("1", "Cana", 2.5, 2),
                new LineaComanda("2", "Tortilla", 3.5, 3)
        );
        assertEquals(5, CalculoTotalComanda.numeroArticulos(lineas));
    }

    @Test
    public void numeroArticulos_listaNula_devuelveCero() {
        assertEquals(0, CalculoTotalComanda.numeroArticulos(null));
    }
}
