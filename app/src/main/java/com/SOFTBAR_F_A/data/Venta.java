package com.SOFTBAR_F_A.data;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Venta {

    private Timestamp fecha;
    private double total;
    private String metodo;
    private String facturaId;
    private String comandaId;
    private String mesaId;
    private int mesaNumero;
    private String turnoId;
    private String usuarioUid;
    private String usuarioEmail;
    private double pagoEfectivo;
    private double pagoTarjeta;
    private double importeRecibido;
    private double cambio;
    private List<LineaComanda> lineas = new ArrayList<>();

    public Venta() {
        // Necesario para Firestore
    }
    private int comensales;

    public Venta(Timestamp fecha, double total, String metodo) {
        this.fecha = fecha;
        this.total = total;
        this.metodo = metodo;
    }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public String getFacturaId() { return facturaId; }
    public void setFacturaId(String facturaId) { this.facturaId = facturaId; }

    public String getComandaId() { return comandaId; }
    public void setComandaId(String comandaId) { this.comandaId = comandaId; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public int getMesaNumero() { return mesaNumero; }
    public void setMesaNumero(int mesaNumero) { this.mesaNumero = mesaNumero; }

    public String getTurnoId() { return turnoId; }
    public void setTurnoId(String turnoId) { this.turnoId = turnoId; }

    public String getUsuarioUid() { return usuarioUid; }
    public void setUsuarioUid(String usuarioUid) { this.usuarioUid = usuarioUid; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public double getPagoEfectivo() { return pagoEfectivo; }
    public void setPagoEfectivo(double pagoEfectivo) { this.pagoEfectivo = pagoEfectivo; }

    public double getPagoTarjeta() { return pagoTarjeta; }
    public void setPagoTarjeta(double pagoTarjeta) { this.pagoTarjeta = pagoTarjeta; }

    public double getImporteRecibido() { return importeRecibido; }
    public void setImporteRecibido(double importeRecibido) { this.importeRecibido = importeRecibido; }

    public double getCambio() { return cambio; }
    public void setCambio(double cambio) { this.cambio = cambio; }

    public int getComensales() { return comensales; }

    public void setComensales(int comensales) { this.comensales = comensales;}

    public List<LineaComanda> getLineas() { return lineas; }
    public void setLineas(List<LineaComanda> lineas) {
        this.lineas = lineas != null ? lineas : new ArrayList<>();
    }
}
