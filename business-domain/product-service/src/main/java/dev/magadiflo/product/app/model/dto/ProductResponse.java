package dev.magadiflo.product.app.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(Long id,
                              String name,
                              BigDecimal price,
                              LocalDateTime createAt) {
}
