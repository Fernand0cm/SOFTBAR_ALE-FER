package com.SOFTBAR_F_A.data;

public class Mesa {

    public static final String LIBRE = "libre";
    public static final String OCUPADA = "ocupada";
    public static final String COBRO = "cobro";
    public static final String CERRADA = "cerrada";

    private int numero;
    private String estado;
    private String comandaActivaId;

    public Mesa() {
        // Necesario para Firestore
    }

    public Mesa(int numero, String estado) {
        this.numero = numero;
        this.estado = estado;
    }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getComandaActivaId() { return comandaActivaId; }
    public void setComandaActivaId(String comandaActivaId) {
        this.comandaActivaId = comandaActivaId;
    }
}
