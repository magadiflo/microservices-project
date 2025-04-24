package dev.magadiflo.item.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class RestClientConfig {

    @Value("${custom.base-url.product-service}")
    private String productServiceBaseUrl;

    @LoadBalanced
    @Bean
    public RestClient.Builder restClientBuilder(List<RestClientCustomizer> customizers) {
        RestClient.Builder builder = RestClient.builder();
        customizers.forEach(customizer -> customizer.customize(builder));
        return builder;
    }

    @Bean(name = "productRestClient")
    public RestClient productRestClient(@Qualifier("restClientBuilder") RestClient.Builder builder) {
        return builder.baseUrl(this.productServiceBaseUrl).build();
    }
}
