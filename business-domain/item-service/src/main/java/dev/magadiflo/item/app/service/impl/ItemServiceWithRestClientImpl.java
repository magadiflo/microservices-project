package dev.magadiflo.item.app.service.impl;

import dev.magadiflo.item.app.constant.ItemConstant;
import dev.magadiflo.item.app.exception.CommunicationException;
import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.model.dto.Product;
import dev.magadiflo.item.app.model.dto.ProductRequest;
import dev.magadiflo.item.app.service.ItemService;
import dev.magadiflo.item.app.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class ItemServiceWithRestClientImpl implements ItemService {

    private final RestClient productRestClient;

    public ItemServiceWithRestClientImpl(@Qualifier("productRestClient") RestClient productRestClient) {
        this.productRestClient = productRestClient;
    }

    @Override
    public List<Item> findItems() {
        log.info("Obteniendo productos desde el product-service");
        List<Product> products = this.productRestClient.get()
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (products == null) {
            throw new IllegalStateException(ItemConstant.ILEGAL_STATE_PRODUCT_MESSAGE);
        }
        log.info("Productos recuperados desde el product-service: {}", products.size());
        log.info("Generando items a partir de los productos recuperados");
        return products.stream()
                .map(product -> new Item(product, 1))
                .toList();
    }

    @Override
    public Item findItemByProductId(Long productId, int quantity) {
        log.info("Buscando producto con id {}", productId);
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
        log.info("Retornando item con producto con id {} y cantidad {}", product, quantity);
        return new Item(product, quantity);
    }

    @Override
    public Product saveProduct(ProductRequest productRequest) {
        log.info("Enviando producto al product-service para guardarlo: {}", productRequest);
        return this.productRestClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productRequest)
                .retrieve()
                .body(Product.class);
    }

    @Override
    public Product updateProduct(Long productId, ProductRequest productRequest) {
        log.info("Enviando producto al product-service para actualizar producto con id {}, con los datos {}", productId, productRequest);
        return this.productRestClient.put()
                .uri("/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(productRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String bodyMessage = Util.readInputStream(response.getBody());
                    throw new NoSuchElementException(ItemConstant.NO_FOUND_ELEMENT_MESSAGE.formatted(productId).concat(". ").concat(bodyMessage));
                })
                .body(Product.class);
    }

    @Override
    public void deleteProduct(Long productId) {
        log.info("Enviando producto al product-service para su eliminación. ProductId: {}", productId);
        this.productRestClient.delete()
                .uri("/{productId}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String bodyMessage = Util.readInputStream(response.getBody());
                    throw new NoSuchElementException(ItemConstant.NO_FOUND_ELEMENT_MESSAGE.formatted(productId).concat(". ").concat(bodyMessage));
                })
                .toBodilessEntity();
    }
}
