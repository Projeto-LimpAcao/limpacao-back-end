package com.limpacao.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.MensagemChatContagemDTO;
import com.limpacao.dto.MensagemChatResponseDTO;
import com.limpacao.service.MensagemChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat/mensagens")
@RequiredArgsConstructor
public class MensagemChatController {

    private final MensagemChatService service;

    @GetMapping("/relatorio/{relatorioId}")
    public ResponseEntity<List<MensagemChatResponseDTO>> listarPorRelatorio(
            @PathVariable Long relatorioId) {
        return ResponseEntity.ok(service.listarPorRelatorio(relatorioId));
    }

    @GetMapping("/contagens")
    public ResponseEntity<List<MensagemChatContagemDTO>> contarPorRelatorio() {
        return ResponseEntity.ok(service.contarPorRelatorio());
    }
}
