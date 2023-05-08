package com.magadiflo.msp.business.domain.items.app.clients;

import com.magadiflo.msp.business.domain.items.app.models.Producto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-productos", path = "/api/v1/productos")
public interface IProductoClienteFeign {
    @GetMapping
    List<Producto> listarProductos();

    @GetMapping(path = "/{id}")
    Producto verProducto(@PathVariable Long id);
}
