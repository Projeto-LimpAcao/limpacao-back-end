package com.limpacao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.limpacao.entity.OfertaAjuda;

public interface OfertaAjudaRepository extends JpaRepository<OfertaAjuda, Long> {

    List<OfertaAjuda> findByRelatorioIdOrderByCriadoEm(Long relatorioId);
    List<OfertaAjuda> findByRelatorioIdInOrderByCriadoEmDesc(List<Long> relatorioIds);
    List<OfertaAjuda> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);
    boolean existsByUsuarioIdAndRelatorioId(Long usuarioId, Long relatorioId);
}
