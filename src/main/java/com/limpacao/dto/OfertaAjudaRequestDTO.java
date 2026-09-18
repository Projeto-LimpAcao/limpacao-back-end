package com.limpacao.dto;

public record OfertaAjudaRequestDTO(
    Long relatorioId,
    String nome,
    String telefone,
    String mensagem
) {
}
