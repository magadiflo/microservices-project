package dev.magadiflo.gateway.server.app.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SampleGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Global: ejecutando filtro request PRE");

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    log.info("Global: ejecutando filtro response POST");

                }));
    }
}
