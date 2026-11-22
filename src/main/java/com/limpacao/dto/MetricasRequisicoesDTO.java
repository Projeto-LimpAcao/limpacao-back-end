package com.limpacao.dto;

import java.util.List;

public record MetricasRequisicoesDTO(
        long total,
        long sucesso,
        long errosCliente,
        long errosServidor,
        long tempoMedioMs,
        List<MetodoDTO> metodos,
        List<RotaDTO> rotas) {

    public record MetodoDTO(
            String metodo,
            long total,
            long erros) {
    }

    public record RotaDTO(
            String metodo,
            String rota,
            long total,
            long erros,
            long tempoMedioMs) {
    }
}
