package com.limpacao.dto;

import java.time.OffsetDateTime;

import com.limpacao.enums.TipoParticipacao;

public record ParticipacaoResponseDTO(
    Long id,
    Long relatorioId,
    Long usuarioId,
    TipoParticipacao tipoParticipacao,
    OffsetDateTime criadoEm
) {
}
