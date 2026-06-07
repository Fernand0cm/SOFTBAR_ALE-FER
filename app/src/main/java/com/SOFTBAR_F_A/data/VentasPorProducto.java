package com.SOFTBAR_F_A.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrega las lineas de un conjunto de ventas por producto, sumando unidades e
 * importe. El resultado sale ordenado de mas a menos vendido, lo que sirve a la
 * vez para "ventas por producto" y "productos mas vendidos".
 *
 * Calculo puro, sin dependencias de Android: testeable con JUnit.
 */
public final class VentasPorProducto {

    private VentasPorProducto() { }

    public static class Item {
        public final String nombre;
        public final int cantidad;
        public final double importe;

        public Item(String nombre, int cantidad, double importe) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.importe = importe;
        }
    }

    public static List<Item> agregar(List<Venta> ventas) {
        Map<String, int[]> cantidades = new LinkedHashMap<>();
        Map<String, Double> importes = new LinkedHashMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();

        if (ventas != null) {
            for (Venta venta : ventas) {
                if (venta.getLineas() == null) continue;
                for (LineaComanda linea : venta.getLineas()) {
                    String clave = linea.getCodigoBarras() != null
                            ? linea.getCodigoBarras()
                            : linea.getNombre();
                    if (clave == null) continue;

                    int[] acc = cantidades.get(clave);
                    if (acc == null) {
                        cantidades.put(clave, new int[]{linea.getCantidad()});
                        importes.put(clave, linea.subtotal());
                        nombres.put(clave, linea.getNombre());
                    } else {
                        acc[0] += linea.getCantidad();
                        importes.put(clave, Dinero.sumar(importes.get(clave), linea.subtotal()));
                    }
                }
            }
        }

        List<Item> items = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : cantidades.entrySet()) {
            String clave = entry.getKey();
            items.add(new Item(nombres.get(clave), entry.getValue()[0], importes.get(clave)));
        }

        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                if (b.cantidad != a.cantidad) return Integer.compare(b.cantidad, a.cantidad);
                return Double.compare(b.importe, a.importe);
            }
        });
        return items;
    }
}
