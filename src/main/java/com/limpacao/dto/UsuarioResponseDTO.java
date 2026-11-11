package com.limpacao.dto;

import java.time.OffsetDateTime;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    OffsetDateTime criadoEm
) {
}
