package com.limpacao.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.dto.RelatorioRequestDTO;
import com.limpacao.dto.RelatorioResponseDTO;
import com.limpacao.entity.Relatorio;
import com.limpacao.mapper.RelatorioMapper;
import com.limpacao.repository.RelatorioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final RelatorioRepository repository;
    private final RelatorioMapper mapper;

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> listar() {
        return mapper.toDTOList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public RelatorioResponseDTO buscarPorId(Long id) {
        Relatorio relatorio = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Relatorio nao encontrado com id: " + id));
        return mapper.toDTO(relatorio);
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> listarPorUsuario(Long usuarioId) {
        return mapper.toDTOList(repository.findByUsuarioId(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> buscarPorTermo(String termo) {
        return mapper.toDTOList(repository.findByTituloContainingIgnoreCaseOrLocalizacaoContainingIgnoreCase(termo, termo));
    }

    @Transactional
    public RelatorioResponseDTO salvar(RelatorioRequestDTO dto) {
        Relatorio relatorio = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(relatorio));
    }

    @Transactional
    public RelatorioResponseDTO atualizar(Long id, RelatorioRequestDTO dto) {
        Relatorio existente = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Relatorio nao encontrado com id: " + id));

        if (dto.titulo() != null) existente.setTitulo(dto.titulo());
        if (dto.localizacao() != null) existente.setLocalizacao(dto.localizacao());
        if (dto.latitude() != null) existente.setLatitude(dto.latitude());
        if (dto.longitude() != null) existente.setLongitude(dto.longitude());
        if (dto.urgencia() != null) existente.setUrgencia(dto.urgencia());
        if (dto.status() != null) {
            existente.setStatusAnterior(existente.getStatus().name());
            existente.setStatus(dto.status());
        }
        if (dto.imagem() != null) existente.setImagem(dto.imagem());
        if (dto.descricao() != null) existente.setDescricao(dto.descricao());
        if (dto.autor() != null) existente.setAutor(dto.autor());
        if (dto.multirao() != null) existente.setMultirao(dto.multirao());

        return mapper.toDTO(repository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Relatorio nao encontrado com id: " + id);
        }
        repository.deleteById(id);
    }
}
