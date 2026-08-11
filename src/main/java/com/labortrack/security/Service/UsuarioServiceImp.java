package com.labortrack.security.Service;

import com.labortrack.security.Model.Dto.Request.CreateUsuarioRequestDto;
import com.labortrack.security.Model.Dto.Response.UserResponseDto;
import com.labortrack.security.Model.Entity.Usuario;
import com.labortrack.security.Model.Mapper.UsuarioMapper;
import com.labortrack.security.Repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioServiceImp implements IUsuarioService{
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImp(UsuarioRepository usuarioRepository,
                             UsuarioMapper usuarioMapper){
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public List<UserResponseDto> findAll() {
        List <Usuario> usuarios = usuarioRepository.findAll();
        List<UserResponseDto> userResponseDtos = new ArrayList<>();
        usuarios.stream().forEach(usuario -> userResponseDtos.add(usuarioMapper.toDto(usuario)));
        return userResponseDtos;
    }

    @Override
    public UserResponseDto findById(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id)); //Implementar manejo de excepciones despues
    }

    @Override
    public UserResponseDto createUser(CreateUsuarioRequestDto requestDto) {
        if(usuarioRepository.existsByEmail(requestDto.email())){
            throw new IllegalArgumentException("El email ya se encuentra registrado");
            //throw new BusinnesRuleException --> Implementar manejo de excepciones despues
        }
        String password = encriptPassword(requestDto.password());
        Usuario usuario = usuarioMapper.toEntity(requestDto);
        usuario.setPassword(password);
        Usuario saveUser = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(saveUser);
    }

    @Override
    public Usuario deleteById(Long id) {
        return null;
    }

    @Override
    public String encriptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }
}
