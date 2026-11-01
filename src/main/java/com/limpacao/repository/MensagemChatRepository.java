package com.limpacao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.limpacao.dto.MensagemChatContagemDTO;
import com.limpacao.entity.MensagemChat;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    List<MensagemChat> findByRelatorioIdOrderByCriadoEm(Long relatorioId);

    @Query("""
            select new com.limpacao.dto.MensagemChatContagemDTO(m.relatorioId, count(m.id))
            from MensagemChat m
            group by m.relatorioId
            """)
    List<MensagemChatContagemDTO> contarPorRelatorio();
}
