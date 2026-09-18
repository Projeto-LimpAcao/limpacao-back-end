package com.limpacao.dto;

import com.limpacao.enums.StatusRelatorio;
import com.limpacao.enums.Urgencia;

public record RelatorioRequestDTO(
    String titulo,
    String localizacao,
    Double latitude,
    Double longitude,
    Urgencia urgencia,
    StatusRelatorio status,
    String imagem,
    String descricao,
    String autor,
    Boolean multirao,
    Long usuarioId
) {
}
