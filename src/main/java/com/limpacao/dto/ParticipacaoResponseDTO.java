package com.limpacao.dto;

import java.time.LocalDateTime;

import com.limpacao.enums.TipoParticipacao;

public record ParticipacaoResponseDTO(
    Long id,
    Long relatorioId,
    Long usuarioId,
    TipoParticipacao tipoParticipacao,
    LocalDateTime criadoEm
) {
}
