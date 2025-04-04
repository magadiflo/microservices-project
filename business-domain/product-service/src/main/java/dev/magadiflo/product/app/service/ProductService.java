package dev.magadiflo.product.app.service;

import dev.magadiflo.product.app.model.dto.ProductRequest;
import dev.magadiflo.product.app.model.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> findProducts();

    ProductResponse findProduct(Long productId);

    ProductResponse saveProduct(ProductRequest request);

    ProductResponse updateProduct(Long productId, ProductRequest request);

    void deleteProduct(Long productId);
}
