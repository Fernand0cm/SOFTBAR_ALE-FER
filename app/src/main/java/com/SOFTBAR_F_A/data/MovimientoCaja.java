package com.SOFTBAR_F_A.data;

import com.google.firebase.Timestamp;

public class MovimientoCaja {

    public static final String APERTURA = "apertura";
    public static final String RETIRADA = "retirada";
    public static final String ENTRADA = "entrada";

    private Timestamp fecha;
    private String tipo;
    private double importe;
    private String descripcion;

    public MovimientoCaja() {
        // Necesario para Firestore
    }

    public MovimientoCaja(Timestamp fecha, String tipo, double importe, String descripcion) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.importe = importe;
        this.descripcion = descripcion;
    }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
