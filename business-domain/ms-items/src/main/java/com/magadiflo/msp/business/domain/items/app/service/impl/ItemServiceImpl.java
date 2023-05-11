package com.magadiflo.msp.business.domain.items.app.service.impl;

import com.magadiflo.msp.business.domain.items.app.models.Item;
import com.magadiflo.msp.business.domain.items.app.service.IItemService;
import com.magadiflo.msp.shared.library.commons.app.models.entity.Producto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    @Override
    public Producto save(Producto producto) {
        HttpEntity<Producto> body = new HttpEntity<>(producto);
        ResponseEntity<Producto> exchange = this.clienteRest.exchange(URL_PRODUCTOS, HttpMethod.POST, body, Producto.class);
        return exchange.getBody();
    }

    @Override
    public Producto update(Long id, Producto producto) {
        HttpEntity<Producto> body = new HttpEntity<>(producto);
        ResponseEntity<Producto> exchange = this.clienteRest.exchange(URL_PRODUCTOS + "/{id}", HttpMethod.PUT, body, Producto.class, Collections.singletonMap("id", id));
        return exchange.getBody();
    }

    @Override
    public void delete(Long id) {
        this.clienteRest.delete(URL_PRODUCTOS + "/{id}", Collections.singletonMap("id", id));
    }
}
