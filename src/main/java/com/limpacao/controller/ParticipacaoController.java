package com.limpacao.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.ParticipacaoResponseDTO;
import com.limpacao.enums.TipoParticipacao;
import com.limpacao.service.ParticipacaoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ParticipacaoController {

    private final ParticipacaoService service;

    @PostMapping("/relatorios/{relatorioId}/participacoes")
    public ResponseEntity<ParticipacaoResponseDTO> alternar(
            @PathVariable Long relatorioId,
            Authentication authentication) {
        ParticipacaoResponseDTO resultado = service.alternarParticipacao(relatorioId, authentication.getName());
        if (resultado == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultado);
    }

    /* Ids dos reportes em cujo mutirão o usuário autenticado está participando. */
    @GetMapping("/participacoes/minhas")
    public ResponseEntity<List<Long>> listarMinhas(Authentication authentication) {
        return ResponseEntity.ok(service.listarMutiroesDoUsuario(authentication.getName()));
    }

    @GetMapping("/relatorios/{relatorioId}/participacoes/verificar")
    public ResponseEntity<Boolean> verificar(@PathVariable Long relatorioId,
            @RequestParam Long usuarioId,
            @RequestParam TipoParticipacao tipo) {
        return ResponseEntity.ok(service.estaParticipando(relatorioId, usuarioId, tipo));
    }
}
