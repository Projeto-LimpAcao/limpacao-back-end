package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.PreferenciasUsuarioRequestDTO;
import com.limpacao.dto.PreferenciasUsuarioResponseDTO;
import com.limpacao.entity.Usuario;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PreferenciasUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PreferenciasUsuarioService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new PreferenciasUsuarioService(usuarioRepository);
        usuario = Usuario.builder()
                .id(1L)
                .nome("Pessoa")
                .email("pessoa@exemplo.com")
                .senha("hash")
                .build();
    }

    @Test
    void atualizaEGravaPreferenciasDoUsuarioAutenticado() {
        when(usuarioRepository.findByEmail("pessoa@exemplo.com"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PreferenciasUsuarioResponseDTO resultado = service.atualizar(
                "pessoa@exemplo.com",
                new PreferenciasUsuarioRequestDTO("preto", true));

        assertEquals("preto", resultado.paleta());
        assertTrue(resultado.modoEscuro());
        assertEquals("preto", usuario.getPaletaTema());
        assertTrue(usuario.isModoEscuro());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void retornaPreferenciasPadraoParaContaSemPreferencias() {
        when(usuarioRepository.findByEmail("pessoa@exemplo.com"))
                .thenReturn(Optional.of(usuario));

        PreferenciasUsuarioResponseDTO resultado = service.buscar("pessoa@exemplo.com");

        assertEquals("verde", resultado.paleta());
        assertFalse(resultado.modoEscuro());
    }

    @Test
    void rejeitaPaletaDesconhecida() {
        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizar(
                        "pessoa@exemplo.com",
                        new PreferenciasUsuarioRequestDTO("invalida", false)));

        assertEquals(400, erro.getStatusCode().value());
    }
}
