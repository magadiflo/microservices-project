package dev.magadiflo.gateway.server.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/authorized", "/logout").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/items", "/api/v1/users").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products/{productId}", "/api/v1/items/{productId}", "/api/v1/users/{userId}", "/api/v1/users/username/{username}").hasAnyAuthority("SCOPE_write", "SCOPE_read")
                        .pathMatchers("/api/v1/products/**", "/api/v1/items/**", "/api/v1/users/**").hasAuthority("SCOPE_write")
                        .anyExchange().authenticated()
                )
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

}
