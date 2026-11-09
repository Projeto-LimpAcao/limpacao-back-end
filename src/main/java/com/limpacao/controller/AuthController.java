package com.limpacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.AuthRequestDTO;
import com.limpacao.dto.AuthResponseDTO;
import com.limpacao.dto.RedefinirSenhaRequestDTO;
import com.limpacao.dto.SolicitarRecuperacaoSenhaRequestDTO;
import com.limpacao.dto.UsuarioRequestDTO;
import com.limpacao.entity.Usuario;
import com.limpacao.exception.CredenciaisInvalidasException;
import com.limpacao.exception.RegraDeNegocioException;
import com.limpacao.repository.UsuarioRepository;
import com.limpacao.security.JwtService;
import com.limpacao.service.RecuperacaoSenhaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraDeNegocioException("Este e-mail já está cadastrado.");
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome().trim())
                .email(dto.email().trim())
                .senha(passwordEncoder.encode(dto.senha()))
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario.getEmail());
        return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getEmail(), usuario.getNome()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO dto) {
        var usuario = usuarioRepository.findByEmail(dto.email().trim())
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos.");
        }

        String token = jwtService.gerarToken(usuario.getEmail());
        return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getEmail(), usuario.getNome()));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@RequestBody @Valid SolicitarRecuperacaoSenhaRequestDTO dto) {
        recuperacaoSenhaService.solicitar(dto.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequestDTO dto) {
        recuperacaoSenhaService.redefinir(dto);
        return ResponseEntity.noContent().build();
    }
}
