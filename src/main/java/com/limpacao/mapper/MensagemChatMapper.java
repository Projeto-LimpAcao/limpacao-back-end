package com.limpacao.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.limpacao.dto.MensagemChatRequestDTO;
import com.limpacao.dto.MensagemChatResponseDTO;
import com.limpacao.entity.MensagemChat;

@Component
public class MensagemChatMapper {

    public MensagemChat toEntity(MensagemChatRequestDTO dto) {
        if (dto == null) return null;
        return MensagemChat.builder()
                .relatorioId(dto.relatorioId())
                .autor(dto.autor())
                .texto(dto.texto())
                .build();
    }

    public MensagemChatResponseDTO toDTO(MensagemChat entity) {
        if (entity == null) return null;
        return new MensagemChatResponseDTO(
                entity.getId(),
                entity.getRelatorioId(),
                entity.getAutor(),
                entity.getTexto(),
                entity.getCriadoEm());
    }

    public List<MensagemChatResponseDTO> toDTOList(List<MensagemChat> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDTO).toList();
    }
}
