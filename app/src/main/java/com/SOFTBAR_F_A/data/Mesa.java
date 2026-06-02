package com.SOFTBAR_F_A.data;

public class Mesa {

    public static final String LIBRE = "libre";
    public static final String OCUPADA = "ocupada";
    public static final String COBRO = "cobro";
    public static final String CERRADA = "cerrada";

    private int numero;
    private String estado;
    private String comandaActivaId;
    private double posX;
    private double posY;

    public Mesa() {
        // Necesario para Firestore
    }

    public Mesa(int numero, String estado) {
        this.numero = numero;
        this.estado = estado;
        this.posX = 0;
        this.posY = 0;
    }

    public Mesa(int numero, String estado, double posX, double posY) {
        this.numero = numero;
        this.estado = estado;
        this.posX = posX;
        this.posY = posY;
    }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getComandaActivaId() { return comandaActivaId; }
    public void setComandaActivaId(String comandaActivaId) {
        this.comandaActivaId = comandaActivaId;
    }

    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }
}