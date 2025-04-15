package dev.magadiflo.item.app.model.dto;

import java.math.BigDecimal;

public record ProductRequest(String name,
                             BigDecimal price) {
}
