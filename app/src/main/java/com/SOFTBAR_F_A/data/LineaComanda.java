package com.SOFTBAR_F_A.data;

public class LineaComanda {

    private String codigoBarras;
    private String nombre;
    private double precio;
    private int cantidad;

    public LineaComanda() {
        // Necesario para Firestore
    }

    public LineaComanda(String codigoBarras, String nombre, double precio, int cantidad) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double subtotal() {
        return precio * cantidad;
    }
}
