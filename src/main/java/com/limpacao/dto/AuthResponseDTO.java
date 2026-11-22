package com.limpacao.dto;

import com.limpacao.enums.PerfilUsuario;

public record AuthResponseDTO(
    String token,
    String email,
    String nome,
    PerfilUsuario perfil
) {
}
