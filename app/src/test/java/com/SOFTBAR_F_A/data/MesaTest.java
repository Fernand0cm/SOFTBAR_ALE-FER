package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MesaTest {

    @Test
    public void constructorVacio_dejaCamposPorDefecto() {
        Mesa m = new Mesa();
        assertEquals(0, m.getNumero());
        assertNull(m.getEstado());
        assertNull(m.getComandaActivaId());
    }

    @Test
    public void constructorConDatos_asignaNumeroYEstado() {
        Mesa m = new Mesa(3, Mesa.OCUPADA);
        assertEquals(3, m.getNumero());
        assertEquals(Mesa.OCUPADA, m.getEstado());
    }

    @Test
    public void setters_actualizanCampos() {
        Mesa m = new Mesa();
        m.setNumero(7);
        m.setEstado(Mesa.COBRO);
        m.setComandaActivaId("c-123");

        assertEquals(7, m.getNumero());
        assertEquals(Mesa.COBRO, m.getEstado());
        assertEquals("c-123", m.getComandaActivaId());
    }

    @Test
    public void constantesEstado_sonValoresEsperados() {
        assertEquals("libre", Mesa.LIBRE);
        assertEquals("ocupada", Mesa.OCUPADA);
        assertEquals("cobro", Mesa.COBRO);
        assertEquals("cerrada", Mesa.CERRADA);
    }
}
