package com.magadiflo.msp.business.domain.items.app.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * @LoadBalanced, con esa anotación, de forma automática RestTemplate usará Ribbon para el balanceo de carga, es decir
     * por debajo, RestTemplate buscará la mejor instancia usando el balanceador de carga.
     */
    @Bean(name = "clienteRest")
    @LoadBalanced
    public RestTemplate registrarRestTemplate() {
        return new RestTemplate();
    }

}
