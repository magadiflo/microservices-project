package com.magadiflo.msp.business.domain.items.app.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CustomResilience {
    private final static Logger LOG = LoggerFactory.getLogger(CustomResilience.class);

    /**
     * El id, es el id que le damos a cada Circuit Break cuando lo creamos.
     * En la clase ItemResource método getItem(...) creamos un circuit breaker
     * al que le pusimos como id = "items".
     * Ese id, es el que se pasa al ...factory.configureDefault(id -> {...}..
     * para poder configurarle los parámetros como el tamaño de la ventana
     * deslizante (sobre qué cantidad de request trabajará), el
     * umbral de tasa de fallas, el tiempo de espera en estado abierto, etc.
     * de esa forma sobreescribimos los criterios que por defecto traer el
     * circuit breaker.
     * Todos los circuit breaker que tengamos en la aplicación pasarán por
     * ese factory como id, pero como solo tenemos uno creado "items",
     * obviamente solo ese se está pasando.
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> {
            LOG.info("id del circuit breaker = {}", id);
            return new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .failureRateThreshold(50)
                            .waitDurationInOpenState(Duration.ofSeconds(10L))
                            .permittedNumberOfCallsInHalfOpenState(5)
                            .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(2L)).build())
                    .build();
        });
    }
}
