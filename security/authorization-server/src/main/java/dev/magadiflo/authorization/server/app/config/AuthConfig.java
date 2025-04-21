package dev.magadiflo.authorization.server.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

@Configuration
public class AuthConfig {

    @Value("${custom.base-url.user-service}")
    private String userServiceBaseUrl;

    @LoadBalanced
    @Bean(name = "userRestClient")
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().baseUrl(this.userServiceBaseUrl);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
