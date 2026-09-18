package com.limpacao.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.dto.OfertaAjudaRequestDTO;
import com.limpacao.dto.OfertaAjudaResponseDTO;
import com.limpacao.entity.OfertaAjuda;
import com.limpacao.mapper.OfertaAjudaMapper;
import com.limpacao.repository.OfertaAjudaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfertaAjudaService {

    private final OfertaAjudaRepository repository;
    private final OfertaAjudaMapper mapper;

    @Transactional(readOnly = true)
    public List<OfertaAjudaResponseDTO> listarPorRelatorio(Long relatorioId) {
        return mapper.toDTOList(repository.findByRelatorioIdOrderByCriadoEm(relatorioId));
    }

    @Transactional
    public OfertaAjudaResponseDTO salvar(OfertaAjudaRequestDTO dto) {
        OfertaAjuda oferta = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(oferta));
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new java.util.NoSuchElementException("Oferta de ajuda nao encontrada com id: " + id);
        }
        repository.deleteById(id);
    }
}
