package com.limpacao.dto;

import java.time.LocalDateTime;

public record OfertaAjudaResponseDTO(
    Long id,
    Long relatorioId,
    String nome,
    String telefone,
    String mensagem,
    LocalDateTime criadoEm
) {
}
