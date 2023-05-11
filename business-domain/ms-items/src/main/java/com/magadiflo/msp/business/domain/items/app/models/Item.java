package com.magadiflo.msp.business.domain.items.app.models;

import com.magadiflo.msp.shared.library.commons.app.models.entity.Producto;

public class Item {
    private Producto producto;
    private Integer cantidad;

    public Item() {
    }

    public Item(Producto producto, Integer cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    //-- Método auxiliar
    public Double getTotal() {
        return this.producto.getPrecio() * this.cantidad.doubleValue();
    }
}
