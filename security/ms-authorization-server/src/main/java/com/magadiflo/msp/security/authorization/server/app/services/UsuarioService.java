package com.magadiflo.msp.security.authorization.server.app.services;

import com.magadiflo.msp.security.authorization.server.app.clients.IUsuarioFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {
    private static final Logger LOG = LoggerFactory.getLogger(UsuarioService.class);
    private final IUsuarioFeignClient usuarioFeignClient;

    public UsuarioService(IUsuarioFeignClient usuarioFeignClient) {
        this.usuarioFeignClient = usuarioFeignClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.usuarioFeignClient.findByUsername(username)
                .map(usuario -> {

                    List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                            .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                            .peek(simpleGrantedAuthority -> LOG.info("Rol: {}", simpleGrantedAuthority.getAuthority()))
                            .toList();

                    UserDetails userDetails = User.builder()
                            .username(usuario.getUsername())
                            .password(usuario.getPassword())
                            .authorities(authorities)
                            .disabled(!usuario.getEnabled())
                            .build();

                    LOG.info("Detalles del usuario autenticado: {}", userDetails);

                    return userDetails;
                })
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Error en el login, no existe el usuario %s en el sistema", username)));
    }
}