package dev.magadiflo.item.app.service.impl;

import dev.magadiflo.item.app.constant.ItemConstant;
import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class ItemServiceWithRestClientImpl implements ItemService {

    private final RestClient productRestClient;

    public ItemServiceWithRestClientImpl(@Qualifier("productRestClient") RestClient.Builder restClientBuilder) {
        this.productRestClient = restClientBuilder.build();
    }

    @Override
    public List<Item> findItems() {
        List<Product> productsResponse = this.productRestClient.get()
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        List<Product> products = Optional.ofNullable(productsResponse)
                .orElseThrow(() -> new NoSuchElementException(ItemConstant.NO_SUCH_LIST_ELEMENTS_MESSAGE));

        return products.stream()
                .map(product -> new Item(product, 1))
                .toList();
    }

    @Override
    public Item findItemByProductId(Long productId, int quantity) {
        Product productResponse = this.productRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{productId}")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .retrieve()
                .body(Product.class);

        Product product = Optional.ofNullable(productResponse)
                .orElseThrow(() -> new NoSuchElementException(ItemConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId)));

        return new Item(product, quantity);
    }
}
