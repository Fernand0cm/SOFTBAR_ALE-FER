package com.SOFTBAR_F_A.data;

public class Producto {

    private String codigoBarras;
    private String nombre;
    private double precio;

    public Producto() {
        // Necesario para Firestore
    }

    public Producto(String codigoBarras, String nombre, double precio) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.precio = precio;
    }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}
