package com.limpacao.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.limpacao.enums.StatusRelatorio;
import com.limpacao.enums.Urgencia;

public record RelatorioResponseDTO(
    Long id,
    String titulo,
    String localizacao,
    Double latitude,
    Double longitude,
    Urgencia urgencia,
    StatusRelatorio status,
    LocalDate data,
    String imagem,
    String descricao,
    String autor,
    Integer participantes,
    Integer ajudantes,
    Boolean multirao,
    String statusAnterior,
    Long usuarioId,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm
) {
}
