package com.springboot.picpay.simplified.repository;

import com.springboot.picpay.simplified.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByDocumento (String documento);
    Optional<Usuario> findByEmail (String email);
    boolean existsByDocumento (String documento);
    boolean existsByEmail (String email);
}
