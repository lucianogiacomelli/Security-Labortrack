package com.labortrack.security.Repository;


import com.labortrack.security.Model.Entity.RefreshToken;
import com.labortrack.security.Model.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Ref;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUsuario(Usuario usuario);
    int deleteByUsuario(Usuario usuario);
    void deleteByToken(String token);
}
