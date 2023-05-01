package com.magadiflo.msp.infrastructure.gateway.app.gateway.filters.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Para que la clase se detecte automáticamente como un filtro debe tener al final
 * del nombre de la clase: GatewayFilterFactory.
 * Ejemplo:
 * Nombre_cualquiera + GatewayFilterFactory => EjemploGatewayFilterFactory
 * Aunque también se puede realizar la configuración con cualquier nombre en el application.properties
 */
@Component
public class EjemploGatewayFilterFactory extends AbstractGatewayFilterFactory<EjemploGatewayFilterFactory.Configuracion> {

    private static final Logger LOG = LoggerFactory.getLogger(EjemploGatewayFilterFactory.class.getName());

    public EjemploGatewayFilterFactory() {
        super(Configuracion.class);
    }

    @Override
    public GatewayFilter apply(Configuracion configuracion) {
        return (exchange, chain) -> {
            LOG.info("Ejecutando PRE Gateway Filter Factory: {}", configuracion.mensaje);

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        LOG.info("Ejecutando POST Gateway Filter Factory: {}", configuracion.mensaje);

                        Optional.ofNullable(configuracion.cookieValor).ifPresent(cookie -> {
                            exchange.getResponse().addCookie(ResponseCookie.from(configuracion.cookieNombre, cookie).build());
                        });
                    }));
        };
    }

    public static class Configuracion {
        private String mensaje;
        private String cookieValor;
        private String cookieNombre;

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getCookieValor() {
            return cookieValor;
        }

        public void setCookieValor(String cookieValor) {
            this.cookieValor = cookieValor;
        }

        public String getCookieNombre() {
            return cookieNombre;
        }

        public void setCookieNombre(String cookieNombre) {
            this.cookieNombre = cookieNombre;
        }
    }
}

