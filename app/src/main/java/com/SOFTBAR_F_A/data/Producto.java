package com.SOFTBAR_F_A.data;

public class Producto {

    /** Tipo de IVA aplicado por defecto (general de hosteleria, 10%). */
    public static final double IVA_POR_DEFECTO = 0.10;

    private String codigoBarras;
    private String nombre;
    private double precio;
    private double tipoIva = IVA_POR_DEFECTO;
    private boolean activo = true;

    public Producto() {
        // Necesario para Firestore
    }

    public Producto(String codigoBarras, String nombre, double precio) {
        this(codigoBarras, nombre, precio, IVA_POR_DEFECTO);
    }

    public Producto(String codigoBarras, String nombre, double precio, double tipoIva) {
        this(codigoBarras, nombre, precio, tipoIva, true);
    }

    public Producto(String codigoBarras, String nombre, double precio,
                    double tipoIva, boolean activo) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.precio = precio;
        this.tipoIva = tipoIva;
        this.activo = activo;
    }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    /**
     * Tipo de IVA del producto. Si no se ha definido (productos antiguos sin el
     * campo) se devuelve el tipo general por defecto.
     */
    public double getTipoIva() {
        return tipoIva > 0 ? tipoIva : IVA_POR_DEFECTO;
    }

    public void setTipoIva(double tipoIva) { this.tipoIva = tipoIva; }

    /**
     * Indica si el producto esta disponible para la venta. Los productos no se
     * borran (para no perder su rastro), se desactivan.
     */
    public boolean isActivo() { return activo; }

    public void setActivo(boolean activo) { this.activo = activo; }
}
