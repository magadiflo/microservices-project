package dev.magadiflo.item.app.service;

import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.model.dto.ProductRequest;

import java.util.List;

public interface ItemService {
    List<Item> findItems();

    Item findItemByProductId(Long productId, int quantity);

    Product saveProduct(ProductRequest productRequest);

    Product updateProduct(Long productId, ProductRequest productRequest);

    void deleteProduct(Long productId);
}
