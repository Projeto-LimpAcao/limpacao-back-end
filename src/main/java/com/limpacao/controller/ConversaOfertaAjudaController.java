package com.limpacao.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.MensagemOfertaAjudaRequestDTO;
import com.limpacao.dto.MensagemOfertaAjudaResponseDTO;
import com.limpacao.service.ConversaOfertaAjudaService;
import com.limpacao.service.NotificacaoMensagemOfertaAjudaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ofertas-ajuda/{ofertaId}/mensagens")
@RequiredArgsConstructor
public class ConversaOfertaAjudaController {

    private final ConversaOfertaAjudaService service;
    private final NotificacaoMensagemOfertaAjudaService notificacaoService;

    @GetMapping
    public ResponseEntity<List<MensagemOfertaAjudaResponseDTO>> listar(
            @PathVariable Long ofertaId,
            Authentication authentication) {
        var mensagens = service.listar(ofertaId, authentication.getName());
        notificacaoService.marcarComoLidas(ofertaId, authentication.getName());
        return ResponseEntity.ok(mensagens);
    }

    @PostMapping
    public ResponseEntity<MensagemOfertaAjudaResponseDTO> enviar(
            @PathVariable Long ofertaId,
            @RequestBody MensagemOfertaAjudaRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(service.enviar(
                ofertaId,
                request,
                authentication.getName()));
    }
}
