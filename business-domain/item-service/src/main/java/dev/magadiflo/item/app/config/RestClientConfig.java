package dev.magadiflo.item.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${custom.base-url.product-service}")
    private String productServiceBaseUrl;

    @LoadBalanced
    @Bean(name = "productRestClient")
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().baseUrl(this.productServiceBaseUrl);
    }
}
