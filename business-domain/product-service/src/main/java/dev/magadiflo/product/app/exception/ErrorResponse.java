package dev.magadiflo.product.app.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ErrorResponse(int status,
                            String error,
                            String message,
                            String path) {
    @JsonProperty
    public LocalDateTime timestamp() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
