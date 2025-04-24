package dev.magadiflo.gateway.server.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/authorized", "/logout").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/items", "/api/v1/users").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products/{productId}", "/api/v1/items/{productId}", "/api/v1/users/{userId}", "/api/v1/users/username/{username}").hasAnyRole("ADMIN", "USER")
                        .pathMatchers("/api/v1/products/**", "/api/v1/items/**", "/api/v1/users/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt -> {
                            jwt.jwtAuthenticationConverter(source -> {
                                Collection<String> roles = source.getClaimAsStringList("roles");

                                Collection<GrantedAuthority> authorities = roles.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toSet());

                                return Mono.just(new JwtAuthenticationToken(source, authorities));
                            });
                        }));
        return http.build();
    }

}
