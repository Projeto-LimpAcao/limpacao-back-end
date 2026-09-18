package com.limpacao.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.limpacao.dto.UsuarioRequestDTO;
import com.limpacao.dto.UsuarioResponseDTO;
import com.limpacao.entity.Usuario;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequestDTO dto) {
        if (dto == null) return null;
        return Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(dto.senha())
                .build();
    }

    public UsuarioResponseDTO toDTO(Usuario entity) {
        if (entity == null) return null;
        return new UsuarioResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getCriadoEm());
    }

    public List<UsuarioResponseDTO> toDTOList(List<Usuario> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDTO).toList();
    }
}
