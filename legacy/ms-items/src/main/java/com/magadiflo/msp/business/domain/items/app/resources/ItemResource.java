package com.magadiflo.msp.business.domain.items.app.resources;

import com.magadiflo.msp.business.domain.items.app.models.Item;
import com.magadiflo.msp.business.domain.items.app.models.Producto;
import com.magadiflo.msp.business.domain.items.app.service.IItemService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemResource {
    private final IItemService itemService;

    public ItemResource(@Qualifier(value = "itemServiceFeign") IItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(this.itemService.findAll());
    }

    @HystrixCommand(fallbackMethod = "metodoAlternativo")
    @GetMapping(path = "/producto/{productoId}/cantidad/{cantidad}")
    public ResponseEntity<Item> getItem(@PathVariable Long productoId, @PathVariable Integer cantidad) {
        return ResponseEntity.ok(this.itemService.findByProductId(productoId, cantidad));
    }

    public ResponseEntity<Item> metodoAlternativo(Long productoId, Integer cantidad) {
        Producto producto = new Producto();
        producto.setId(productoId);
        producto.setNombre("Cámara Sony");
        producto.setPrecio(500D);

        Item item = new Item();
        item.setCantidad(cantidad);
        item.setProducto(producto);

        return ResponseEntity.ok(item);
    }
}