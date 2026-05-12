package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComandaTest {

    @Test
    public void constructorVacio_dejaCamposPorDefecto() {
        Comanda c = new Comanda();
        assertNull(c.getMesaId());
        assertEquals(0, c.getMesaNumero());
        assertNull(c.getEstado());
        assertNotNull(c.getLineas());
        assertTrue(c.getLineas().isEmpty());
    }

    @Test
    public void constructorConMesa_creaComandaAbierta() {
        Comanda c = new Comanda("m-3", 3);
        assertEquals("m-3", c.getMesaId());
        assertEquals(3, c.getMesaNumero());
        assertEquals(Comanda.ABIERTA, c.getEstado());
        assertNotNull(c.getFechaApertura());
    }

    @Test
    public void setLineas_nulo_dejaListaVaciaEnVezDeNull() {
        Comanda c = new Comanda("m-1", 1);
        c.setLineas(null);
        assertNotNull(c.getLineas());
        assertTrue(c.getLineas().isEmpty());
    }

    @Test
    public void constantesEstado_sonValoresEsperados() {
        assertEquals("abierta", Comanda.ABIERTA);
        assertEquals("pagada", Comanda.PAGADA);
    }
}
