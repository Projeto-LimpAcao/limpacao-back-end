package com.limpacao.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.limpacao.dto.RelatorioRequestDTO;
import com.limpacao.dto.RelatorioResponseDTO;
import com.limpacao.entity.Relatorio;

@Component
public class RelatorioMapper {

    public Relatorio toEntity(RelatorioRequestDTO dto) {
        if (dto == null) return null;
        return Relatorio.builder()
                .titulo(dto.titulo())
                .localizacao(dto.localizacao())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .urgencia(dto.urgencia())
                .status(dto.status())
                .imagem(dto.imagem())
                .descricao(dto.descricao())
                .autor(dto.autor())
                .multirao(dto.multirao() != null ? dto.multirao() : false)
                .usuarioId(dto.usuarioId())
                .build();
    }

    public RelatorioResponseDTO toDTO(Relatorio entity) {
        if (entity == null) return null;
        return new RelatorioResponseDTO(
                entity.getId(),
                entity.getTitulo(),
                entity.getLocalizacao(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getUrgencia(),
                entity.getStatus(),
                entity.getData(),
                entity.getImagem(),
                entity.getDescricao(),
                entity.getAutor(),
                entity.getParticipantes(),
                entity.getAjudantes(),
                entity.getMultirao(),
                entity.getStatusAnterior(),
                entity.getUsuarioId(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm());
    }

    public List<RelatorioResponseDTO> toDTOList(List<Relatorio> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDTO).toList();
    }
}
