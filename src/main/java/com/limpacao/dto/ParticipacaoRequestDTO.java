package com.limpacao.dto;

import com.limpacao.enums.TipoParticipacao;

public record ParticipacaoRequestDTO(
    Long relatorioId,
    Long usuarioId,
    TipoParticipacao tipoParticipacao
) {
}
