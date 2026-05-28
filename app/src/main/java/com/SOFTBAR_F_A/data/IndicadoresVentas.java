package com.SOFTBAR_F_A.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculos puros sobre una lista de Venta.
 * Sin dependencias de Android para poder testearlos con JUnit.
 */
public class IndicadoresVentas {

    private IndicadoresVentas() { }

    public static double total(List<Venta> ventas) {
        double total = 0;
        for (Venta v : ventas) total += v.getTotal();
        return total;
    }

    public static int numeroTickets(List<Venta> ventas) {
        return ventas.size();
    }

    public static double ticketMedio(List<Venta> ventas) {
        int n = ventas.size();
        return n > 0 ? total(ventas) / n : 0;
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

    public static List<ProductoVendido> ventasPorProducto(List<Venta> ventas) {
        Map<String, ProductoVendido> mapa = new HashMap<>();

        for (Venta venta : ventas) {
            if (venta.getLineas() == null) continue;

            for (LineaComanda linea : venta.getLineas()) {
                if (linea == null || linea.getNombre() == null) continue;

                String codigo = linea.getCodigoBarras();
                String clave = codigo != null && !codigo.trim().isEmpty()
                        ? codigo
                        : linea.getNombre();

                ProductoVendido actual = mapa.get(clave);

                if (actual == null) {
                    actual = new ProductoVendido(
                            linea.getNombre(),
                            linea.getCantidad(),
                            linea.subtotal());
                    mapa.put(clave, actual);
                } else {
                    actual.cantidad += linea.getCantidad();
                    actual.total += linea.subtotal();
                }
            }
        }

        List<ProductoVendido> resultado = new ArrayList<>(mapa.values());

        resultado.sort((a, b) -> Double.compare(b.total, a.total));

        return resultado;
    }

    public static class ProductoVendido {
        private final String nombre;
        private int cantidad;
        private double total;

        public ProductoVendido(String nombre, int cantidad, double total) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.total = total;
        }

        public String getNombre() {
            return nombre;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getTotal() {
            return total;
        }
    }
}