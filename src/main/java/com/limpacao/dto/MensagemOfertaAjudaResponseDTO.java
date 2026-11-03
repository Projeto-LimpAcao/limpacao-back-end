package com.limpacao.dto;

import java.time.LocalDateTime;

public record MensagemOfertaAjudaResponseDTO(
        Long id,
        Long ofertaId,
        String autor,
        String texto,
        LocalDateTime criadoEm,
        boolean minha) {
}
