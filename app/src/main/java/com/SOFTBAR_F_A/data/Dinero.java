package com.SOFTBAR_F_A.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidades de calculo monetario basadas en {@link BigDecimal} con redondeo a
 * 2 decimales (HALF_UP, el redondeo "comercial").
 *
 * El objetivo es evitar los errores de coma flotante del tipo {@code double} al
 * operar con importes, IVA y cambios (por ejemplo {@code 0.1 + 0.2 != 0.3}).
 * Toda operacion monetaria de la app deberia pasar por aqui antes de mostrarse
 * o persistirse.
 *
 * No depende de Android: se puede probar con JUnit puro.
 */
public final class Dinero {

    public static final int ESCALA = 2;
    public static final RoundingMode MODO = RoundingMode.HALF_UP;

    private Dinero() { }

    /** Redondea un importe a 2 decimales (HALF_UP). */
    public static double redondear(double importe) {
        return escalar(BigDecimal.valueOf(importe)).doubleValue();
    }

    /** Suma varios importes y redondea el resultado a 2 decimales. */
    public static double sumar(double... importes) {
        BigDecimal acc = BigDecimal.ZERO;
        for (double i : importes) {
            acc = acc.add(BigDecimal.valueOf(i));
        }
        return escalar(acc).doubleValue();
    }

    /** Resta {@code b} a {@code a} y redondea a 2 decimales. */
    public static double restar(double a, double b) {
        return escalar(BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)))
                .doubleValue();
    }

    /** Multiplica un precio unitario por una cantidad y redondea a 2 decimales. */
    public static double multiplicar(double precio, int cantidad) {
        return escalar(BigDecimal.valueOf(precio)
                .multiply(BigDecimal.valueOf(cantidad))).doubleValue();
    }

    /**
     * Cuota de IVA contenida en un total con impuestos incluidos:
     * <pre>cuota = total - total / (1 + tipo)</pre>
     * Se calcula con precision intermedia alta y se redondea a 2 decimales.
     */
    public static double cuotaIvaIncluido(double totalConIva, double tipoIva) {
        BigDecimal total = BigDecimal.valueOf(totalConIva);
        BigDecimal divisor = BigDecimal.ONE.add(BigDecimal.valueOf(tipoIva));
        BigDecimal base = total.divide(divisor, 10, MODO);
        return escalar(total.subtract(base)).doubleValue();
    }

    private static BigDecimal escalar(BigDecimal valor) {
        return valor.setScale(ESCALA, MODO);
    }
}
