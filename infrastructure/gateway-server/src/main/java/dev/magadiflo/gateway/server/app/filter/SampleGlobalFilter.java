package dev.magadiflo.gateway.server.app.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class SampleGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Global: ejecutando filtro request PRE");
        ServerHttpRequest newRequest = exchange.getRequest()
                .mutate()
                .header("token-request", "123456")
                .build();

        ServerWebExchange newExchange = exchange.mutate()
                .request(newRequest)
                .build();

        return chain.filter(newExchange)
                .then(Mono.fromRunnable(() -> {
                    log.info("Global: ejecutando filtro response POST");
                    String tokenRequest = newExchange.getRequest().getHeaders().getFirst("token-request");
                    Optional.ofNullable(tokenRequest).ifPresent(value ->
                            newExchange.getResponse().getHeaders().add("token-response", value));

                    newExchange.getResponse()
                            .getCookies()
                            .add("color", ResponseCookie.from("color", "red").build());
                }));
    }
}
