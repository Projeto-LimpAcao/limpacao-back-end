package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.RelatorioRequestDTO;
import com.limpacao.enums.Urgencia;
import com.limpacao.mapper.RelatorioMapper;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RelatorioMapper relatorioMapper;

    private RelatorioService service;

    @BeforeEach
    void setUp() {
        service = new RelatorioService(
                relatorioRepository,
                usuarioRepository,
                relatorioMapper);
    }

    @Test
    void rejeitaCadastroSemImagem() {
        var request = new RelatorioRequestDTO(
                "Título",
                "Referência, Rua A, 10, Florianópolis, SC",
                "88000-000",
                "Rua A",
                "Florianópolis",
                "SC",
                "10",
                null,
                null,
                Urgencia.MEDIA,
                null,
                "[]",
                "Descrição",
                null,
                false,
                null);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.salvar(request, "pessoa@exemplo.com"));

        assertEquals(400, erro.getStatusCode().value());
        verifyNoInteractions(relatorioRepository, usuarioRepository, relatorioMapper);
    }
}
