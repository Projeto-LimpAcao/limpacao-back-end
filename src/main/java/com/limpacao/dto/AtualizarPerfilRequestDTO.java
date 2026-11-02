package com.limpacao.dto;

public record AtualizarPerfilRequestDTO(
        String nome,
        String senhaAtual,
        String novaSenha) {
}
