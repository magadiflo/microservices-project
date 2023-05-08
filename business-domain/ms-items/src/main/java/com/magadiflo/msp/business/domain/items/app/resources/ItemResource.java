package com.magadiflo.msp.business.domain.items.app.resources;

import com.magadiflo.msp.business.domain.items.app.models.Item;
import com.magadiflo.msp.business.domain.items.app.models.Producto;
import com.magadiflo.msp.business.domain.items.app.service.IItemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemResource {
    private final IItemService itemService;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private static final Logger LOG = LoggerFactory.getLogger(ItemResource.class);

    public ItemResource(@Qualifier(value = "itemServiceFeign") IItemService itemService, CircuitBreakerFactory circuitBreakerFactory) {
        this.itemService = itemService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    /**
     * Los parámetros @RequestParam String nombre, @RequestHeader(name = "token-req") String token,
     * son enviados desde el filtro de fábrica configurado en el application.yml del microservicio
     * Spring Cloud Gateway. Los colocamos como requeridos en false, ya que si por alguna razón
     * no se envían no nos lance un error.
     * <p>
     * Como el ms Spring Cloud Gateway tiene configurados en su application.yml los filtros de
     * fábrica (AddRequestHeader, AddResponseHeader, AddRequestParameter) y en ellos definidos
     * valores predeterminados. Cuando se llame a este endPoint (/api/v1/items) para listar
     * todos los items pero a través del Spring Cloud Gateway, es que se agregarán dichos
     * filtros al request (o response) por eso es que aquí los podemos capturar.
     * <p>
     * En los otros métodos handler como el getItem(...) también podríamos colocar estos parámetros
     * y capturar sus valores. Pero, ojo, como el getItem(...) tiene un método alternativo (fallbackMethod = "metodoAlternativo"),
     * ese método alternativo también debería tener los mismos parámetros que el propio método getItem(...) sino
     * arrojará un error.
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems(@RequestParam(name = "nombre", required = false) String nombre,
                                                  @RequestHeader(name = "token-req", required = false) String token) {
        LOG.info("[Desde filtro de fábrica] name: {}, token: {}", nombre, token);
        return ResponseEntity.ok(this.itemService.findAll());
    }

    @GetMapping(path = "/producto/{productoId}/cantidad/{cantidad}")
    public ResponseEntity<Item> getItem(@PathVariable Long productoId, @PathVariable Integer cantidad) {
        /**
         * .create("items"), donde "items" es el identificador de este Circuit Breaker
         * La implementación del código de abajo es similar a cómo usábamos el fallBack de Hystrix para llamar
         * a un método alternativo cuando falle el método original, pero en este caso estamos usando
         * Resilience4j junto a expresiones lambda (Forma programática).
         */
        return circuitBreakerFactory.create("items")
                .run(() -> ResponseEntity.ok(this.itemService.findByProductId(productoId, cantidad)),
                        e -> this.metodoAlternativo(productoId, cantidad, e));
    }

    public ResponseEntity<Item> metodoAlternativo(Long productoId, Integer cantidad, Throwable e) {
        LOG.info("[Dentro del método alternativo] mensaje de error: {}", e.getMessage());
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
