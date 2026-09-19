package com.limpacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.AuthRequestDTO;
import com.limpacao.dto.AuthResponseDTO;
import com.limpacao.dto.UsuarioRequestDTO;
import com.limpacao.entity.Usuario;
import com.limpacao.repository.UsuarioRepository;
import com.limpacao.security.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            return ResponseEntity.badRequest().build();
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario.getEmail());
        return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getEmail(), usuario.getNome()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO dto) {
        var usuario = usuarioRepository.findByEmail(dto.email())
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtService.gerarToken(usuario.getEmail());
        return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getEmail(), usuario.getNome()));
    }
}
