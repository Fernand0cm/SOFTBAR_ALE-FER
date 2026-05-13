package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResumenCajaTest {

    @Test
    public void totalEsperado_noIncluyeVentasTarjeta() {
        ResumenCaja r = new ResumenCaja(100, 200, 150, 10, 30);
        assertEquals(280.0, r.totalEsperado(), 0.0001);
        assertEquals(280.0, r.efectivoEsperado(), 0.0001);
    }

    @Test
    public void diferencia_restaEsperadoAlContado() {
        ResumenCaja r = new ResumenCaja(100, 200, 150, 10, 30);
        assertEquals(20.0, r.diferencia(300), 0.0001);
    }

    @Test
    public void calcular_listasNulas_devuelveCero() {
        ResumenCaja r = ResumenCaja.calcular(null, null, "Efectivo", "Tarjeta");
        assertEquals(0.0, r.getApertura(), 0.0001);
        assertEquals(0.0, r.getVentasEfectivo(), 0.0001);
        assertEquals(0.0, r.getVentasTarjeta(), 0.0001);
        assertEquals(0.0, r.totalEsperado(), 0.0001);
    }

    @Test
    public void calcular_separaEfectivoYTarjeta() {
        Timestamp t = Timestamp.now();
        List<Venta> ventas = Arrays.asList(
                new Venta(t, 10.0, "Efectivo"),
                new Venta(t, 5.5, "Tarjeta"),
                new Venta(t, 7.0, "Efectivo")
        );
        ResumenCaja r = ResumenCaja.calcular(ventas, Collections.emptyList(),
                "Efectivo", "Tarjeta");
        assertEquals(17.0, r.getVentasEfectivo(), 0.0001);
        assertEquals(5.5, r.getVentasTarjeta(), 0.0001);
    }

    @Test
    public void calcular_mixtoCuentaComoEfectivo() {
        Timestamp t = Timestamp.now();
        List<Venta> ventas = Collections.singletonList(new Venta(t, 14.5, "Mixto"));
        ResumenCaja r = ResumenCaja.calcular(ventas, Collections.emptyList(),
                "Efectivo", "Tarjeta");
        assertEquals(14.5, r.getVentasEfectivo(), 0.0001);
        assertEquals(0.0, r.getVentasTarjeta(), 0.0001);
    }

    @Test
    public void calcular_usaDesgloseDePagoSiExiste() {
        Timestamp t = Timestamp.now();
        Venta venta = new Venta(t, 20.0, "Mixto");
        venta.setPagoEfectivo(8.0);
        venta.setPagoTarjeta(12.0);

        ResumenCaja r = ResumenCaja.calcular(Collections.singletonList(venta),
                Collections.emptyList(), "Efectivo", "Tarjeta");

        assertEquals(8.0, r.getVentasEfectivo(), 0.0001);
        assertEquals(12.0, r.getVentasTarjeta(), 0.0001);
        assertEquals(8.0, r.efectivoEsperado(), 0.0001);
    }

    @Test
    public void calcular_sumaMovimientosPorTipo() {
        Timestamp t = Timestamp.now();
        List<MovimientoCaja> movs = Arrays.asList(
                new MovimientoCaja(t, MovimientoCaja.APERTURA, 100.0, ""),
                new MovimientoCaja(t, MovimientoCaja.ENTRADA, 20.0, ""),
                new MovimientoCaja(t, MovimientoCaja.RETIRADA, 50.0, "")
        );
        ResumenCaja r = ResumenCaja.calcular(Collections.emptyList(), movs,
                "Efectivo", "Tarjeta");
        assertEquals(100.0, r.getApertura(), 0.0001);
        assertEquals(20.0, r.getOtrosIngresos(), 0.0001);
        assertEquals(50.0, r.getRetiradas(), 0.0001);
        assertEquals(70.0, r.totalEsperado(), 0.0001);
    }

    @Test
    public void calcular_retiradaConImporteNegativo_seToma_enValorAbsoluto() {
        Timestamp t = Timestamp.now();
        List<MovimientoCaja> movs = Collections.singletonList(
                new MovimientoCaja(t, MovimientoCaja.RETIRADA, -30.0, "")
        );
        ResumenCaja r = ResumenCaja.calcular(Collections.emptyList(), movs,
                "Efectivo", "Tarjeta");
        assertEquals(30.0, r.getRetiradas(), 0.0001);
    }
}
