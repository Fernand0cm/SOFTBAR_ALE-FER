package com.SOFTBAR_F_A.data;

import java.util.Calendar;
import java.util.List;

/**
 * Calculos puros sobre una lista de Venta.
 * Sin dependencias de Android para poder testearlos con JUnit.
 */
public class IndicadoresVentas {

    private IndicadoresVentas() { }

    public static double total(List<Venta> ventas) {
        double total = 0;
        for (Venta v : ventas) total = Dinero.sumar(total, v.getTotal());
        return total;
    }

    public static int numeroTickets(List<Venta> ventas) {
        return ventas.size();
    }

    public static double ticketMedio(List<Venta> ventas) {
        int n = ventas.size();
        return n > 0 ? Dinero.redondear(total(ventas) / n) : 0;
    }

    /**
     * Suma de ventas por hora del dia (24 posiciones, indice = hora 0..23).
     */
    public static double[] ventasPorHora(List<Venta> ventas) {
        double[] porHora = new double[24];
        Calendar cal = Calendar.getInstance();
        for (Venta v : ventas) {
            if (v.getFecha() == null) continue;
            cal.setTime(v.getFecha().toDate());
            int hora = cal.get(Calendar.HOUR_OF_DAY);
            porHora[hora] += v.getTotal();
        }
        return porHora;
    }
}
