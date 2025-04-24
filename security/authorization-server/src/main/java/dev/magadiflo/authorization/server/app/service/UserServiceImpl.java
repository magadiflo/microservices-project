package dev.magadiflo.authorization.server.app.service;

import dev.magadiflo.authorization.server.app.constant.AuthConstant;
import dev.magadiflo.authorization.server.app.exception.CommunicationException;
import dev.magadiflo.authorization.server.app.model.dto.User;
import dev.magadiflo.authorization.server.app.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    private final RestClient userRestClient;

    public UserServiceImpl(@Qualifier("userRestClient") RestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Buscando usuario con username: {}", username);
        User user = this.userRestClient.get()
                .uri("/username/{username}", username)
                .exchange((request, response) -> {
                    HttpStatusCode statusCode = response.getStatusCode();
                    if (statusCode == HttpStatus.OK) {
                        return response.bodyTo(User.class);
                    }

                    if (statusCode == HttpStatus.NOT_FOUND) {
                        log.warn("Usuario no encontrado con username: {}", username);
                        throw new UsernameNotFoundException(AuthConstant.NO_FOUND_MESSAGE.formatted(username));
                    }

                    String bodyMessage = Util.readInputStream(response.getBody());
                    throw new CommunicationException(AuthConstant.COMMUNICATION_MESSAGE.formatted(bodyMessage));
                });
        log.info("Usuario encontrado: {}", user);
        Set<GrantedAuthority> authorities = user.roles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
        log.info("Retornando un UserDetails con roles: {}", authorities);
        return new org.springframework.security.core.userdetails.User(user.username(), user.password(), user.enabled(), true, true, true, authorities);
    }
}
