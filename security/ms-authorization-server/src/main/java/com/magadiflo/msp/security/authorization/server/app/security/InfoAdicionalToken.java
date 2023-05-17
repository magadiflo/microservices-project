package com.magadiflo.msp.security.authorization.server.app.security;

import com.magadiflo.msp.security.authorization.server.app.services.IUsuarioService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InfoAdicionalToken implements TokenEnhancer {
    private final IUsuarioService usuarioService;

    public InfoAdicionalToken(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        return this.usuarioService.findByUsername(authentication.getName())
                .map(usuario -> {

                    Map<String, Object> info = new HashMap<>();
                    info.put("nombre", usuario.getNombre());
                    info.put("apellido", usuario.getApellido());
                    info.put("correo", usuario.getEmail());

                    ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);

                    return accessToken;
                }).orElseGet(() -> accessToken);
    }
}
