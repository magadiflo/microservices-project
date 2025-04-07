package dev.magadiflo.item.app.client;

import dev.magadiflo.item.app.model.dto.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service", url = "127.0.0.1:8001", path = "/api/v1/products")
public interface ProductFeignClient {
    @GetMapping
    List<Product> findProducts();

    @GetMapping(path = "/{productId}")
    Product findProduct(@PathVariable Long productId);
}
