package com.SOFTBAR_F_A.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PermisosTest {

    @Test
    public void administrador_puedeTodo() {
        assertTrue(Permisos.puede(Usuario.ADMIN, Permisos.CONFIG));
        assertTrue(Permisos.puede(Usuario.ADMIN, Permisos.CAJA));
        assertTrue(Permisos.puede(Usuario.ADMIN, Permisos.STOCK));
    }

    @Test
    public void camarero_noAccedeAConfigNiCaja() {
        assertFalse(Permisos.puede(Usuario.CAMARERO, Permisos.CONFIG));
        assertFalse(Permisos.puede(Usuario.CAMARERO, Permisos.CAJA));
        assertTrue(Permisos.puede(Usuario.CAMARERO, Permisos.MESAS));
        assertTrue(Permisos.puede(Usuario.CAMARERO, Permisos.BARRA));
    }

    @Test
    public void caja_accedeACajaEInformesPeroNoConfig() {
        assertTrue(Permisos.puede(Usuario.CAJA, Permisos.CAJA));
        assertTrue(Permisos.puede(Usuario.CAJA, Permisos.INFORMES));
        assertFalse(Permisos.puede(Usuario.CAJA, Permisos.CONFIG));
    }

    @Test
    public void cocina_soloMesasEHistorial() {
        assertTrue(Permisos.puede(Usuario.COCINA, Permisos.MESAS));
        assertTrue(Permisos.puede(Usuario.COCINA, Permisos.HISTORIAL));
        assertFalse(Permisos.puede(Usuario.COCINA, Permisos.BARRA));
        assertFalse(Permisos.puede(Usuario.COCINA, Permisos.CONFIG));
    }

    @Test
    public void rolNuloODesconocido_caeEnPermisosMinimos() {
        assertFalse(Permisos.puede(null, Permisos.CONFIG));
        assertFalse(Permisos.puede("inventado", Permisos.CAJA));
    }
}
