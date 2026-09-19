package com.limpacao.dto;

public record AuthResponseDTO(
    String token,
    String email,
    String nome
) {
}
