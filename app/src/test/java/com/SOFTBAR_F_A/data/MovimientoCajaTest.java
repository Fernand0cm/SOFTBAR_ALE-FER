package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.firebase.Timestamp;

import org.junit.Test;

public class MovimientoCajaTest {

    @Test
    public void constructorVacio_dejaCamposPorDefecto() {
        MovimientoCaja m = new MovimientoCaja();
        assertNull(m.getFecha());
        assertNull(m.getTipo());
        assertEquals(0.0, m.getImporte(), 0.0001);
        assertNull(m.getDescripcion());
    }

    @Test
    public void constructorConDatos_asignaTodo() {
        Timestamp t = Timestamp.now();
        MovimientoCaja m = new MovimientoCaja(t, MovimientoCaja.APERTURA, 100.0, "Inicio");
        assertNotNull(m.getFecha());
        assertEquals(MovimientoCaja.APERTURA, m.getTipo());
        assertEquals(100.0, m.getImporte(), 0.0001);
        assertEquals("Inicio", m.getDescripcion());
    }

    @Test
    public void constantesTipo_sonValoresEsperados() {
        assertEquals("apertura", MovimientoCaja.APERTURA);
        assertEquals("retirada", MovimientoCaja.RETIRADA);
        assertEquals("entrada", MovimientoCaja.ENTRADA);
    }
}
