package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.limpacao.dto.RedefinirSenhaRequestDTO;
import com.limpacao.entity.RecuperacaoSenha;
import com.limpacao.entity.Usuario;
import com.limpacao.exception.RegraDeNegocioException;
import com.limpacao.repository.RecuperacaoSenhaRepository;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    private static final String EMAIL = "pessoa@exemplo.com";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecuperacaoSenhaRepository recuperacaoSenhaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private RecuperacaoSenhaService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new RecuperacaoSenhaService(
                usuarioRepository, recuperacaoSenhaRepository, passwordEncoder, emailService);
        usuario = Usuario.builder()
                .id(1L)
                .nome("Pessoa")
                .email(EMAIL)
                .senha("hash-antigo")
                .build();
    }

    @Test
    void naoEnviaCodigoParaEmailNaoCadastrado() {
        when(usuarioRepository.findByEmail("desconhecido@exemplo.com")).thenReturn(Optional.empty());

        service.solicitar("desconhecido@exemplo.com");

        verify(recuperacaoSenhaRepository, never()).save(any());
        verify(emailService, never()).enviarCodigoRecuperacaoSenha(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void geraCodigoDeSeisDigitosSalvaHashEEnviaPorEmail() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash-codigo");

        service.solicitar(" " + EMAIL + " ");

        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarCodigoRecuperacaoSenha(
                eq(EMAIL), eq("Pessoa"), codigo.capture(), eq(RecuperacaoSenhaService.VALIDADE_MINUTOS));
        assertTrue(codigo.getValue().matches("\\d{6}"));
        verify(passwordEncoder).encode(codigo.getValue());

        ArgumentCaptor<RecuperacaoSenha> salvo = ArgumentCaptor.forClass(RecuperacaoSenha.class);
        verify(recuperacaoSenhaRepository).deleteByUsuarioId(1L);
        verify(recuperacaoSenhaRepository).save(salvo.capture());
        assertEquals("hash-codigo", salvo.getValue().getCodigoHash());
        assertEquals(1L, salvo.getValue().getUsuarioId());
        assertTrue(salvo.getValue().getExpiraEm().isAfter(LocalDateTime.now()));
    }

    @Test
    void naoReenviaCodigoAntesDoIntervaloMinimo() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(1L))
                .thenReturn(Optional.of(recuperacao(LocalDateTime.now().plusMinutes(15), 0)));

        service.solicitar(EMAIL);

        verify(recuperacaoSenhaRepository, never()).save(any());
        verify(emailService, never()).enviarCodigoRecuperacaoSenha(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void redefineSenhaComCodigoValido() {
        RecuperacaoSenha recuperacao = recuperacao(LocalDateTime.now().plusMinutes(10), 0);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.of(recuperacao));
        when(passwordEncoder.matches("123456", "hash-codigo")).thenReturn(true);
        when(passwordEncoder.encode("nova123")).thenReturn("hash-novo");

        service.redefinir(new RedefinirSenhaRequestDTO(EMAIL, "123456", "nova123"));

        assertEquals("hash-novo", usuario.getSenha());
        assertTrue(recuperacao.isUtilizado());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void incrementaTentativasQuandoCodigoEstaErrado() {
        RecuperacaoSenha recuperacao = recuperacao(LocalDateTime.now().plusMinutes(10), 0);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.of(recuperacao));
        when(passwordEncoder.matches("000000", "hash-codigo")).thenReturn(false);

        assertThrows(RegraDeNegocioException.class,
                () -> service.redefinir(new RedefinirSenhaRequestDTO(EMAIL, "000000", "nova123")));

        assertEquals(1, recuperacao.getTentativas());
        assertFalse(recuperacao.isUtilizado());
        assertEquals("hash-antigo", usuario.getSenha());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void recusaCodigoExpirado() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(1L))
                .thenReturn(Optional.of(recuperacao(LocalDateTime.now().minusMinutes(1), 0)));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> service.redefinir(new RedefinirSenhaRequestDTO(EMAIL, "123456", "nova123")));

        assertEquals("O código expirou. Solicite um novo código.", erro.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void bloqueiaAposLimiteDeTentativas() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(1L))
                .thenReturn(Optional.of(recuperacao(
                        LocalDateTime.now().plusMinutes(10), RecuperacaoSenhaService.MAXIMO_TENTATIVAS)));

        assertThrows(RegraDeNegocioException.class,
                () -> service.redefinir(new RedefinirSenhaRequestDTO(EMAIL, "123456", "nova123")));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void recusaCodigoJaUtilizado() {
        RecuperacaoSenha recuperacao = recuperacao(LocalDateTime.now().plusMinutes(10), 0);
        recuperacao.setUtilizado(true);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.of(recuperacao));

        assertThrows(RegraDeNegocioException.class,
                () -> service.redefinir(new RedefinirSenhaRequestDTO(EMAIL, "123456", "nova123")));

        verify(usuarioRepository, never()).save(any());
    }

    private RecuperacaoSenha recuperacao(LocalDateTime expiraEm, int tentativas) {
        return RecuperacaoSenha.builder()
                .id(10L)
                .usuarioId(1L)
                .codigoHash("hash-codigo")
                .expiraEm(expiraEm)
                .tentativas(tentativas)
                .criadoEm(LocalDateTime.now())
                .build();
    }
}
