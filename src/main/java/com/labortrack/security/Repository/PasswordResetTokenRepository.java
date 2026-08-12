package com.labortrack.security.Repository;

import com.labortrack.security.Model.Entity.PasswordResetToken;
import com.labortrack.security.Model.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUsuario(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
}
