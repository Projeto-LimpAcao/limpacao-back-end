package com.limpacao.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    LocalDateTime criadoEm
) {
}
