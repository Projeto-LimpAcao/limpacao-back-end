package com.limpacao.dto;

public record UsuarioRequestDTO(
    String nome,
    String email,
    String senha
) {
}
