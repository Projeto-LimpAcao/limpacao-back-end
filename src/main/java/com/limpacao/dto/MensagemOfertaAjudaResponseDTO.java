package com.limpacao.dto;

import java.time.OffsetDateTime;

public record MensagemOfertaAjudaResponseDTO(
        Long id,
        Long ofertaId,
        String autor,
        String texto,
        OffsetDateTime criadoEm,
        boolean minha) {
}
