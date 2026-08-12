package com.labortrack.security.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Setter
@Getter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant expiryDate;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, Usuario usuario, Instant expiryDate) {
        this.token = token;
        this.usuario = usuario;
        this.expiryDate = expiryDate;
    }
}