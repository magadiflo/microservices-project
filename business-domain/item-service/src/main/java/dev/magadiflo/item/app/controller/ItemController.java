package dev.magadiflo.item.app.controller;

import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.service.ItemService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemController {

    private final ItemService itemService;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public ItemController(@Qualifier("itemServiceWithRestClientImpl") ItemService itemService,
                          CircuitBreakerFactory circuitBreakerFactory) {
        this.itemService = itemService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping
    public ResponseEntity<List<Item>> findProducts() {
        return ResponseEntity.ok(this.itemService.findItems());
    }

    @GetMapping(path = "/filters-gateway")
    public ResponseEntity<Void> findProducts(@RequestParam(name = "color-name") String colorName,
                                             @RequestHeader(name = "X-Request-color") String headerColor) {
        log.info("AddRequestParameter (color-name): {}", colorName);
        log.info("AddRequestHeader (X-Request-color): {}", headerColor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{productId}")
    public ResponseEntity<Item> findProduct(@PathVariable Long productId, @RequestParam int quantity) {
        Product product = new Product(0L, "Producto respaldo", BigDecimal.ZERO, LocalDateTime.now(), 0);
        Item item = new Item(product, 1);

        return this.circuitBreakerFactory
                .create("items")
                .run(
                        () -> {
                            log.info("Llamando al product-service");
                            return ResponseEntity.ok(this.itemService.findItemByProductId(productId, quantity));
                        },
                        throwable -> {
                            log.warn("Error cuando se llamó al product-service, se envía información alternativa");
                            return ResponseEntity.ok(item);
                        }
                );
    }

    @CircuitBreaker(name = "items", fallbackMethod = "fallbackMethod")
    @GetMapping(path = "/cb/{productId}")
    public ResponseEntity<Item> showItem(@PathVariable Long productId, @RequestParam int quantity) {
        return ResponseEntity.ok(this.itemService.findItemByProductId(productId, quantity));
    }

    private ResponseEntity<Item> fallbackMethod(Long productId, int quantity, Throwable throwable) {
        log.info("Dentro del fallbackMethod(), error: {}", throwable.getMessage());
        log.info("productId: {}, quantity: {}", productId, quantity);
        Product product = new Product(0L, "Producto respaldo desde fallbackMethod()", BigDecimal.ZERO, LocalDateTime.now(), 0);
        Item item = new Item(product, 1);
        return ResponseEntity.ok(item);
    }
}
