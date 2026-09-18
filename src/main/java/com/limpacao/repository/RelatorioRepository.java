package com.limpacao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.limpacao.entity.Relatorio;

public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    List<Relatorio> findByUsuarioId(Long usuarioId);

    List<Relatorio> findByStatus(String status);

    List<Relatorio> findByUrgencia(String urgencia);

    List<Relatorio> findByTituloContainingIgnoreCaseOrLocalizacaoContainingIgnoreCase(String titulo, String localizacao);
}
