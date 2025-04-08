package dev.magadiflo.item.app.service.impl;

import dev.magadiflo.item.app.constant.ItemConstant;
import dev.magadiflo.item.app.exception.CommunicationException;
import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.service.ItemService;
import dev.magadiflo.item.app.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class ItemServiceWithRestClientImpl implements ItemService {

    private final RestClient productRestClient;

    public ItemServiceWithRestClientImpl(@Qualifier("productRestClient") RestClient.Builder restClientBuilder) {
        this.productRestClient = restClientBuilder.build();
    }

    @Override
    public List<Item> findItems() {
        List<Product> products = this.productRestClient.get()
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (products == null) {
            throw new IllegalStateException(ItemConstant.ILEGAL_STATE_PRODUCT_MESSAGE);
        }

        return products.stream()
                .map(product -> new Item(product, 1))
                .toList();
    }

    @Override
    public Item findItemByProductId(Long productId, int quantity) {
        Product product = this.productRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{productId}")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .exchange((request, response) -> {
                    HttpStatusCode statusCode = response.getStatusCode();

                    if (statusCode == HttpStatus.OK) {
                        return response.bodyTo(Product.class);
                    }

                    if (statusCode == HttpStatus.NOT_FOUND) {
                        throw new NoSuchElementException(ItemConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId));
                    }

                    String bodyMessage = Util.readInputStream(response.getBody());
                    throw new CommunicationException(ItemConstant.COMMUNICATION_MESSAGE.formatted(bodyMessage));
                });
        return new Item(product, quantity);
    }
}
