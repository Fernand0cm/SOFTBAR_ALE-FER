package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class IndicadoresVentasTest {

    private Venta venta(double total, int hora) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hora);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return new Venta(new Timestamp(c.getTime()), total, "Efectivo");
    }

    @Test
    public void total_listaVacia_devuelveCero() {
        assertEquals(0.0, IndicadoresVentas.total(Collections.emptyList()), 0.0001);
    }

    @Test
    public void total_sumaCorrectamente() {
        List<Venta> ventas = Arrays.asList(venta(10.0, 12), venta(5.5, 13));
        assertEquals(15.5, IndicadoresVentas.total(ventas), 0.0001);
    }

    @Test
    public void numeroTickets_cuentaElementos() {
        List<Venta> ventas = Arrays.asList(venta(1, 10), venta(2, 11), venta(3, 12));
        assertEquals(3, IndicadoresVentas.numeroTickets(ventas));
    }

    @Test
    public void ticketMedio_listaVacia_devuelveCero() {
        assertEquals(0.0, IndicadoresVentas.ticketMedio(Collections.emptyList()), 0.0001);
    }

    @Test
    public void ticketMedio_calculaPromedio() {
        List<Venta> ventas = Arrays.asList(venta(10.0, 12), venta(20.0, 13));
        assertEquals(15.0, IndicadoresVentas.ticketMedio(ventas), 0.0001);
    }

    @Test
    public void ventasPorHora_distribuyeEnIndicesCorrectos() {
        List<Venta> ventas = Arrays.asList(
                venta(10.0, 9),
                venta(5.0, 9),
                venta(20.0, 14)
        );
        double[] result = IndicadoresVentas.ventasPorHora(ventas);

        assertEquals(24, result.length);
        assertEquals(15.0, result[9], 0.0001);
        assertEquals(20.0, result[14], 0.0001);
        assertEquals(0.0, result[10], 0.0001);
    }

    @Test
    public void ventasPorHora_ignoraVentasSinFecha() {
        Venta sinFecha = new Venta(null, 99.0, "Tarjeta");
        double[] result = IndicadoresVentas.ventasPorHora(Collections.singletonList(sinFecha));
        for (double v : result) assertEquals(0.0, v, 0.0001);
    }
}
