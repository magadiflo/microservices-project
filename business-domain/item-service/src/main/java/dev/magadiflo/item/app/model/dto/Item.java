package dev.magadiflo.item.app.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record Item(Product product, int quantity) {
    @JsonProperty
    public BigDecimal total() {
        return product.price().multiply(BigDecimal.valueOf(quantity));
    }
}
