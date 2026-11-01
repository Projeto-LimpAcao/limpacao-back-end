package com.limpacao.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RelatorioImagemSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<String> tiposColuna = jdbcTemplate.query(
                """
                        SELECT DATA_TYPE
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'relatorios'
                          AND column_name = 'imagem'
                        """,
                (resultado, linha) -> resultado.getString("DATA_TYPE"));

        if (!tiposColuna.isEmpty() && !"longtext".equalsIgnoreCase(tiposColuna.get(0))) {
            jdbcTemplate.execute("ALTER TABLE relatorios MODIFY COLUMN imagem LONGTEXT NULL");
        }
    }
}
