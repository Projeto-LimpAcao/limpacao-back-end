package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.limpacao.dto.OfertaAjudaRequestDTO;
import com.limpacao.entity.OfertaAjuda;
import com.limpacao.entity.Relatorio;
import com.limpacao.entity.Usuario;
import com.limpacao.mapper.OfertaAjudaMapper;
import com.limpacao.repository.OfertaAjudaRepository;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class OfertaAjudaServiceTest {

    @Mock
    private OfertaAjudaRepository ofertaRepository;

    @Mock
    private OfertaAjudaMapper mapper;

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private OfertaAjudaService service;

    private final Relatorio relatorio = Relatorio.builder().id(10L).usuarioId(1L).build();

    @BeforeEach
    void setUp() {
        service = new OfertaAjudaService(ofertaRepository, mapper, relatorioRepository, usuarioRepository);
        when(relatorioRepository.findById(10L)).thenReturn(Optional.of(relatorio));
    }

    @Test
    void enviarOfertaAtualizaContagemDeAjudantes() {
        Usuario ajudante = Usuario.builder().id(7L).nome("Ana").email("ana@exemplo.com").build();
        when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ajudante));
        when(ofertaRepository.saveAndFlush(any(OfertaAjuda.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));
        when(ofertaRepository.countByRelatorioId(10L)).thenReturn(2L);

        service.salvar(10L, new OfertaAjudaRequestDTO("(48) 99999-9999", "Tenho carro"), "ana@exemplo.com");

        assertEquals(2, relatorio.getAjudantes());
        verify(relatorioRepository).save(relatorio);
    }

    @Test
    void excluirOfertaAtualizaContagemDeAjudantes() {
        relatorio.setAjudantes(1);
        Usuario dono = Usuario.builder().id(1L).email("dono@exemplo.com").build();
        OfertaAjuda oferta = OfertaAjuda.builder().id(3L).relatorioId(10L).usuarioId(7L).build();
        when(ofertaRepository.findById(3L)).thenReturn(Optional.of(oferta));
        when(usuarioRepository.findByEmail("dono@exemplo.com")).thenReturn(Optional.of(dono));
        when(ofertaRepository.countByRelatorioId(10L)).thenReturn(0L);

        service.deletar(3L, "dono@exemplo.com");

        verify(ofertaRepository).delete(oferta);
        assertEquals(0, relatorio.getAjudantes());
    }
}
