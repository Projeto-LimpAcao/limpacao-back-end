package com.limpacao.dto;

import java.time.LocalDateTime;

public record MensagemChatResponseDTO(
    Long id,
    Long relatorioId,
    String autor,
    String texto,
    LocalDateTime criadoEm
) {
}
