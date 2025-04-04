package dev.magadiflo.product.app.model.dto;

import java.math.BigDecimal;

public record ProductRequest(String name,
                             BigDecimal price) {
}
