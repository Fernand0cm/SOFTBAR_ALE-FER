package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ComparativaDiasTest {

    private Date dia(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 12, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Venta ventaEn(Date fecha, double total) {
        Venta v = new Venta();
        v.setFecha(new Timestamp(fecha));
        v.setTotal(total);
        return v;
    }

    @Test
    public void totales_listaNula_devuelveArrayDeCeros() {
        double[] r = ComparativaDias.totalesUltimosDias(null, 7, dia(2026, 5, 10));
        assertEquals(7, r.length);
        assertEquals(0.0, r[6], 0.0001);
    }

    @Test
    public void totales_colocaCadaVentaEnSuDia() {
        Date referencia = dia(2026, 5, 10); // hoy
        List<Venta> ventas = new ArrayList<>();
        ventas.add(ventaEn(dia(2026, 5, 10), 20.0)); // hoy -> ultima posicion
        ventas.add(ventaEn(dia(2026, 5, 9), 5.0));   // ayer
        ventas.add(ventaEn(dia(2026, 5, 9), 5.0));   // ayer (suma)

        double[] r = ComparativaDias.totalesUltimosDias(ventas, 7, referencia);

        assertEquals(20.0, r[6], 0.0001); // hoy
        assertEquals(10.0, r[5], 0.0001); // ayer
        assertEquals(0.0, r[4], 0.0001);
    }

    @Test
    public void totales_ignoraVentasFueraDeVentana() {
        Date referencia = dia(2026, 5, 10);
        List<Venta> ventas = new ArrayList<>();
        ventas.add(ventaEn(dia(2026, 5, 1), 99.0)); // 9 dias antes, fuera de 7

        double[] r = ComparativaDias.totalesUltimosDias(ventas, 7, referencia);

        for (double v : r) assertEquals(0.0, v, 0.0001);
    }
}
