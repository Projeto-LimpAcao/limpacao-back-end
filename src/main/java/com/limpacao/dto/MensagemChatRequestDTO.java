package com.limpacao.dto;

public record MensagemChatRequestDTO(
    Long relatorioId,
    String autor,
    String texto
) {
}
