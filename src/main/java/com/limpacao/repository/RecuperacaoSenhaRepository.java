package com.limpacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.limpacao.entity.RecuperacaoSenha;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, Long> {

    Optional<RecuperacaoSenha> findTopByUsuarioIdOrderByIdDesc(Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}
