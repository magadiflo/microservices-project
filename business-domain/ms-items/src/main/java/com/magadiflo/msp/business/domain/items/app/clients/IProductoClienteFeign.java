package com.magadiflo.msp.business.domain.items.app.clients;

import com.magadiflo.msp.business.domain.items.app.models.Producto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "ms-productos", path = "/api/v1/productos")
public interface IProductoClienteFeign {
    @GetMapping
    List<Producto> listarProductos();

    @GetMapping(path = "/{id}")
    Producto verProducto(@PathVariable Long id);

    @PostMapping
    Producto crearProducto(@RequestBody Producto producto);

    @PutMapping(path = "/{id}")
    Producto actualizarProducto(@PathVariable Long id, @RequestBody Producto producto);

    @DeleteMapping(path = "/{id}")
    void eliminarProducto(@PathVariable Long id);
}
