package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.RelatorioRequestDTO;
import com.limpacao.entity.Relatorio;
import com.limpacao.enums.StatusRelatorio;
import com.limpacao.enums.Urgencia;
import com.limpacao.exception.RegraDeNegocioException;
import com.limpacao.mapper.RelatorioMapper;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    private static final String FOTO_CONCLUSAO = "[\"data:image/png;base64,AAAA\"]";

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
                null,
                null,
                null);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.salvar(request, "pessoa@exemplo.com"));

        assertEquals(400, erro.getStatusCode().value());
        verifyNoInteractions(relatorioRepository, usuarioRepository, relatorioMapper);
    }

    @Test
    void rejeitaResolverSemFotoDeConclusao() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.EM_ANDAMENTO);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));

        assertThrows(
                RegraDeNegocioException.class,
                () -> service.atualizar(1L, mudancaDeStatus(StatusRelatorio.RESOLVIDO, "[]", null)));

        assertEquals(StatusRelatorio.EM_ANDAMENTO, relatorio.getStatus());
        verify(relatorioRepository, never()).save(any());
    }

    @Test
    void resolveComFotoDeConclusao() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.PENDENTE);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(relatorioRepository.save(relatorio)).thenReturn(relatorio);

        service.atualizar(1L, mudancaDeStatus(StatusRelatorio.RESOLVIDO, FOTO_CONCLUSAO, null));

        assertEquals(StatusRelatorio.RESOLVIDO, relatorio.getStatus());
        assertEquals("PENDENTE", relatorio.getStatusAnterior());
        assertEquals(FOTO_CONCLUSAO, relatorio.getImagemConclusao());
    }

    @Test
    void rejeitaReabrirResolvidoSemJustificativa() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.RESOLVIDO);
        relatorio.setImagemConclusao(FOTO_CONCLUSAO);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));

        assertThrows(
                RegraDeNegocioException.class,
                () -> service.atualizar(1L, mudancaDeStatus(StatusRelatorio.EM_ANDAMENTO, null, "   ")));

        assertEquals(StatusRelatorio.RESOLVIDO, relatorio.getStatus());
        assertEquals(FOTO_CONCLUSAO, relatorio.getImagemConclusao());
        verify(relatorioRepository, never()).save(any());
    }

    @Test
    void reabreResolvidoComJustificativaEDescartaFoto() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.RESOLVIDO);
        relatorio.setImagemConclusao(FOTO_CONCLUSAO);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(relatorioRepository.save(relatorio)).thenReturn(relatorio);

        service.atualizar(1L, mudancaDeStatus(
                StatusRelatorio.EM_ANDAMENTO, null, "  O lixo voltou a ser descartado no local.  "));

        assertEquals(StatusRelatorio.EM_ANDAMENTO, relatorio.getStatus());
        assertEquals("RESOLVIDO", relatorio.getStatusAnterior());
        assertEquals("O lixo voltou a ser descartado no local.", relatorio.getJustificativaReabertura());
        assertNull(relatorio.getImagemConclusao());
    }

    @Test
    void iniciaAndamentoSemExigenciasExtras() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.PENDENTE);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(relatorioRepository.save(relatorio)).thenReturn(relatorio);

        service.atualizar(1L, mudancaDeStatus(StatusRelatorio.EM_ANDAMENTO, null, null));

        assertEquals(StatusRelatorio.EM_ANDAMENTO, relatorio.getStatus());
        assertEquals("PENDENTE", relatorio.getStatusAnterior());
    }

    private Relatorio relatorioComStatus(StatusRelatorio status) {
        return Relatorio.builder().id(1L).status(status).build();
    }

    @Test
    void edicaoComEnderecoGravaCoordenadas() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.PENDENTE);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(relatorioRepository.save(relatorio)).thenReturn(relatorio);

        service.atualizar(1L, edicaoDeEndereco(-27.5954, -48.548));

        assertEquals(-27.5954, relatorio.getLatitude());
        assertEquals(-48.548, relatorio.getLongitude());
    }

    @Test
    void edicaoComEnderecoNaoLocalizadoRemoveCoordenadasAntigas() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.PENDENTE);
        relatorio.setLatitude(-27.0);
        relatorio.setLongitude(-48.0);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(relatorioRepository.save(relatorio)).thenReturn(relatorio);

        service.atualizar(1L, edicaoDeEndereco(null, null));

        assertNull(relatorio.getLatitude());
        assertNull(relatorio.getLongitude());
    }

    @Test
    void mudancaDeStatusNaoMexeNasCoordenadas() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.PENDENTE);
        relatorio.setLatitude(-27.0);
        relatorio.setLongitude(-48.0);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(relatorioRepository.save(relatorio)).thenReturn(relatorio);

        service.atualizar(1L, mudancaDeStatus(StatusRelatorio.EM_ANDAMENTO, null, null));

        assertEquals(-27.0, relatorio.getLatitude());
        assertEquals(-48.0, relatorio.getLongitude());
    }

    @Test
    void rejeitaCoordenadasForaDoIntervalo() {
        Relatorio relatorio = relatorioComStatus(StatusRelatorio.PENDENTE);
        when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));

        assertThrows(RegraDeNegocioException.class,
                () -> service.atualizar(1L, edicaoDeEndereco(-127.0, -48.0)));
        assertThrows(RegraDeNegocioException.class,
                () -> service.atualizar(1L, edicaoDeEndereco(-27.0, null)));
        verify(relatorioRepository, never()).save(any());
    }

    private RelatorioRequestDTO edicaoDeEndereco(Double latitude, Double longitude) {
        return new RelatorioRequestDTO(
                null, null, "88000-000", null, null, null, null, latitude, longitude, null,
                null, null, null, null, null, null,
                null, null);
    }

    private RelatorioRequestDTO mudancaDeStatus(
            StatusRelatorio status, String imagemConclusao, String justificativa) {
        return new RelatorioRequestDTO(
                null, null, null, null, null, null, null, null, null, null,
                status, null, null, null, null, null,
                imagemConclusao, justificativa);
    }
}
