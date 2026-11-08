package com.limpacao.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.dto.ParticipacaoResponseDTO;
import com.limpacao.entity.Participacao;
import com.limpacao.entity.Relatorio;
import com.limpacao.entity.Usuario;
import com.limpacao.enums.TipoParticipacao;
import com.limpacao.exception.RecursoNaoEncontradoException;
import com.limpacao.exception.RegraDeNegocioException;
import com.limpacao.mapper.ParticipacaoMapper;
import com.limpacao.repository.ParticipacaoRepository;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipacaoService {

    private final ParticipacaoRepository repository;
    private final RelatorioRepository relatorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParticipacaoMapper mapper;


    @Transactional
    public ParticipacaoResponseDTO alternarParticipacao(Long relatorioId, String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        Relatorio relatorio = relatorioRepository.findById(relatorioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reporte não encontrado com id: " + relatorioId));

        if (!Boolean.TRUE.equals(relatorio.getMultirao())) {
            throw new RegraDeNegocioException("Este reporte não tem mutirão organizado.");
        }
        if (usuario.getId().equals(relatorio.getUsuarioId())) {
            throw new RegraDeNegocioException("Quem criou o reporte já é o organizador do mutirão.");
        }

        var existente = repository.findByRelatorioIdAndUsuarioIdAndTipoParticipacao(
                relatorioId, usuario.getId(), TipoParticipacao.PARTICIPANDO);

        ParticipacaoResponseDTO resultado = null;
        if (existente.isPresent()) {
            repository.delete(existente.get());
            repository.flush();
        } else {
            Participacao participacao = Participacao.builder()
                    .relatorioId(relatorioId)
                    .usuarioId(usuario.getId())
                    .tipoParticipacao(TipoParticipacao.PARTICIPANDO)
                    .build();
            resultado = mapper.toDTO(repository.saveAndFlush(participacao));
        }

        relatorio.setParticipantes((int) contarParticipantes(relatorioId));
        relatorioRepository.save(relatorio);
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<Long> listarMutiroesDoUsuario(String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        return repository.findByUsuarioIdAndTipoParticipacao(usuario.getId(), TipoParticipacao.PARTICIPANDO)
                .stream()
                .map(Participacao::getRelatorioId)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarParticipantes(Long relatorioId) {
        return repository.countByRelatorioIdAndTipoParticipacao(relatorioId, TipoParticipacao.PARTICIPANDO);
    }

    @Transactional(readOnly = true)
    public long contarAjudantes(Long relatorioId) {
        return repository.countByRelatorioIdAndTipoParticipacao(relatorioId, TipoParticipacao.AJUDANDO);
    }

    @Transactional(readOnly = true)
    public boolean estaParticipando(Long relatorioId, Long usuarioId, TipoParticipacao tipo) {
        return repository.existsByRelatorioIdAndUsuarioIdAndTipoParticipacao(relatorioId, usuarioId, tipo);
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }
}
