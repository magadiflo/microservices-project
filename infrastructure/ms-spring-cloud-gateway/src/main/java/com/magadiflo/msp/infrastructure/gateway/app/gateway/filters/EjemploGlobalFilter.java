package com.magadiflo.msp.infrastructure.gateway.app.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/***
 * Cada vez que se realice un request, este filtro se
 * ejecutará y en el método filter(...) modificamos
 * el RESPONSE que saldrá.
 */

@Component
public class EjemploGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(EjemploGlobalFilter.class.getName());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOG.info("Ejecutando filtro PRE");

        // Al request que viene desde el cliente (Ejm. postman) le agregamos el atributo token-request a su header
        exchange.getRequest().mutate().headers(httpHeaders -> {
            httpHeaders.add("token-request", "123456");
        });

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    LOG.info("Ejecutando filtro POST");

                    // Al response que se devolverá al cliente, le modificamos sus headers para enviarle
                    // el mismo token que nos mandó en el request (token-request). Pero antes, preguntamos si está presente
                    // ese token-request en la cabecera del request.
                    Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("token-request"))
                            .ifPresent(valor -> {
                                exchange.getResponse().getHeaders().add("token-response", valor);
                            });


                    exchange.getResponse()
                            .getCookies()
                            .add("color", ResponseCookie.from("color", "rojo").build());
                    exchange.getResponse()
                            .getHeaders()
                            .setContentType(MediaType.TEXT_PLAIN);
                }));
    }

    /**
     * El valor que retorna, es el valor de la prioridad que le estamos dando, en nuestro caso 100.
     * Los valores más altos se interpretan como una prioridad más baja. Como consecuencia, el objeto con el
     * valor más bajo tiene la prioridad más alta (algo similar a los valores de carga en el inicio de Servlet).
     * <p>
     * En palabras sencillas, le estamos diciendo que el número de orden en el que se ejecutará este filtro
     * será el N° 100. Con esto, permitimos que se ejecuten primero otros filtros que tengan un
     * valor menor y por lo tanto, mayor prioridad para ejecutarse primero.
     */
    @Override
    public int getOrder() {
        return 100;
    }
}
