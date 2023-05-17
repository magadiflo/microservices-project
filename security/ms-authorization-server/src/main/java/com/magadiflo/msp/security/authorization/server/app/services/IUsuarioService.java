package com.magadiflo.msp.security.authorization.server.app.services;

import com.magadiflo.msp.shared.library.usuarios.commons.app.models.entity.Usuario;

import java.util.Optional;

public interface IUsuarioService {
    Optional<Usuario> findByUsername(String username);
}
