package com.magadiflo.msp.infrastructure.gateway.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SpringSecurityConfig {
    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers("/api-base/authorization-server-base/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api-base/productos-base/api/v1/productos",
                        "/api-base/items-base/api/v1/items",
                        "/api-base/usuarios-base/usuarios",
                        "/api-base/items-base/api/v1/items/producto/{productoId}/cantidad/{cantidad}",
                        "/api-base/productos-base/api/v1/productos/{id}").permitAll()
                .pathMatchers(HttpMethod.GET, "/api-base/usuarios-base/usuarios/{id}").hasAnyRole("ADMIN", "USER")
                .pathMatchers("/api-base/productos-base/**",
                        "/api-base/items-base/**",
                        "/api-base/usuarios-base/**").hasRole("ADMIN")
                .anyExchange().authenticated()
                .and()
                .csrf().disable()
                .build();
    }
}
