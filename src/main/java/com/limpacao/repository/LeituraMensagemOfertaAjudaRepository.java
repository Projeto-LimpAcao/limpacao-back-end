package com.limpacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.limpacao.entity.LeituraMensagemOfertaAjuda;

public interface LeituraMensagemOfertaAjudaRepository
        extends JpaRepository<LeituraMensagemOfertaAjuda, Long> {

    Optional<LeituraMensagemOfertaAjuda> findByOfertaIdAndUsuarioId(
            Long ofertaId,
            Long usuarioId);
}
