package dev.magadiflo.item.app.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(Long id,
                      String name,
                      BigDecimal price,
                      LocalDateTime createAt,
                      int port) {
}
