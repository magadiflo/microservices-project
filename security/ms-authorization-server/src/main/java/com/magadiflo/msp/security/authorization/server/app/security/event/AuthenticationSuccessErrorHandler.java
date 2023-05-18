package com.magadiflo.msp.security.authorization.server.app.security.event;

import com.magadiflo.msp.security.authorization.server.app.services.IUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {
    private final IUsuarioService usuarioService;

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);

    public AuthenticationSuccessErrorHandler(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        if (authentication.getDetails() instanceof WebAuthenticationDetails) return;

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LOG.info("Login exitoso!: {}", userDetails.getUsername());
    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        LOG.error("Error en el login: {}", exception.getMessage());
    }
}
