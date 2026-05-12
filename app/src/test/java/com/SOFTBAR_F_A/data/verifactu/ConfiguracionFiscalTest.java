package com.SOFTBAR_F_A.data.verifactu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfiguracionFiscalTest {

    @Test
    public void valoresPorDefecto_sonValidosParaDemo() {
        ConfiguracionFiscal config = new ConfiguracionFiscal();

        assertEquals("B12345678", config.getNifEmisor());
        assertEquals("A", config.getSerie());
    }

    @Test
    public void getters_limpianEspaciosYRespetanValor() {
        ConfiguracionFiscal config = new ConfiguracionFiscal();
        config.setNifEmisor(" B87654321 ");
        config.setSerie(" B ");

        assertEquals("B87654321", config.getNifEmisor());
        assertEquals("B", config.getSerie());
    }

    @Test
    public void getters_recuperanDefectoSiElValorEstaVacio() {
        ConfiguracionFiscal config = new ConfiguracionFiscal();
        config.setNifEmisor(" ");
        config.setSerie("");

        assertEquals("B12345678", config.getNifEmisor());
        assertEquals("A", config.getSerie());
    }
}
