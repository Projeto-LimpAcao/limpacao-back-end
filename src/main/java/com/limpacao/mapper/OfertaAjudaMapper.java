package com.limpacao.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.limpacao.dto.OfertaAjudaRequestDTO;
import com.limpacao.dto.OfertaAjudaResponseDTO;
import com.limpacao.entity.OfertaAjuda;

@Component
public class OfertaAjudaMapper {

    public OfertaAjuda toEntity(OfertaAjudaRequestDTO dto) {
        if (dto == null) return null;
        return OfertaAjuda.builder()
                .relatorioId(dto.relatorioId())
                .nome(dto.nome())
                .telefone(dto.telefone())
                .mensagem(dto.mensagem())
                .build();
    }

    public OfertaAjudaResponseDTO toDTO(OfertaAjuda entity) {
        if (entity == null) return null;
        return new OfertaAjudaResponseDTO(
                entity.getId(),
                entity.getRelatorioId(),
                entity.getNome(),
                entity.getTelefone(),
                entity.getMensagem(),
                entity.getCriadoEm());
    }

    public List<OfertaAjudaResponseDTO> toDTOList(List<OfertaAjuda> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDTO).toList();
    }
}
