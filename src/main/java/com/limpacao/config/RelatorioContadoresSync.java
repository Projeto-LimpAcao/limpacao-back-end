package com.limpacao.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/*
 * Recalcula "ajudantes" e "participantes" de todos os reportes a partir das
 * ofertas de ajuda e das participações gravadas. Corrige reportes antigos,
 * criados quando esses contadores ainda não eram atualizados.
 * Obs.: no PostgreSQL a coluna do SET não pode ter o alias da tabela
 * ("SET ajudantes", e não "SET r.ajudantes").
 */
@Component
@RequiredArgsConstructor
public class RelatorioContadoresSync implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.update("""
                UPDATE relatorios r
                SET ajudantes = (
                        SELECT COUNT(*) FROM ofertas_ajuda o
                        WHERE o.relatorio_id = r.id),
                    participantes = (
                        SELECT COUNT(*) FROM participacoes_relatorio p
                        WHERE p.relatorio_id = r.id
                          AND p.tipo_participacao = 'PARTICIPANDO')
                """);
    }
}
