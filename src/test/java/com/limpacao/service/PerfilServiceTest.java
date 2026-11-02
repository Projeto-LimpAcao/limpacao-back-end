package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.AtualizarPerfilRequestDTO;
import com.limpacao.entity.Usuario;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PerfilService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new PerfilService(usuarioRepository, passwordEncoder);
        usuario = Usuario.builder()
                .id(1L)
                .nome("Nome antigo")
                .email("pessoa@exemplo.com")
                .senha("hash-antigo")
                .build();
        when(usuarioRepository.findByEmail("pessoa@exemplo.com"))
                .thenReturn(Optional.of(usuario));
    }

    @Test
    void atualizaNomeSemAlterarSenha() {
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        var resultado = service.atualizar(
                "pessoa@exemplo.com",
                new AtualizarPerfilRequestDTO(" Nome novo ", null, null));

        assertEquals("Nome novo", resultado.nome());
        assertEquals("hash-antigo", usuario.getSenha());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void exigeSenhaAtualParaTrocarSenha() {
        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizar(
                        "pessoa@exemplo.com",
                        new AtualizarPerfilRequestDTO("Nome", null, "nova123")));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void trocaSenhaDepoisDeValidarSenhaAtual() {
        when(passwordEncoder.matches("atual123", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.matches("nova123", "hash-antigo")).thenReturn(false);
        when(passwordEncoder.encode("nova123")).thenReturn("hash-novo");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        service.atualizar(
                "pessoa@exemplo.com",
                new AtualizarPerfilRequestDTO("Nome", "atual123", "nova123"));

        assertEquals("hash-novo", usuario.getSenha());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rejeitaSenhaAtualIncorreta() {
        when(passwordEncoder.matches("errada1", "hash-antigo")).thenReturn(false);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizar(
                        "pessoa@exemplo.com",
                        new AtualizarPerfilRequestDTO("Nome", "errada1", "nova123")));

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }
}
