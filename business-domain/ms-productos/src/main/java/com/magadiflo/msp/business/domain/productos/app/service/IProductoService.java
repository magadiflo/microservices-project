package com.magadiflo.msp.business.domain.productos.app.service;

import com.magadiflo.msp.shared.library.commons.app.models.entity.Producto;

import java.util.List;

public interface IProductoService {
    List<Producto> findAll();

    Producto findById(Long id);

    Producto save(Producto producto);

    void deleteById(Long id);
}
