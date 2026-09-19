package com.limpacao.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.limpacao.dto.MensagemChatRequestDTO;
import com.limpacao.dto.MensagemChatResponseDTO;
import com.limpacao.service.MensagemChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MensagemChatService service;

    @MessageMapping("/chat/{relatorioId}")
    @SendTo("/topic/chat/{relatorioId}")
    public MensagemChatResponseDTO enviar(@DestinationVariable Long relatorioId,
            MensagemChatRequestDTO dto) {
        return service.salvar(dto, relatorioId);
    }
}
