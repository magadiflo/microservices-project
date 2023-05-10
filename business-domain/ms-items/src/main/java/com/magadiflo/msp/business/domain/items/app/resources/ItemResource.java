package com.magadiflo.msp.business.domain.items.app.resources;

import com.magadiflo.msp.business.domain.items.app.models.Item;
import com.magadiflo.msp.business.domain.items.app.models.Producto;
import com.magadiflo.msp.business.domain.items.app.service.IItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemResource {
    private final IItemService itemService;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final Environment environment;
    private static final Logger LOG = LoggerFactory.getLogger(ItemResource.class);

    @Value("${configuracion.texto}")
    private String texto;

    public ItemResource(@Qualifier(value = "itemServiceFeign") IItemService itemService, CircuitBreakerFactory circuitBreakerFactory, Environment environment) {
        this.itemService = itemService;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.environment = environment;
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

    // Usando el CircuitBreakerFactory
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

    // Usando anotaciones

    /**
     * name="items", el nombre del identificador del circuit breaker.
     * Será el mismo que le dimos en el método getItem(...)
     * <p>
     * IMPORTANTE: Si usamos anotaciones, la configuración solo será aplicado
     * vía archivo (application.yml) y no de forma programática.
     */
    @CircuitBreaker(name = "items", fallbackMethod = "metodoAlternativo")
    @GetMapping(path = "/producto-2/{productoId}/cantidad/{cantidad}")
    public ResponseEntity<Item> getItem2(@PathVariable Long productoId, @PathVariable Integer cantidad) {
        return ResponseEntity.ok(this.itemService.findByProductId(productoId, cantidad));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "metodoAlternativo2")
    @TimeLimiter(name = "items")
    @GetMapping(path = "/producto-3/{productoId}/cantidad/{cantidad}")
    public CompletableFuture<ResponseEntity<Item>> getItem3(@PathVariable Long productoId, @PathVariable Integer cantidad) {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(this.itemService.findByProductId(productoId, cantidad)));
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

    public CompletableFuture<ResponseEntity<Item>> metodoAlternativo2(Long productoId, Integer cantidad, Throwable e) {
        LOG.info("[Dentro del método alternativo] mensaje de error: {}", e.getMessage());
        Producto producto = new Producto();
        producto.setId(productoId);
        producto.setNombre("Cámara Sony");
        producto.setPrecio(500D);

        Item item = new Item();
        item.setCantidad(cantidad);
        item.setProducto(producto);

        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(item));
    }

    /**
     * Dos formas de inyectar con @Value
     * 1° forma, anotarlo en un atributo de la clase:
     *      @Value("${configuracion.texto}")
     *      private String texto;
     * 2° forma, inyectarlo en el parámetro del método
     *      ..obtenerConfiguracion(@Value("${server.port}") String puerto) {...}
     */
    @GetMapping(path = "/config")
    public ResponseEntity<?> obtenerConfiguracion(@Value("${server.port}") String puerto) {
        LOG.info("texto: {}, puerto: {}", this.texto, puerto);

        Map<String, Object> map = new HashMap<>();
        map.put("texto", this.texto);
        map.put("puerto", puerto);

        if(environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("development")) {
            map.put("nombre", environment.getProperty("configuracion.autor.nombre"));
            map.put("email",  environment.getProperty("configuracion.autor.email"));
        }

        return ResponseEntity.ok(map);
    }
}
