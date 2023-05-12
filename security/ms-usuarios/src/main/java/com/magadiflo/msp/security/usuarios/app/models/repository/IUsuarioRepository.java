package com.magadiflo.msp.security.usuarios.app.models.repository;

import com.magadiflo.msp.security.usuarios.app.models.entity.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "usuarios")
public interface IUsuarioRepository extends PagingAndSortingRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    @Query(value = "SELECT u FROM Usuario AS u WHERE u.username = ?1")
    Optional<Usuario> obtenerPorUsername(String username);
}
