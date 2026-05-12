package com.SOFTBAR_F_A.data.verifactu;

import com.google.firebase.Timestamp;

import com.SOFTBAR_F_A.data.LineaComanda;

import java.util.ArrayList;
import java.util.List;

/**
 * Factura simplificada en formato Verifactu.
 * Cada factura encadena su hash con el de la anterior, formando una cadena
 * verificable por la AEAT.
 */
public class Factura {

    private String numero;            // ej. "0001/2026"
    private Timestamp fecha;
    private String nifEmisor;
    private double total;
    private double cuotaIva;          // calculada al 10% por defecto
    private String hashAnterior;      // hash de la factura previa (vacio en la primera)
    private String hashActual;        // hash de esta factura
    private String urlValidacion;     // URL del QR Verifactu
    private String metodo;
    private String mesaId;
    private int mesaNumero;
    private List<LineaComanda> lineas = new ArrayList<>();

    public Factura() {
        // Necesario para Firestore
    }

    public Factura(String numero, Timestamp fecha, String nifEmisor,
                   double total, double cuotaIva,
                   String hashAnterior, String hashActual, String urlValidacion) {
        this.numero = numero;
        this.fecha = fecha;
        this.nifEmisor = nifEmisor;
        this.total = total;
        this.cuotaIva = cuotaIva;
        this.hashAnterior = hashAnterior;
        this.hashActual = hashActual;
        this.urlValidacion = urlValidacion;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getNifEmisor() { return nifEmisor; }
    public void setNifEmisor(String nifEmisor) { this.nifEmisor = nifEmisor; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(double cuotaIva) { this.cuotaIva = cuotaIva; }

    public String getHashAnterior() { return hashAnterior; }
    public void setHashAnterior(String hashAnterior) { this.hashAnterior = hashAnterior; }

    public String getHashActual() { return hashActual; }
    public void setHashActual(String hashActual) { this.hashActual = hashActual; }

    public String getUrlValidacion() { return urlValidacion; }
    public void setUrlValidacion(String urlValidacion) { this.urlValidacion = urlValidacion; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public int getMesaNumero() { return mesaNumero; }
    public void setMesaNumero(int mesaNumero) { this.mesaNumero = mesaNumero; }

    public List<LineaComanda> getLineas() { return lineas; }
    public void setLineas(List<LineaComanda> lineas) {
        this.lineas = lineas != null ? lineas : new ArrayList<>();
    }
}
