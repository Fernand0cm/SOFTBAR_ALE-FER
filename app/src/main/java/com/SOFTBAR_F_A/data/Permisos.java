package com.SOFTBAR_F_A.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Matriz de permisos: que modulos puede usar cada rol. El administrador puede
 * todo; el resto tiene un subconjunto.
 *
 * Logica pura sin dependencias de Android: testeable con JUnit.
 */
public final class Permisos {

    public static final String TURNO = "turno";
    public static final String MESAS = "mesas";
    public static final String BARRA = "barra";
    public static final String CAJA = "caja";
    public static final String INFORMES = "informes";
    public static final String STOCK = "stock";
    public static final String HISTORIAL = "historial";
    public static final String CONFIG = "config";

    private static final Map<String, Set<String>> MATRIZ = new HashMap<>();

    static {
        MATRIZ.put(Usuario.CAJA, new HashSet<>(Arrays.asList(
                TURNO, MESAS, BARRA, CAJA, INFORMES, HISTORIAL)));
        MATRIZ.put(Usuario.CAMARERO, new HashSet<>(Arrays.asList(
                MESAS, BARRA, HISTORIAL)));
        MATRIZ.put(Usuario.COCINA, new HashSet<>(Arrays.asList(
                MESAS, HISTORIAL)));
    }

    private Permisos() { }

    public static boolean puede(String rol, String modulo) {
        String r = (rol != null && !rol.trim().isEmpty()) ? rol.trim() : Usuario.ROL_POR_DEFECTO;
        if (Usuario.ADMIN.equals(r)) return true;
        Set<String> permitidos = MATRIZ.getOrDefault(r, Collections.emptySet());
        return permitidos.contains(modulo);
    }
}
