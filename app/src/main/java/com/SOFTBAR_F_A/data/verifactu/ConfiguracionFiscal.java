package com.SOFTBAR_F_A.data.verifactu;

public class ConfiguracionFiscal {

    public static final String NIF_EMISOR_DEFECTO = "B12345678";
    public static final String SERIE_DEFECTO = "A";

    private String nifEmisor;
    private String serie;

    public ConfiguracionFiscal() {
        nifEmisor = NIF_EMISOR_DEFECTO;
        serie = SERIE_DEFECTO;
    }

    public String getNifEmisor() {
        return nifEmisor != null && !nifEmisor.trim().isEmpty()
                ? nifEmisor.trim()
                : NIF_EMISOR_DEFECTO;
    }

    public void setNifEmisor(String nifEmisor) { this.nifEmisor = nifEmisor; }

    public String getSerie() {
        return serie != null && !serie.trim().isEmpty()
                ? serie.trim()
                : SERIE_DEFECTO;
    }

    public void setSerie(String serie) { this.serie = serie; }
}
