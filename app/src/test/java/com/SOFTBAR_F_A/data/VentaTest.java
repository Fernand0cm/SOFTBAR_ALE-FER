package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Date;

public class VentaTest {

    @Test
    public void constructorConDatos_asignaCampos() {
        Timestamp t = new Timestamp(new Date(1700000000000L));
        Venta v = new Venta(t, 14.5, "Efectivo");

        assertEquals(t, v.getFecha());
        assertEquals(14.5, v.getTotal(), 0.0001);
        assertEquals("Efectivo", v.getMetodo());
    }

    @Test
    public void setters_actualizanCampos() {
        Venta v = new Venta();
        Timestamp t = Timestamp.now();
        v.setFecha(t);
        v.setTotal(20.0);
        v.setMetodo("Tarjeta");

        assertNotNull(v.getFecha());
        assertEquals(20.0, v.getTotal(), 0.0001);
        assertEquals("Tarjeta", v.getMetodo());
    }
}
