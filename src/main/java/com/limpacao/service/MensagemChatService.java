package com.limpacao.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.dto.MensagemChatRequestDTO;
import com.limpacao.dto.MensagemChatResponseDTO;
import com.limpacao.entity.MensagemChat;
import com.limpacao.mapper.MensagemChatMapper;
import com.limpacao.repository.MensagemChatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MensagemChatService {

    private final MensagemChatRepository repository;
    private final MensagemChatMapper mapper;

    @Transactional(readOnly = true)
    public List<MensagemChatResponseDTO> listarPorRelatorio(Long relatorioId) {
        return mapper.toDTOList(repository.findByRelatorioIdOrderByCriadoEm(relatorioId));
    }

    @Transactional
    public MensagemChatResponseDTO salvar(MensagemChatRequestDTO dto, Long relatorioId) {
        MensagemChat mensagem = mapper.toEntity(dto, relatorioId);
        return mapper.toDTO(repository.save(mensagem));
    }
}
