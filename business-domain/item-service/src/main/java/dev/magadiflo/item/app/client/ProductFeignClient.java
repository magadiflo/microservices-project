package dev.magadiflo.item.app.client;

import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.model.dto.ProductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "product-service", path = "/api/v1/products")
public interface ProductFeignClient {
    @GetMapping
    List<Product> findProducts();

    @GetMapping(path = "/{productId}")
    Optional<Product> findProduct(@PathVariable Long productId);

    @PostMapping
    Product saveProduct(@RequestBody ProductRequest productRequest);

    @PutMapping(path = "/{productId}")
    Product updateProduct(@PathVariable Long productId, @RequestBody ProductRequest productRequest);

    @DeleteMapping(path = "/{productId}")
    void deleteProduct(@PathVariable Long productId);
}
