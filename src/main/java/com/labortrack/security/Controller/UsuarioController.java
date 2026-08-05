package com.labortrack.security.Controller;

import com.labortrack.security.Model.Dto.Request.CreateUsuarioRequestDto;
import com.labortrack.security.Model.Dto.Response.UserResponseDto;
import com.labortrack.security.Service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@PreAuthorize("denyAll()")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }
    @GetMapping("/hi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("Hello");
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponseDto> create(@RequestBody @Valid CreateUsuarioRequestDto dto){
        UserResponseDto usuario = usuarioService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @GetMapping("/{idUser}")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH','OPERARIO')")
    public ResponseEntity<UserResponseDto> findById (@PathVariable Long idUser){
        UserResponseDto userResponseDto = usuarioService.findById(idUser);
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public ResponseEntity<List<UserResponseDto>> findByAll (){
        List<UserResponseDto> userResponseDto = usuarioService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDto);
    }
}
