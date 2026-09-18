package com.limpacao.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.limpacao.dto.MensagemChatRequestDTO;
import com.limpacao.dto.MensagemChatResponseDTO;
import com.limpacao.service.MensagemChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/relatorios/{relatorioId}/chat")
@RequiredArgsConstructor
public class MensagemChatController {

    private final MensagemChatService service;

    @GetMapping
    public ResponseEntity<List<MensagemChatResponseDTO>> listar(@PathVariable Long relatorioId) {
        return ResponseEntity.ok(service.listarPorRelatorio(relatorioId));
    }

    @PostMapping
    public ResponseEntity<MensagemChatResponseDTO> salvar(@PathVariable Long relatorioId,
            @RequestBody MensagemChatRequestDTO dto) {
        MensagemChatRequestDTO request = new MensagemChatRequestDTO(relatorioId, dto.autor(), dto.texto());
        MensagemChatResponseDTO criado = service.salvar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }
}
