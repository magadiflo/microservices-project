package com.magadiflo.msp.business.domain.items.app.service.impl;

import com.magadiflo.msp.business.domain.items.app.clients.IProductoClienteFeign;
import com.magadiflo.msp.business.domain.items.app.models.Item;
import com.magadiflo.msp.business.domain.items.app.models.Producto;
import com.magadiflo.msp.business.domain.items.app.service.IItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service(value = "itemServiceFeign")
public class ItemServiceFeign implements IItemService {
    private final IProductoClienteFeign productoClienteFeign;

    public ItemServiceFeign(IProductoClienteFeign productoClienteFeign) {
        this.productoClienteFeign = productoClienteFeign;
    }

    @Override
    public List<Item> findAll() {
        return this.productoClienteFeign.listarProductos().stream().map(producto -> new Item(producto, 1)).collect(Collectors.toList());
    }

    @Override
    public Item findByProductId(Long productId, Integer cantidad) {
        Producto producto = this.productoClienteFeign.verProducto(productId);
        return new Item(producto, cantidad);
    }
}
