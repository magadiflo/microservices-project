package dev.magadiflo.item.app.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class ResilienceConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> customizerCircuitBreaker() {
        return factory -> factory.configureDefault(circuitBreakerId -> {
            log.info("Configurando circuit breaker: {}", circuitBreakerId);

            return new Resilience4JConfigBuilder(circuitBreakerId)
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .failureRateThreshold(50)
                            .waitDurationInOpenState(Duration.ofSeconds(10L))
                            .permittedNumberOfCallsInHalfOpenState(5)
                            .slowCallRateThreshold(50)
                            .slowCallDurationThreshold(Duration.ofSeconds(2L))
                            .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(6L))
                            .build())
                    .build();
        });
    }
}
