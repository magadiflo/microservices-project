package dev.magadiflo.user.app.repository;

import dev.magadiflo.user.app.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Set<Role> findAllByNameIn(Collection<String> roleNames);

    Optional<Role> findByName(String name);
}
