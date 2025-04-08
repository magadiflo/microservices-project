package dev.magadiflo.item.app.service.impl;

import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class ItemServiceWithRestClientImpl implements ItemService {

    private final RestClient productRestClient;

    public ItemServiceWithRestClientImpl(RestClient.Builder restClientBuilder) {
        this.productRestClient = restClientBuilder.baseUrl("lb://product-service/api/v1/products").build();
    }

    @Override
    public List<Item> findItems() {
        return this.productRestClient.get()
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public Item findItemByProductId(Long productId, int quantity) {
        return this.productRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{productId}")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .retrieve()
                .body(Item.class);
    }
}
