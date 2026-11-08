package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.limpacao.entity.Participacao;
import com.limpacao.entity.Relatorio;
import com.limpacao.entity.Usuario;
import com.limpacao.enums.TipoParticipacao;
import com.limpacao.exception.RegraDeNegocioException;
import com.limpacao.mapper.ParticipacaoMapper;
import com.limpacao.repository.ParticipacaoRepository;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ParticipacaoServiceTest {

    private static final String EMAIL = "pessoa@exemplo.com";

    @Mock
    private ParticipacaoRepository participacaoRepository;

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private ParticipacaoService service;

    private final Usuario usuario = Usuario.builder().id(7L).email(EMAIL).build();

    @BeforeEach
    void setUp() {
        service = new ParticipacaoService(
                participacaoRepository,
                relatorioRepository,
                usuarioRepository,
                new ParticipacaoMapper());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
    }

    @Test
    void entraNoMutiraoEAtualizaContagemDoReporte() {
        Relatorio relatorio = reporte(true, 1L);
        when(relatorioRepository.findById(10L)).thenReturn(Optional.of(relatorio));
        when(participacaoRepository.findByRelatorioIdAndUsuarioIdAndTipoParticipacao(
                10L, 7L, TipoParticipacao.PARTICIPANDO)).thenReturn(Optional.empty());
        when(participacaoRepository.saveAndFlush(any(Participacao.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));
        when(participacaoRepository.countByRelatorioIdAndTipoParticipacao(10L, TipoParticipacao.PARTICIPANDO))
                .thenReturn(3L);

        var resultado = service.alternarParticipacao(10L, EMAIL);

        assertNotNull(resultado);
        assertEquals(7L, resultado.usuarioId());
        assertEquals(TipoParticipacao.PARTICIPANDO, resultado.tipoParticipacao());
        assertEquals(3, relatorio.getParticipantes());
        verify(relatorioRepository).save(relatorio);
    }

    @Test
    void saiDoMutiraoEAtualizaContagemDoReporte() {
        Relatorio relatorio = reporte(true, 1L);
        relatorio.setParticipantes(1);
        Participacao existente = Participacao.builder()
                .id(5L).relatorioId(10L).usuarioId(7L)
                .tipoParticipacao(TipoParticipacao.PARTICIPANDO).build();
        when(relatorioRepository.findById(10L)).thenReturn(Optional.of(relatorio));
        when(participacaoRepository.findByRelatorioIdAndUsuarioIdAndTipoParticipacao(
                10L, 7L, TipoParticipacao.PARTICIPANDO)).thenReturn(Optional.of(existente));
        when(participacaoRepository.countByRelatorioIdAndTipoParticipacao(10L, TipoParticipacao.PARTICIPANDO))
                .thenReturn(0L);

        var resultado = service.alternarParticipacao(10L, EMAIL);

        assertNull(resultado);
        verify(participacaoRepository).delete(existente);
        assertEquals(0, relatorio.getParticipantes());
    }

    @Test
    void rejeitaParticiparDeReporteSemMutirao() {
        when(relatorioRepository.findById(10L)).thenReturn(Optional.of(reporte(false, 1L)));

        assertThrows(RegraDeNegocioException.class, () -> service.alternarParticipacao(10L, EMAIL));

        verify(participacaoRepository, never()).saveAndFlush(any());
        verify(relatorioRepository, never()).save(any());
    }

    @Test
    void rejeitaDonoParticipandoDoProprioMutirao() {
        when(relatorioRepository.findById(10L)).thenReturn(Optional.of(reporte(true, 7L)));

        assertThrows(RegraDeNegocioException.class, () -> service.alternarParticipacao(10L, EMAIL));

        verify(participacaoRepository, never()).saveAndFlush(any());
    }

    @Test
    void listaReportesEmQueOUsuarioParticipa() {
        when(participacaoRepository.findByUsuarioIdAndTipoParticipacao(7L, TipoParticipacao.PARTICIPANDO))
                .thenReturn(List.of(
                        Participacao.builder().relatorioId(10L).build(),
                        Participacao.builder().relatorioId(12L).build()));

        assertEquals(List.of(10L, 12L), service.listarMutiroesDoUsuario(EMAIL));
    }

    private Relatorio reporte(boolean multirao, Long donoId) {
        return Relatorio.builder().id(10L).multirao(multirao).usuarioId(donoId).build();
    }
}
