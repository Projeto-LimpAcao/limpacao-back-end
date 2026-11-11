package com.limpacao.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.limpacao.enums.StatusRelatorio;
import com.limpacao.enums.Urgencia;

public record RelatorioResponseDTO(
    Long id,
    String titulo,
    String localizacao,
    String cep,
    String rua,
    String cidade,
    String estado,
    String numero,
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
    String imagemConclusao,
    String justificativaReabertura,
    Long usuarioId,
    OffsetDateTime criadoEm,
    OffsetDateTime atualizadoEm
) {
}
