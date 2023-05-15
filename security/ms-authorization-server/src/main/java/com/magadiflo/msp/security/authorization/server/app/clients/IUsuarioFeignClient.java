package com.magadiflo.msp.security.authorization.server.app.clients;

import com.magadiflo.msp.shared.library.usuarios.commons.app.models.entity.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-usuarios", path = "/usuarios")
public interface IUsuarioFeignClient {
    @GetMapping(path = "/search/buscar-usuario")
    Usuario findByUsername(@RequestParam(value = "usuario") String username);

}
