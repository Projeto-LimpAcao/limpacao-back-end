package com.limpacao.exception;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroRespostaDTO(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    int status,
    String erro,
    String mensagem,
    String detail,
    String caminho,
    List<CampoErroDTO> validacoes
) {
    public ErroRespostaDTO(int status, String erro, String mensagem, String caminho) {
        this(LocalDateTime.now(), status, erro, mensagem, mensagem, caminho, null);
    }

    public ErroRespostaDTO(int status, String erro, String mensagem, String caminho, List<CampoErroDTO> validacoes) {
        this(LocalDateTime.now(), status, erro, mensagem, mensagem, caminho, validacoes);
    }
}
