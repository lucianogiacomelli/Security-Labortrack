package com.labortrack.security.Service.Auth;

import com.labortrack.security.Model.Entity.Usuario;
import com.labortrack.security.Repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserDetailsServiceImp implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImp(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("There is no user with email: " + email));

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(usuario.getRol().name())
        );
// Se protege la clave con un string vacío en caso de que el registro sea vía Google (password null)
        String password = usuario.getPassword() != null ? usuario.getPassword() : "";

        return new User(
                usuario.getEmail(),
                password,
                usuario.isEnabled(),
                usuario.isAccountNonExpired(),
                usuario.isCredentialsNonExpired(),
                usuario.isAccountNonLocked(),
                authorities);
    }
}
