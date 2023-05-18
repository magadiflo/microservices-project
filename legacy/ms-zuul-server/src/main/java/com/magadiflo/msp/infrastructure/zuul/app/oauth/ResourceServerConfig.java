package com.magadiflo.msp.infrastructure.zuul.app.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfigurationSource;

@RefreshScope
@EnableResourceServer
@Configuration
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Value("${config.security.oauth.jwt.key}")
    private String jwtKey;
    private final CorsConfigurationSource corsConfigurationSource;

    public ResourceServerConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(this.jwtTokenStore());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/api-base/authorization-server-base/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api-base/productos-base/api/v1/productos",
                        "/api-base/items-base/api/v1/items",
                        "/api-base/usuarios-base/usuarios")
                .permitAll()
                .antMatchers(HttpMethod.GET,
                        "/api-base/productos-base/api/v1/productos/{id}",
                        "/api-base/items-base/api/v1/items/producto/{productoId}/cantidad/{cantidad}",
                        "/api-base/usuarios-base/usuarios/{id}")
                .hasAnyRole("ADMIN", "USER")
                .antMatchers("/api-base/productos-base/**",
                        "/api-base/items-base/**", "/api-base/usuarios-base/**")
                .hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .cors().configurationSource(this.corsConfigurationSource);
    }

    @Bean
    public JwtTokenStore jwtTokenStore() {
        return new JwtTokenStore(this.jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(this.jwtKey);
        return jwtAccessTokenConverter;
    }
}
