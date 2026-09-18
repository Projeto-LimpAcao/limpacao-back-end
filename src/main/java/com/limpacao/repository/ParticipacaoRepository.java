package com.limpacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.limpacao.entity.Participacao;
import com.limpacao.enums.TipoParticipacao;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

    Optional<Participacao> findByRelatorioIdAndUsuarioIdAndTipoParticipacao(
            Long relatorioId, Long usuarioId, TipoParticipacao tipoParticipacao);

    boolean existsByRelatorioIdAndUsuarioIdAndTipoParticipacao(
            Long relatorioId, Long usuarioId, TipoParticipacao tipoParticipacao);

    long countByRelatorioIdAndTipoParticipacao(Long relatorioId, TipoParticipacao tipoParticipacao);
}
