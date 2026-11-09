package com.limpacao.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.dto.RedefinirSenhaRequestDTO;
import com.limpacao.entity.RecuperacaoSenha;
import com.limpacao.entity.Usuario;
import com.limpacao.exception.RegraDeNegocioException;
import com.limpacao.repository.RecuperacaoSenhaRepository;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    static final int VALIDADE_MINUTOS = 15;
    static final int MAXIMO_TENTATIVAS = 5;
    static final int INTERVALO_REENVIO_SEGUNDOS = 60;

    private static final String CODIGO_INVALIDO = "Código inválido ou expirado.";

    private final UsuarioRepository usuarioRepository;
    private final RecuperacaoSenhaRepository recuperacaoSenhaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom geradorAleatorio = new SecureRandom();

    @Transactional
    public void solicitar(String email) {
        var usuarioEncontrado = usuarioRepository.findByEmail(email.trim());
        if (usuarioEncontrado.isEmpty()) {
            log.info("Recuperação de senha solicitada para e-mail não cadastrado.");
            return;
        }

        Usuario usuario = usuarioEncontrado.get();
        if (solicitadoRecentemente(usuario.getId())) {
            log.info("Recuperação de senha ignorada: código enviado há menos de {}s para o usuário {}.",
                    INTERVALO_REENVIO_SEGUNDOS, usuario.getId());
            return;
        }

        String codigo = String.format("%06d", geradorAleatorio.nextInt(1_000_000));

        recuperacaoSenhaRepository.deleteByUsuarioId(usuario.getId());
        recuperacaoSenhaRepository.save(RecuperacaoSenha.builder()
                .usuarioId(usuario.getId())
                .codigoHash(passwordEncoder.encode(codigo))
                .expiraEm(LocalDateTime.now().plusMinutes(VALIDADE_MINUTOS))
                .build());

        emailService.enviarCodigoRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), codigo, VALIDADE_MINUTOS);
    }

    @Transactional(noRollbackFor = RegraDeNegocioException.class)
    public void redefinir(RedefinirSenhaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email().trim())
                .orElseThrow(() -> new RegraDeNegocioException(CODIGO_INVALIDO));

        RecuperacaoSenha recuperacao = recuperacaoSenhaRepository
                .findTopByUsuarioIdOrderByIdDesc(usuario.getId())
                .filter(registro -> !registro.isUtilizado())
                .orElseThrow(() -> new RegraDeNegocioException(CODIGO_INVALIDO));

        if (recuperacao.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RegraDeNegocioException("O código expirou. Solicite um novo código.");
        }

        if (recuperacao.getTentativas() >= MAXIMO_TENTATIVAS) {
            throw new RegraDeNegocioException("Limite de tentativas atingido. Solicite um novo código.");
        }

        if (!passwordEncoder.matches(dto.codigo(), recuperacao.getCodigoHash())) {
            recuperacao.setTentativas(recuperacao.getTentativas() + 1);
            recuperacaoSenhaRepository.save(recuperacao);
            throw new RegraDeNegocioException(CODIGO_INVALIDO);
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);

        recuperacao.setUtilizado(true);
        recuperacaoSenhaRepository.save(recuperacao);
    }

    private boolean solicitadoRecentemente(Long usuarioId) {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(INTERVALO_REENVIO_SEGUNDOS);
        return recuperacaoSenhaRepository.findTopByUsuarioIdOrderByIdDesc(usuarioId)
                .filter(registro -> !registro.isUtilizado())
                .map(RecuperacaoSenha::getCriadoEm)
                .filter(criadoEm -> criadoEm.isAfter(limite))
                .isPresent();
    }
}
