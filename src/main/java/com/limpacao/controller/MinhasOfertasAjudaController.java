package com.limpacao.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.OfertaAjudaResponseDTO;
import com.limpacao.service.OfertaAjudaService;
import com.limpacao.service.NotificacaoMensagemOfertaAjudaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ofertas-ajuda")
@RequiredArgsConstructor
public class MinhasOfertasAjudaController {

    private final OfertaAjudaService service;
    private final NotificacaoMensagemOfertaAjudaService notificacaoService;

    @GetMapping("/minhas")
    public ResponseEntity<List<OfertaAjudaResponseDTO>> listarMinhas(
            Authentication authentication) {
        return ResponseEntity.ok(service.listarMinhas(authentication.getName()));
    }

    @GetMapping("/enviadas")
    public ResponseEntity<List<OfertaAjudaResponseDTO>> listarEnviadas(
            Authentication authentication) {
        return ResponseEntity.ok(service.listarEnviadas(authentication.getName()));
    }

    @GetMapping("/mensagens-nao-lidas")
    public ResponseEntity<List<com.limpacao.dto.MensagemOfertaAjudaNaoLidaDTO>> listarMensagensNaoLidas(
            Authentication authentication) {
        return ResponseEntity.ok(notificacaoService.listarNaoLidas(authentication.getName()));
    }
}
