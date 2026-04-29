package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProductoTest {

    @Test
    public void constructorVacio_dejaCamposPorDefecto() {
        Producto p = new Producto();
        assertEquals(null, p.getCodigoBarras());
        assertEquals(null, p.getNombre());
        assertEquals(0.0, p.getPrecio(), 0.0001);
    }

    @Test
    public void constructorConDatos_asignaTodo() {
        Producto p = new Producto("8410428000005", "Cana", 2.5);
        assertEquals("8410428000005", p.getCodigoBarras());
        assertEquals("Cana", p.getNombre());
        assertEquals(2.5, p.getPrecio(), 0.0001);
    }

    @Test
    public void setters_actualizanCampos() {
        Producto p = new Producto();
        p.setCodigoBarras("123");
        p.setNombre("Tortilla");
        p.setPrecio(3.5);

        assertEquals("123", p.getCodigoBarras());
        assertEquals("Tortilla", p.getNombre());
        assertEquals(3.5, p.getPrecio(), 0.0001);
    }
}
