package com.magadiflo.msp.infrastructure.gateway.app.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/***
 * Cada vez que se realice un request, este filtro se
 * ejecutará y en el método filter(...) modificamos
 * el RESPONSE que saldrá.
 */

@Component
public class EjemploGlobalFilter implements GlobalFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EjemploGlobalFilter.class.getName());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOG.info("Ejecutando filtro PRE");
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    LOG.info("Ejecutando filtro POST");
                    exchange.getResponse()
                            .getCookies()
                            .add("color", ResponseCookie.from("color", "rojo").build());
                    exchange.getResponse()
                            .getHeaders()
                            .setContentType(MediaType.TEXT_PLAIN);
                }));
    }
}
