package com.example.Fatia.Prime;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);

    boolean existsByRole(UsuarioRole role);

    Optional<Usuario> findByEmailIgnoreCase(String email);
}