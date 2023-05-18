package com.magadiflo.msp.security.authorization.server.app.clients;

import com.magadiflo.msp.shared.library.usuarios.commons.app.models.entity.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@FeignClient(name = "ms-usuarios", path = "/usuarios")
public interface IUsuarioFeignClient {
    @GetMapping(path = "/search/buscar-usuario")
    Optional<Usuario> findByUsername(@RequestParam(value = "usuario") String username);

    @PutMapping(path = "/{id}")
    Usuario update(@RequestBody Usuario usuario, @PathVariable Long id);

}
