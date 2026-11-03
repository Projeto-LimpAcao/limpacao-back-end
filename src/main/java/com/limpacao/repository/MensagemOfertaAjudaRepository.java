package com.limpacao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.limpacao.entity.MensagemOfertaAjuda;

public interface MensagemOfertaAjudaRepository extends JpaRepository<MensagemOfertaAjuda, Long> {

    List<MensagemOfertaAjuda> findByOfertaIdOrderByCriadoEmAsc(Long ofertaId);

    java.util.Optional<MensagemOfertaAjuda> findFirstByOfertaIdOrderByIdDesc(Long ofertaId);

    long countByOfertaIdAndIdGreaterThanAndUsuarioIdNot(
            Long ofertaId,
            Long mensagemId,
            Long usuarioId);

    long countByOfertaIdAndUsuarioIdNot(Long ofertaId, Long usuarioId);
}
