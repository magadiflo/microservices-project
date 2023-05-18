package com.magadiflo.msp.security.authorization.server.app.security.event;

import com.magadiflo.msp.security.authorization.server.app.services.IUsuarioService;
import com.magadiflo.msp.shared.library.usuarios.commons.app.models.entity.Usuario;
import feign.FeignException;
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
        Usuario usuario = this.usuarioService.findByUsername(authentication.getName()).orElseThrow();
        if (usuario.getIntentos() != null && usuario.getIntentos() > 0) {
            usuario.setIntentos(0);
            this.usuarioService.update(usuario, usuario.getId());
        }
    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        LOG.error("Error en el login: {}", exception.getMessage());
        try {
            Usuario usuario = this.usuarioService.findByUsername(authentication.getName()).orElseThrow();

            if (usuario.getIntentos() == null) {
                usuario.setIntentos(0);
            }

            usuario.setIntentos(usuario.getIntentos() + 1);
            LOG.info("N° de intentos fallidos: {}", usuario.getIntentos());

            if (usuario.getIntentos() >= 3) {
                usuario.setEnabled(false);
                LOG.info("El usuario {} deshabilitado 3 intentos fallidos", usuario.getUsername());
            }
            this.usuarioService.update(usuario, usuario.getId());

        } catch (FeignException e) {
            LOG.error("El usuario {} no existe en el sistema", authentication.getName());
        }
    }
}
