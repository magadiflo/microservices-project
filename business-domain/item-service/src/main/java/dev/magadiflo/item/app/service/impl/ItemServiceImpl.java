package dev.magadiflo.item.app.service.impl;

import dev.magadiflo.item.app.client.ProductFeignClient;
import dev.magadiflo.item.app.constant.ItemConstant;
import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.model.dto.ProductRequest;
import dev.magadiflo.item.app.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ProductFeignClient productFeignClient;

    @Override
    public List<Item> findItems() {
        return this.productFeignClient.findProducts().stream()
                .map(product -> new Item(product, 1))
                .toList();
    }

    @Override
    public Item findItemByProductId(Long productId, int quantity) {
        return this.productFeignClient.findProduct(productId)
                .map(product -> new Item(product, quantity))
                .orElseThrow(() -> new NoSuchElementException(ItemConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId)));
    }

    @Override
    public Product saveProduct(ProductRequest request) {
        return this.productFeignClient.saveProduct(request);
    }

    @Override
    public Product updateProduct(Long productId, ProductRequest request) {
        return this.productFeignClient.updateProduct(productId, request);
    }

    @Override
    public void deleteProduct(Long productId) {
        this.productFeignClient.deleteProduct(productId);
    }
}
