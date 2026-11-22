package com.limpacao.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.limpacao.entity.Usuario;
import com.limpacao.enums.PerfilUsuario;
import com.limpacao.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdministradorInicialTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdministradorInicial criar(String email, String senha) {
        return criar(email, senha, "");
    }

    private AdministradorInicial criar(String email, String senha, String lista) {
        return new AdministradorInicial(usuarioRepository, passwordEncoder,
                new ListaAdministradores(lista), email, senha, "Admin");
    }

    @Test
    void naoFazNadaSemEmailConfigurado() {
        criar("", "segredo").run(null);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void promoveContasJaCadastradasDaListaDeAdministradores() {
        Usuario usuario = Usuario.builder().email("limpacao.projeto@gmail.com").senha("hash").build();
        when(usuarioRepository.findByEmail("limpacao.projeto@gmail.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("ainda-nao-cadastrado@gmail.com")).thenReturn(Optional.empty());

        criar("", "", "limpacao.projeto@gmail.com, ainda-nao-cadastrado@gmail.com").run(null);

        assertEquals(PerfilUsuario.ADMIN, usuario.getPerfil());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void criaAdministradorQuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail("admin@limpacao.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("segredo")).thenReturn("hash");

        criar("admin@limpacao.com", "segredo").run(null);

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertEquals(PerfilUsuario.ADMIN, salvo.getValue().getPerfil());
        assertEquals("hash", salvo.getValue().getSenha());
    }

    @Test
    void promoveUsuarioExistenteSemTrocarSenha() {
        Usuario usuario = Usuario.builder().email("ana@exemplo.com").senha("hash-antigo").build();
        when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario));

        criar("ana@exemplo.com", "outra").run(null);

        assertEquals(PerfilUsuario.ADMIN, usuario.getPerfil());
        assertEquals("hash-antigo", usuario.getSenha());
        verify(usuarioRepository).save(usuario);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void naoCriaAdministradorSemSenha() {
        when(usuarioRepository.findByEmail("admin@limpacao.com")).thenReturn(Optional.empty());

        criar("admin@limpacao.com", "").run(null);

        verify(usuarioRepository, never()).save(any());
    }
}
