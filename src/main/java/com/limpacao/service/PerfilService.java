package com.limpacao.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.AtualizarPerfilRequestDTO;
import com.limpacao.dto.PerfilResponseDTO;
import com.limpacao.entity.Usuario;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PerfilResponseDTO atualizar(String emailAutenticado, AtualizarPerfilRequestDTO request) {
        if (request == null || request.nome() == null || request.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe seu nome.");
        }

        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."));

        String novaSenha = request.novaSenha() == null ? "" : request.novaSenha();
        if (!novaSenha.isBlank()) {
            validarSenha(request.senhaAtual(), novaSenha, usuario);
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        usuario.setNome(request.nome().trim());
        Usuario atualizado = usuarioRepository.save(usuario);
        return new PerfilResponseDTO(atualizado.getNome(), atualizado.getEmail());
    }

    private void validarSenha(String senhaAtual, String novaSenha, Usuario usuario) {
        if (senhaAtual == null || senhaAtual.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe a senha atual para definir uma nova senha.");
        }
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A senha atual está incorreta.");
        }
        if (novaSenha.length() < TAMANHO_MINIMO_SENHA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A nova senha deve ter pelo menos 6 caracteres.");
        }
        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A nova senha deve ser diferente da senha atual.");
        }
    }
}
