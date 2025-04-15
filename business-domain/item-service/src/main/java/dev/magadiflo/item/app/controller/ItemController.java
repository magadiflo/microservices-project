package dev.magadiflo.item.app.controller;

import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.model.dto.ProductRequest;
import dev.magadiflo.item.app.service.ItemService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RefreshScope
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemController {

    private final ItemService itemService;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final Environment environment;

    @Value("${custom.text}")
    private String text;

    public ItemController(@Qualifier("itemServiceWithRestClientImpl") ItemService itemService,
                          CircuitBreakerFactory circuitBreakerFactory, Environment environment) {
        this.itemService = itemService;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.environment = environment;
    }

    @GetMapping
    public ResponseEntity<List<Item>> findProducts() {
        return ResponseEntity.ok(this.itemService.findItems());
    }

    @GetMapping(path = "/retrieve-configs")
    public ResponseEntity<Map<String, Object>> retrieveConfig(@Value("${server.port}") int port) {
        log.info("server.port: {}", port);
        log.info("custom.text: {}", this.text);
        log.info("custom.author.name: {}", this.environment.getProperty("custom.author.name"));
        log.info("custom.author.email: {}", this.environment.getProperty("custom.author.email"));

        return ResponseEntity.ok(Map.of(
                "custom.text", this.text,
                "server.port", port,
                "custom.author.name", this.environment.getProperty("custom.author.name"),
                "custom.author.email", this.environment.getProperty("custom.author.email"))
        );
    }

    @GetMapping(path = "/fallback")
    public ResponseEntity<Item> fallbackItem() {
        Product product = new Product(0L, "Fallback desde Gateway", BigDecimal.ZERO, LocalDateTime.now(), 0);
        return ResponseEntity.ok(new Item(product, 1));
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

    @TimeLimiter(name = "items", fallbackMethod = "fallbackMethod2")
    @GetMapping(path = "/cb-2/{productId}")
    public CompletableFuture<ResponseEntity<Item>> showItem2(@PathVariable Long productId, @RequestParam int quantity) {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(this.itemService.findItemByProductId(productId, quantity)));
    }

    private ResponseEntity<Item> fallbackMethod(Long productId, int quantity, Throwable throwable) {
        log.info("Dentro del fallbackMethod(), error: {}", throwable.getMessage());
        log.info("productId: {}, quantity: {}", productId, quantity);
        Product product = new Product(0L, "Producto respaldo desde fallbackMethod()", BigDecimal.ZERO, LocalDateTime.now(), 0);
        Item item = new Item(product, 1);
        return ResponseEntity.ok(item);
    }

    private CompletableFuture<ResponseEntity<Item>> fallbackMethod2(Long productId, int quantity, Throwable throwable) {
        log.info("Dentro del fallbackMethod2(), error: {}", throwable.getMessage());
        log.info("productId: {} - quantity: {}", productId, quantity);
        Product product = new Product(0L, "Producto respaldo desde fallbackMethod2()", BigDecimal.ZERO, LocalDateTime.now(), 0);
        Item item = new Item(product, 1);
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(item));
    }

    @PostMapping(path = "/for-product")
    public ResponseEntity<Product> saveProduct(@RequestBody ProductRequest productRequest) {
        Product productResponse = this.itemService.saveProduct(productRequest);
        URI location = URI.create("/api/v1/products/%d".formatted(productResponse.id()));
        return ResponseEntity.created(location).body(productResponse);
    }

    @PutMapping(path = "/for-product/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok(this.itemService.updateProduct(productId, productRequest));
    }

    @DeleteMapping(path = "/for-product/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        this.itemService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
