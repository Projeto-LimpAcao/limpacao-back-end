package com.limpacao.dto;

import java.time.OffsetDateTime;

public record MensagemChatResponseDTO(
    Long id,
    Long relatorioId,
    String autor,
    String texto,
    OffsetDateTime criadoEm,
    Long respostaAId,
    String respostaAAutor,
    String respostaATexto
) {
}
