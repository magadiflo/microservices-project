package dev.magadiflo.gateway.server.app.filter.factory;

import dev.magadiflo.gateway.server.app.dto.ConfigurationCookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SampleCookieGatewayFilterFactory extends AbstractGatewayFilterFactory<ConfigurationCookie> {
    @Override
    public GatewayFilter apply(ConfigurationCookie config) {
        return (exchange, chain) -> {
            log.info("Ejecuta PRE GatewayFilterFactory: {}", config.message());

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        log.info("Ejecuta POST GatewayFilterFactory: {}", config.message());
                    }));
        };
    }
}
