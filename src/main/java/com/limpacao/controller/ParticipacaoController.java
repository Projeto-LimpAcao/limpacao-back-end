package com.limpacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.ParticipacaoRequestDTO;
import com.limpacao.dto.ParticipacaoResponseDTO;
import com.limpacao.enums.TipoParticipacao;
import com.limpacao.service.ParticipacaoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/relatorios/{relatorioId}/participacoes")
@RequiredArgsConstructor
public class ParticipacaoController {

    private final ParticipacaoService service;

    @PostMapping
    public ResponseEntity<ParticipacaoResponseDTO> toggle(@PathVariable Long relatorioId,
            @RequestBody ParticipacaoRequestDTO dto) {
        ParticipacaoRequestDTO request = new ParticipacaoRequestDTO(relatorioId, dto.usuarioId(), dto.tipoParticipacao());
        ParticipacaoResponseDTO resultado = service.toggle(request);
        if (resultado == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/verificar")
    public ResponseEntity<Boolean> verificar(@PathVariable Long relatorioId,
            @RequestParam Long usuarioId,
            @RequestParam TipoParticipacao tipo) {
        return ResponseEntity.ok(service.estaParticipando(relatorioId, usuarioId, tipo));
    }
}
