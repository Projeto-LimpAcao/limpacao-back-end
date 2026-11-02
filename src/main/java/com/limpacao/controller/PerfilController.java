package com.limpacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.AtualizarPerfilRequestDTO;
import com.limpacao.dto.PerfilResponseDTO;
import com.limpacao.service.PerfilService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/me/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService service;

    @PutMapping
    public ResponseEntity<PerfilResponseDTO> atualizar(
            @RequestBody AtualizarPerfilRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(service.atualizar(authentication.getName(), request));
    }
}
