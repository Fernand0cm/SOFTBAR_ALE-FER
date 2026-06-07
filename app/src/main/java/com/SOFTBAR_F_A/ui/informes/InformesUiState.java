package com.SOFTBAR_F_A.ui.informes;

import com.SOFTBAR_F_A.data.CalculoIva;
import com.SOFTBAR_F_A.data.Dinero;
import com.SOFTBAR_F_A.data.IndicadoresVentas;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.VentasPorProducto;

import java.util.ArrayList;
import java.util.List;

/**
 * Estado inmutable que el ViewModel publica y la pantalla de informes observa.
 * Modela los tres estados habituales de una vista que carga datos: cargando,
 * error y datos (que ademas puede estar vacio), e incluye los KPIs, el desglose
 * fiscal (base/IVA) y el ranking de productos.
 */
public final class InformesUiState {

    public enum Tipo { CARGANDO, ERROR, DATOS }

    public final Tipo tipo;
    public final String error;
    public final double total;
    public final int numeroTickets;
    public final double ticketMedio;
    public final double base;
    public final double iva;
    public final double[] ventasPorHora;
    public final List<VentasPorProducto.Item> topProductos;

    private InformesUiState(Tipo tipo, String error, double total, int numeroTickets,
                            double ticketMedio, double base, double iva,
                            double[] ventasPorHora, List<VentasPorProducto.Item> topProductos) {
        this.tipo = tipo;
        this.error = error;
        this.total = total;
        this.numeroTickets = numeroTickets;
        this.ticketMedio = ticketMedio;
        this.base = base;
        this.iva = iva;
        this.ventasPorHora = ventasPorHora;
        this.topProductos = topProductos;
    }

    public static InformesUiState cargando() {
        return new InformesUiState(Tipo.CARGANDO, null, 0, 0, 0, 0, 0,
                new double[24], new ArrayList<>());
    }

    public static InformesUiState error(String mensaje) {
        return new InformesUiState(Tipo.ERROR, mensaje, 0, 0, 0, 0, 0,
                new double[24], new ArrayList<>());
    }

    public static InformesUiState datos(List<Venta> ventas) {
        double total = IndicadoresVentas.total(ventas);
        double iva = CalculoIva.cuotaTotal(todasLasLineas(ventas));
        double base = Dinero.restar(total, iva);
        return new InformesUiState(
                Tipo.DATOS,
                null,
                total,
                IndicadoresVentas.numeroTickets(ventas),
                IndicadoresVentas.ticketMedio(ventas),
                base,
                iva,
                IndicadoresVentas.ventasPorHora(ventas),
                VentasPorProducto.agregar(ventas));
    }

    private static List<LineaComanda> todasLasLineas(List<Venta> ventas) {
        List<LineaComanda> lineas = new ArrayList<>();
        if (ventas != null) {
            for (Venta venta : ventas) {
                if (venta.getLineas() != null) lineas.addAll(venta.getLineas());
            }
        }
        return lineas;
    }

    /** Hay datos pero no hay ninguna venta en el periodo. */
    public boolean isVacio() {
        return tipo == Tipo.DATOS && numeroTickets == 0;
    }
}
