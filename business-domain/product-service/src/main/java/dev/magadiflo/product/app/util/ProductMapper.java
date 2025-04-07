package dev.magadiflo.product.app.util;

import dev.magadiflo.product.app.entity.Product;
import dev.magadiflo.product.app.model.dto.ProductRequest;
import dev.magadiflo.product.app.model.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product, int port) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getCreateAt(), port);
    }

    public Product toProduct(ProductRequest request) {
        return Product.builder()
                .name(request.name())
                .price(request.price())
                .createAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public Product toUpdateProduct(Product productDB, ProductRequest request) {
        productDB.setName(request.name());
        productDB.setPrice(request.price());
        return productDB;
    }
}
