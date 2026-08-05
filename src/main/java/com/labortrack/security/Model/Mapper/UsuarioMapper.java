package com.labortrack.security.Model.Mapper;

import com.labortrack.security.Model.Dto.Request.CreateUsuarioRequestDto;
import com.labortrack.security.Model.Dto.Response.UserResponseDto;
import com.labortrack.security.Model.Entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "googleId", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    Usuario toEntity(CreateUsuarioRequestDto dto);


    UserResponseDto toDto(Usuario usuario);
}
