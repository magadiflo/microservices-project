package dev.magadiflo.authorization.server.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    private final RestClient userRestClient;

    public UserServiceImpl(@Qualifier("userRestClient") RestClient.Builder restClientBuilder) {
        this.userRestClient = restClientBuilder.build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
