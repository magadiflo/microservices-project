package dev.magadiflo.product.app.util;

import dev.magadiflo.product.app.entity.Product;
import dev.magadiflo.product.app.model.dto.ProductRequest;
import dev.magadiflo.product.app.model.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getCreateAt());
    }

    public Product toProduct(ProductRequest request) {
        return Product.builder()
                .name(request.name())
                .price(request.price())
                .createAt(LocalDateTime.now())
                .build();
    }

    public Product toUpdateProduct(Product productDB, ProductRequest request) {
        productDB.setName(request.name());
        productDB.setPrice(request.price());
        return productDB;
    }
}
