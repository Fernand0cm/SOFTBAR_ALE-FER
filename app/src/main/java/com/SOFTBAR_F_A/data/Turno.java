package com.SOFTBAR_F_A.data;

import com.google.firebase.Timestamp;

public class Turno {

    public static final String ABIERTO = "abierto";
    public static final String CERRADO = "cerrado";

    private Timestamp fechaApertura;
    private Timestamp fechaCierre;
    private String estado;
    private double importeInicial;
    private double efectivoContado;
    private double efectivoEsperado;
    private double diferenciaCaja;
    private String usuarioUid;
    private String usuarioEmail;

    public Turno() {
        // Necesario para Firestore
    }

    public Turno(Timestamp fechaApertura, double importeInicial,
                 String usuarioUid, String usuarioEmail) {
        this.fechaApertura = fechaApertura;
        this.importeInicial = importeInicial;
        this.usuarioUid = usuarioUid;
        this.usuarioEmail = usuarioEmail;
        this.estado = ABIERTO;
    }

    public Timestamp getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Timestamp fechaApertura) { this.fechaApertura = fechaApertura; }

    public Timestamp getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(Timestamp fechaCierre) { this.fechaCierre = fechaCierre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getImporteInicial() { return importeInicial; }
    public void setImporteInicial(double importeInicial) { this.importeInicial = importeInicial; }

    public double getEfectivoContado() { return efectivoContado; }
    public void setEfectivoContado(double efectivoContado) { this.efectivoContado = efectivoContado; }

    public double getEfectivoEsperado() { return efectivoEsperado; }
    public void setEfectivoEsperado(double efectivoEsperado) { this.efectivoEsperado = efectivoEsperado; }

    public double getDiferenciaCaja() { return diferenciaCaja; }
    public void setDiferenciaCaja(double diferenciaCaja) { this.diferenciaCaja = diferenciaCaja; }

    public String getUsuarioUid() { return usuarioUid; }
    public void setUsuarioUid(String usuarioUid) { this.usuarioUid = usuarioUid; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
}
