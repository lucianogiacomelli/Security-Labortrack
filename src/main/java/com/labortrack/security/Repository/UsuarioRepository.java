package com.labortrack.security.Repository;

import com.labortrack.security.Model.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findUserEntityByEmail(String email);

}
