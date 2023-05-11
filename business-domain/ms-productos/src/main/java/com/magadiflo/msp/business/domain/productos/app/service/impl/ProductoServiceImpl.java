package com.magadiflo.msp.business.domain.productos.app.service.impl;

import com.magadiflo.msp.business.domain.productos.app.repository.IProductoRepository;
import com.magadiflo.msp.business.domain.productos.app.service.IProductoService;
import com.magadiflo.msp.shared.library.commons.app.models.entity.Producto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoServiceImpl implements IProductoService {
    private final IProductoRepository productoRepository;

    public ProductoServiceImpl(IProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return (List<Producto>) this.productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto findById(Long id) {
        return this.productoRepository.findById(id).orElseGet(() -> null);
    }

    @Override
    @Transactional
    public Producto save(Producto producto) {
        return this.productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        this.productoRepository.deleteById(id);
    }
}
