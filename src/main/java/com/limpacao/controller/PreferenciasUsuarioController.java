package com.limpacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.PreferenciasUsuarioRequestDTO;
import com.limpacao.dto.PreferenciasUsuarioResponseDTO;
import com.limpacao.service.PreferenciasUsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/me/preferencias")
@RequiredArgsConstructor
public class PreferenciasUsuarioController {

    private final PreferenciasUsuarioService service;

    @GetMapping
    public ResponseEntity<PreferenciasUsuarioResponseDTO> buscar(Authentication authentication) {
        return ResponseEntity.ok(service.buscar(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<PreferenciasUsuarioResponseDTO> atualizar(
            @RequestBody PreferenciasUsuarioRequestDTO preferencias,
            Authentication authentication) {
        return ResponseEntity.ok(service.atualizar(authentication.getName(), preferencias));
    }
}
