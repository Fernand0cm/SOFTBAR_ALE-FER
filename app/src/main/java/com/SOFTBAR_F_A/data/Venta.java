package com.SOFTBAR_F_A.data;

import com.google.firebase.Timestamp;

public class Venta {

    private Timestamp fecha;
    private double total;
    private String metodo;

    public Venta() {
        // Necesario para Firestore
    }

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
}
