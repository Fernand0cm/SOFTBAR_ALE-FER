package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertEquals;

import com.SOFTBAR_F_A.R;

import org.junit.Test;

public class EstadoMesaColorTest {

    @Test
    public void libre_devuelveColorLibre() {
        assertEquals(R.color.mesa_libre, EstadoMesaColor.colorPara(Mesa.LIBRE));
    }

    @Test
    public void ocupada_devuelveColorOcupada() {
        assertEquals(R.color.mesa_ocupada, EstadoMesaColor.colorPara(Mesa.OCUPADA));
    }

    @Test
    public void cobro_devuelveColorCobro() {
        assertEquals(R.color.mesa_cobro, EstadoMesaColor.colorPara(Mesa.COBRO));
    }

    @Test
    public void cerrada_devuelveColorCerrada() {
        assertEquals(R.color.mesa_cerrada, EstadoMesaColor.colorPara(Mesa.CERRADA));
    }

    @Test
    public void estadoNuloODesconocido_caeAColorCerrada() {
        assertEquals(R.color.mesa_cerrada, EstadoMesaColor.colorPara(null));
        assertEquals(R.color.mesa_cerrada, EstadoMesaColor.colorPara("loquesea"));
    }
}
