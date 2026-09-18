package com.limpacao.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.limpacao.dto.ParticipacaoRequestDTO;
import com.limpacao.dto.ParticipacaoResponseDTO;
import com.limpacao.entity.Participacao;

@Component
public class ParticipacaoMapper {

    public Participacao toEntity(ParticipacaoRequestDTO dto) {
        if (dto == null) return null;
        return Participacao.builder()
                .relatorioId(dto.relatorioId())
                .usuarioId(dto.usuarioId())
                .tipoParticipacao(dto.tipoParticipacao())
                .build();
    }

    public ParticipacaoResponseDTO toDTO(Participacao entity) {
        if (entity == null) return null;
        return new ParticipacaoResponseDTO(
                entity.getId(),
                entity.getRelatorioId(),
                entity.getUsuarioId(),
                entity.getTipoParticipacao(),
                entity.getCriadoEm());
    }

    public List<ParticipacaoResponseDTO> toDTOList(List<Participacao> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDTO).toList();
    }
}
