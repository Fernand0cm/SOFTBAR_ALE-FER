package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VentasPorProductoTest {

    private Venta ventaCon(LineaComanda... lineas) {
        Venta v = new Venta();
        v.setLineas(new ArrayList<>(Arrays.asList(lineas)));
        return v;
    }

    @Test
    public void agregar_listaNula_devuelveListaVacia() {
        assertTrue(VentasPorProducto.agregar(null).isEmpty());
    }

    @Test
    public void agregar_sumaCantidadesEImportesDelMismoProducto() {
        List<Venta> ventas = Arrays.asList(
                ventaCon(new LineaComanda("1", "Cafe", 1.5, 2)),
                ventaCon(new LineaComanda("1", "Cafe", 1.5, 3)));

        List<VentasPorProducto.Item> items = VentasPorProducto.agregar(ventas);

        assertEquals(1, items.size());
        assertEquals("Cafe", items.get(0).nombre);
        assertEquals(5, items.get(0).cantidad);
        assertEquals(7.5, items.get(0).importe, 0.0001);
    }

    @Test
    public void agregar_ordenaDeMasAMenosVendido() {
        List<Venta> ventas = Arrays.asList(
                ventaCon(
                        new LineaComanda("1", "Cafe", 1.5, 2),
                        new LineaComanda("2", "Cerveza", 2.0, 5)));

        List<VentasPorProducto.Item> items = VentasPorProducto.agregar(ventas);

        assertEquals(2, items.size());
        assertEquals("Cerveza", items.get(0).nombre);
        assertEquals(5, items.get(0).cantidad);
        assertEquals("Cafe", items.get(1).nombre);
    }
}
