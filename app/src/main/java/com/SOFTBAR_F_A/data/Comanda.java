package com.SOFTBAR_F_A.data;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Comanda {

    public static final String ABIERTA = "abierta";
    public static final String PAGADA = "pagada";

    private String mesaId;
    private int mesaNumero;
    private String estado;
    private Timestamp fechaApertura;
    private List<LineaComanda> lineas = new ArrayList<>();
    private int comensales;
    public Comanda() {
        // Necesario para Firestore
    }

    public Comanda(String mesaId, int mesaNumero) {
        this.mesaId = mesaId;
        this.mesaNumero = mesaNumero;
        this.estado = ABIERTA;
        this.fechaApertura = Timestamp.now();
        this.comensales = 1;
    }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public int getMesaNumero() { return mesaNumero; }
    public void setMesaNumero(int mesaNumero) { this.mesaNumero = mesaNumero; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Timestamp fechaApertura) { this.fechaApertura = fechaApertura; }

    public int getComensales() {
        return comensales;
    }

    public void setComensales(int comensales) {
        this.comensales = comensales;
    }

    public List<LineaComanda> getLineas() { return lineas; }
    public void setLineas(List<LineaComanda> lineas) {
        this.lineas = lineas != null ? lineas : new ArrayList<>();
    }
}
