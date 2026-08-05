package com.labortrack.security.Model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends Base{
    @Column(unique = true)
    private String email;
    private String password;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String apellido;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    @Column(unique = true, nullable = false)
    private RolNombre rol;

}
