package dev.magadiflo.item.app.model.dto;

import java.math.BigDecimal;

public record Item(Product product, int quantity) {
    public BigDecimal total() {
        return product.price().multiply(BigDecimal.valueOf(quantity));
    }
}
