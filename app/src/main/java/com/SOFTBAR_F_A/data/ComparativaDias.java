package com.SOFTBAR_F_A.data;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Calcula el total vendido por dia en una ventana de los ultimos N dias,
 * para la grafica comparativa de informes.
 *
 * Calculo puro y determinista (recibe la fecha de referencia), sin
 * dependencias de Android: testeable con JUnit.
 */
public final class ComparativaDias {

    private ComparativaDias() { }

    /**
     * Devuelve un array de {@code dias} posiciones con el total de cada dia.
     * La ultima posicion corresponde al dia de {@code referencia} (hoy) y la
     * primera al dia mas antiguo de la ventana.
     */
    public static double[] totalesUltimosDias(List<Venta> ventas, int dias, Date referencia) {
        double[] totales = new double[dias];
        if (ventas == null || dias <= 0) return totales;

        Calendar hoy = inicioDelDia(referencia);
        for (Venta venta : ventas) {
            if (venta.getFecha() == null) continue;
            Calendar diaVenta = inicioDelDia(venta.getFecha().toDate());
            long diff = diferenciaEnDias(diaVenta, hoy);
            if (diff >= 0 && diff < dias) {
                int indice = dias - 1 - (int) diff;
                totales[indice] = Dinero.sumar(totales[indice], venta.getTotal());
            }
        }
        return totales;
    }

    private static long diferenciaEnDias(Calendar desde, Calendar hasta) {
        long ms = hasta.getTimeInMillis() - desde.getTimeInMillis();
        return Math.round(ms / (1000d * 60 * 60 * 24));
    }

    private static Calendar inicioDelDia(Date fecha) {
        Calendar c = Calendar.getInstance();
        c.setTime(fecha);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }
}
