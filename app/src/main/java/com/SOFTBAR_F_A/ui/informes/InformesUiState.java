package com.SOFTBAR_F_A.ui.informes;

import com.SOFTBAR_F_A.data.IndicadoresVentas;
import com.SOFTBAR_F_A.data.Venta;

import java.util.List;

/**
 * Estado inmutable que el ViewModel publica y la pantalla de informes observa.
 * Modela los tres estados habituales de una vista que carga datos: cargando,
 * error y datos (que ademas puede estar vacio).
 */
public final class InformesUiState {

    public enum Tipo { CARGANDO, ERROR, DATOS }

    public final Tipo tipo;
    public final String error;
    public final double total;
    public final int numeroTickets;
    public final double ticketMedio;
    public final double[] ventasPorHora;

    private InformesUiState(Tipo tipo, String error, double total, int numeroTickets,
                            double ticketMedio, double[] ventasPorHora) {
        this.tipo = tipo;
        this.error = error;
        this.total = total;
        this.numeroTickets = numeroTickets;
        this.ticketMedio = ticketMedio;
        this.ventasPorHora = ventasPorHora;
    }

    public static InformesUiState cargando() {
        return new InformesUiState(Tipo.CARGANDO, null, 0, 0, 0, new double[24]);
    }

    public static InformesUiState error(String mensaje) {
        return new InformesUiState(Tipo.ERROR, mensaje, 0, 0, 0, new double[24]);
    }

    public static InformesUiState datos(List<Venta> ventas) {
        return new InformesUiState(
                Tipo.DATOS,
                null,
                IndicadoresVentas.total(ventas),
                IndicadoresVentas.numeroTickets(ventas),
                IndicadoresVentas.ticketMedio(ventas),
                IndicadoresVentas.ventasPorHora(ventas));
    }

    /** Hay datos pero no hay ninguna venta en el dia. */
    public boolean isVacio() {
        return tipo == Tipo.DATOS && numeroTickets == 0;
    }
}
