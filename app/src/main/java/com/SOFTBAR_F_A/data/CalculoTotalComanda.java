package com.SOFTBAR_F_A.data;

import java.util.List;

/**
 * Calculos puros sobre las lineas de una comanda.
 * Sin dependencias de Android para poder testearlos con JUnit.
 */
public class CalculoTotalComanda {

    private CalculoTotalComanda() { }

    public static double total(List<LineaComanda> lineas) {
        if (lineas == null) return 0;
        double total = 0;
        for (LineaComanda l : lineas) {
            total = Dinero.sumar(total, l.subtotal());
        }
        return total;
    }

    public static int numeroArticulos(List<LineaComanda> lineas) {
        if (lineas == null) return 0;
        int n = 0;
        for (LineaComanda l : lineas) n += l.getCantidad();
        return n;
    }
}
