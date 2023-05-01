package com.magadiflo.msp.business.domain.items.app.service.impl;

import com.magadiflo.msp.business.domain.items.app.models.Item;
import com.magadiflo.msp.business.domain.items.app.models.Producto;
import com.magadiflo.msp.business.domain.items.app.service.IItemService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service(value = "itemServiceRestTemplate")
public class ItemServiceImpl implements IItemService {

    private static final String URL_PRODUCTOS = "http://ms-productos/api/v1/productos";
    private final RestTemplate clienteRest;

    public ItemServiceImpl(RestTemplate clienteRest) {
        this.clienteRest = clienteRest;
    }

    @Override
    public List<Item> findAll() {
        Producto[] productos = this.clienteRest.getForObject(URL_PRODUCTOS, Producto[].class);
        List<Producto> productoList = Arrays.asList(productos);
        return productoList.stream().map(producto -> new Item(producto, 1)).collect(Collectors.toList());
    }

    @Override
    public Item findByProductId(Long productId, Integer cantidad) {
        Producto producto = this.clienteRest.getForObject(URL_PRODUCTOS + "/{id}", Producto.class, Collections.singletonMap("id", productId));
        return new Item(producto, cantidad);
    }
}
