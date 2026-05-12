package com.SOFTBAR_F_A.data;

import java.util.List;

/**
 * Calculos puros para el resumen de la caja del turno.
 * Sin dependencias de Android para poder testearlos con JUnit.
 */
public class ResumenCaja {

    private final double apertura;
    private final double ventasEfectivo;
    private final double ventasTarjeta;
    private final double otrosIngresos;
    private final double retiradas;

    public ResumenCaja(double apertura, double ventasEfectivo, double ventasTarjeta,
                      double otrosIngresos, double retiradas) {
        this.apertura = apertura;
        this.ventasEfectivo = ventasEfectivo;
        this.ventasTarjeta = ventasTarjeta;
        this.otrosIngresos = otrosIngresos;
        this.retiradas = retiradas;
    }

    public double getApertura() { return apertura; }
    public double getVentasEfectivo() { return ventasEfectivo; }
    public double getVentasTarjeta() { return ventasTarjeta; }
    public double getOtrosIngresos() { return otrosIngresos; }
    public double getRetiradas() { return retiradas; }

    public double efectivoEsperado() {
        return apertura + ventasEfectivo + otrosIngresos - retiradas;
    }

    public double totalEsperado() {
        return efectivoEsperado();
    }

    public double diferencia(double efectivoContado) {
        return efectivoContado - efectivoEsperado();
    }

    public static ResumenCaja calcular(List<Venta> ventasDelTurno,
                                       List<MovimientoCaja> movimientosDelTurno,
                                       String etiquetaEfectivo,
                                       String etiquetaTarjeta) {
        double efectivo = 0;
        double tarjeta = 0;
        if (ventasDelTurno != null) {
            for (Venta v : ventasDelTurno) {
                if (v.getMetodo() == null) continue;
                if (v.getMetodo().equalsIgnoreCase(etiquetaEfectivo)) {
                    efectivo += v.getTotal();
                } else if (v.getMetodo().equalsIgnoreCase(etiquetaTarjeta)) {
                    tarjeta += v.getTotal();
                } else {
                    // El cobro mixto u otros metodos cuentan como ventas en efectivo
                    // para la caja, por simplicidad.
                    efectivo += v.getTotal();
                }
            }
        }

        double apertura = 0;
        double otrosIngresos = 0;
        double retiradas = 0;
        if (movimientosDelTurno != null) {
            for (MovimientoCaja m : movimientosDelTurno) {
                if (m.getTipo() == null) continue;
                switch (m.getTipo()) {
                    case MovimientoCaja.APERTURA:
                        apertura += m.getImporte();
                        break;
                    case MovimientoCaja.ENTRADA:
                        otrosIngresos += m.getImporte();
                        break;
                    case MovimientoCaja.RETIRADA:
                        retiradas += Math.abs(m.getImporte());
                        break;
                }
            }
        }

        return new ResumenCaja(apertura, efectivo, tarjeta, otrosIngresos, retiradas);
    }
}
