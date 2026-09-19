package com.limpacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.dto.ParticipacaoRequestDTO;
import com.limpacao.dto.ParticipacaoResponseDTO;
import com.limpacao.entity.Participacao;
import com.limpacao.enums.TipoParticipacao;
import com.limpacao.mapper.ParticipacaoMapper;
import com.limpacao.repository.ParticipacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipacaoService {

    private final ParticipacaoRepository repository;
    private final ParticipacaoMapper mapper;

    @Transactional
    public ParticipacaoResponseDTO toggle(ParticipacaoRequestDTO dto) {
        var existente = repository.findByRelatorioIdAndUsuarioIdAndTipoParticipacao(
                dto.relatorioId(), dto.usuarioId(), dto.tipoParticipacao());

        if (existente.isPresent()) {
            repository.delete(existente.get());
            return null;
        }

        Participacao participacao = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(participacao));
    }

    @Transactional(readOnly = true)
    public long contarParticipantes(Long relatorioId) {
        return repository.countByRelatorioIdAndTipoParticipacao(relatorioId, TipoParticipacao.PARTICIPANDO);
    }

    @Transactional(readOnly = true)
    public long contarAjudantes(Long relatorioId) {
        return repository.countByRelatorioIdAndTipoParticipacao(relatorioId, TipoParticipacao.AJUDANDO);
    }

    @Transactional(readOnly = true)
    public boolean estaParticipando(Long relatorioId, Long usuarioId, TipoParticipacao tipo) {
        return repository.existsByRelatorioIdAndUsuarioIdAndTipoParticipacao(relatorioId, usuarioId, tipo);
    }
}
