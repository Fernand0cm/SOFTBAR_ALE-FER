package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void bajoStock_falsoSiNoSeControla() {
        Producto p = new Producto("1", "Cafe", 1.2);
        p.setStock(0);
        p.setStockMinimo(5);
        assertFalse(p.bajoStock());
    }

    @Test
    public void bajoStock_ciertoCuandoStockEnOPorDebajoDelMinimo() {
        Producto p = new Producto("1", "Botellin", 1.5);
        p.setControlarStock(true);
        p.setStock(3);
        p.setStockMinimo(5);
        assertTrue(p.bajoStock());
    }

    @Test
    public void bajoStock_falsoCuandoStockPorEncimaDelMinimo() {
        Producto p = new Producto("1", "Botellin", 1.5);
        p.setControlarStock(true);
        p.setStock(10);
        p.setStockMinimo(5);
        assertFalse(p.bajoStock());
    }
}
