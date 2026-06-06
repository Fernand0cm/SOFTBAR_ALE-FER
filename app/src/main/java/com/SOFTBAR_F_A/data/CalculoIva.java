package com.SOFTBAR_F_A.data;

import java.util.List;

/**
 * Calculo de la cuota de IVA de una venta con tipos mixtos: cada linea aporta su
 * propia cuota segun su tipo (10%, 21%, 4%...), y el total es la suma de todas.
 *
 * Trabaja sobre importes con IVA incluido (el precio de venta al publico ya lo
 * lleva) y delega el redondeo a {@link Dinero}. Sin dependencias de Android para
 * poder probarse con JUnit.
 */
public final class CalculoIva {

    private CalculoIva() { }

    /** Cuota total de IVA contenida en las lineas, cada una a su tipo. */
    public static double cuotaTotal(List<LineaComanda> lineas) {
        if (lineas == null) return 0;
        double cuota = 0;
        for (LineaComanda l : lineas) {
            cuota = Dinero.sumar(cuota,
                    Dinero.cuotaIvaIncluido(l.subtotal(), l.getTipoIva()));
        }
        return cuota;
    }
}
