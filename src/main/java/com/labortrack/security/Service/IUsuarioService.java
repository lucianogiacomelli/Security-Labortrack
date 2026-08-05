package com.labortrack.security.Service;

import com.labortrack.security.Model.Dto.Request.CreateUsuarioRequestDto;
import com.labortrack.security.Model.Dto.Response.UserResponseDto;
import com.labortrack.security.Model.Entity.Usuario;

import java.util.List;

public interface IUsuarioService {
    public List<UserResponseDto> findAll();
    public UserResponseDto findById(Long id);
    public UserResponseDto createUser(CreateUsuarioRequestDto requestDto);
    public Usuario deleteById(Long id);
    public String encriptPassword(String password);
}
