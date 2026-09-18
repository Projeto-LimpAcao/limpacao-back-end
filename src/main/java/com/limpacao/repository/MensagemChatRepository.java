package com.limpacao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.limpacao.entity.MensagemChat;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    List<MensagemChat> findByRelatorioIdOrderByCriadoEm(Long relatorioId);
}
