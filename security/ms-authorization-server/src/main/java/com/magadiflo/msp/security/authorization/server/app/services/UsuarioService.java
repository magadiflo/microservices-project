package com.magadiflo.msp.security.authorization.server.app.services;

import brave.Tracer;
import com.magadiflo.msp.security.authorization.server.app.clients.IUsuarioFeignClient;
import com.magadiflo.msp.shared.library.usuarios.commons.app.models.entity.Usuario;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements IUsuarioService, UserDetailsService {
    private static final Logger LOG = LoggerFactory.getLogger(UsuarioService.class);
    private final IUsuarioFeignClient usuarioFeignClient;
    private final Tracer tracer;

    public UsuarioService(IUsuarioFeignClient usuarioFeignClient, Tracer tracer) {
        this.usuarioFeignClient = usuarioFeignClient;
        this.tracer = tracer;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
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
                    }).get();
        } catch (FeignException fe) {
            String error = String.format("[try-catch]Error en el login, no existe el usuario %s en el sistema", username);

            LOG.error(error);
            this.tracer.currentSpan().tag("error.mensaje", String.format("%s : %s", error, fe.getMessage()));
            throw new UsernameNotFoundException(error);
        }
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return this.usuarioFeignClient.findByUsername(username);
    }

    @Override
    public Usuario update(Usuario usuario, Long id) {
        return this.usuarioFeignClient.update(usuario, id);
    }
}
