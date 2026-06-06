package com.SOFTBAR_F_A.data;

import java.io.Serializable;

public class LineaComanda implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigoBarras;
    private String nombre;
    private double precio;
    private int cantidad;
    private double tipoIva = Producto.IVA_POR_DEFECTO;

    public LineaComanda() {
        // Necesario para Firestore
    }

    public LineaComanda(String codigoBarras, String nombre, double precio, int cantidad) {
        this(codigoBarras, nombre, precio, cantidad, Producto.IVA_POR_DEFECTO);
    }

    public LineaComanda(String codigoBarras, String nombre, double precio,
                        int cantidad, double tipoIva) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.tipoIva = tipoIva;
    }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    /**
     * Tipo de IVA de la linea. Si no se definio (lineas antiguas) se devuelve el
     * tipo general por defecto.
     */
    public double getTipoIva() {
        return tipoIva > 0 ? tipoIva : Producto.IVA_POR_DEFECTO;
    }

    public void setTipoIva(double tipoIva) { this.tipoIva = tipoIva; }

    public double subtotal() {
        return Dinero.multiplicar(precio, cantidad);
    }
}
