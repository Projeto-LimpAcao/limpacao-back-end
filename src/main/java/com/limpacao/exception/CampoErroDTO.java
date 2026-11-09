package com.limpacao.exception;

public record CampoErroDTO(
    String campo,
    String mensagem
) {
}
