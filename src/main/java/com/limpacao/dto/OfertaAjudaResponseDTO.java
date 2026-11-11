package com.limpacao.dto;

import java.time.OffsetDateTime;

public record OfertaAjudaResponseDTO(
    Long id,
    Long relatorioId,
    String nome,
    String telefone,
    String mensagem,
    OffsetDateTime criadoEm
) {
}
