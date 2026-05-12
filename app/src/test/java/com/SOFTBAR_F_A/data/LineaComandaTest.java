package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LineaComandaTest {

    @Test
    public void constructorVacio_dejaCamposPorDefecto() {
        LineaComanda l = new LineaComanda();
        assertNull(l.getCodigoBarras());
        assertNull(l.getNombre());
        assertEquals(0.0, l.getPrecio(), 0.0001);
        assertEquals(0, l.getCantidad());
    }

    @Test
    public void constructorConDatos_asignaTodo() {
        LineaComanda l = new LineaComanda("8410428000005", "Cana", 2.5, 3);
        assertEquals("8410428000005", l.getCodigoBarras());
        assertEquals("Cana", l.getNombre());
        assertEquals(2.5, l.getPrecio(), 0.0001);
        assertEquals(3, l.getCantidad());
    }

    @Test
    public void subtotal_multiplicaPrecioPorCantidad() {
        LineaComanda l = new LineaComanda("123", "Tortilla", 3.5, 2);
        assertEquals(7.0, l.subtotal(), 0.0001);
    }

    @Test
    public void subtotal_conCantidadCero_devuelveCero() {
        LineaComanda l = new LineaComanda("123", "Cafe", 1.8, 0);
        assertEquals(0.0, l.subtotal(), 0.0001);
    }
}
