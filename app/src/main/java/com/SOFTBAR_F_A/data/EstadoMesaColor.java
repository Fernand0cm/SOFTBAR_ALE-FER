package com.SOFTBAR_F_A.data;

import com.SOFTBAR_F_A.R;

/**
 * Mapea el estado textual de una mesa al recurso de color de la paleta.
 */
public class EstadoMesaColor {

    private EstadoMesaColor() { }

    public static int colorPara(String estado) {
        if (estado == null) return R.color.mesa_cerrada;
        switch (estado) {
            case Mesa.LIBRE:    return R.color.mesa_libre;
            case Mesa.OCUPADA:  return R.color.mesa_ocupada;
            case Mesa.COBRO:    return R.color.mesa_cobro;
            case Mesa.CERRADA:  return R.color.mesa_cerrada;
            default:            return R.color.mesa_cerrada;
        }
    }
}
