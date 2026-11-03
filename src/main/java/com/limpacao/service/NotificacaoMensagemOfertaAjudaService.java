package com.limpacao.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.MensagemOfertaAjudaNaoLidaDTO;
import com.limpacao.entity.LeituraMensagemOfertaAjuda;
import com.limpacao.repository.LeituraMensagemOfertaAjudaRepository;
import com.limpacao.repository.MensagemOfertaAjudaRepository;
import com.limpacao.repository.OfertaAjudaRepository;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacaoMensagemOfertaAjudaService {

    private final LeituraMensagemOfertaAjudaRepository leituraRepository;
    private final MensagemOfertaAjudaRepository mensagemRepository;
    private final OfertaAjudaRepository ofertaRepository;
    private final RelatorioRepository relatorioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<MensagemOfertaAjudaNaoLidaDTO> listarNaoLidas(String emailUsuario) {
        var usuario = buscarUsuario(emailUsuario);
        var ofertas = new LinkedHashMap<Long, com.limpacao.entity.OfertaAjuda>();

        var relatorioIds = relatorioRepository.findByUsuarioId(usuario.getId()).stream()
                .map(com.limpacao.entity.Relatorio::getId)
                .toList();
        if (!relatorioIds.isEmpty()) {
            ofertaRepository.findByRelatorioIdInOrderByCriadoEmDesc(relatorioIds)
                    .forEach(oferta -> ofertas.put(oferta.getId(), oferta));
        }
        ofertaRepository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId())
                .forEach(oferta -> ofertas.put(oferta.getId(), oferta));

        return ofertas.values().stream()
                .map(oferta -> {
                    var leitura = leituraRepository.findByOfertaIdAndUsuarioId(
                            oferta.getId(),
                            usuario.getId());
                    long quantidade = leitura
                            .filter(registro -> registro.getUltimaMensagemId() != null)
                            .map(registro -> mensagemRepository
                                    .countByOfertaIdAndIdGreaterThanAndUsuarioIdNot(
                                            oferta.getId(),
                                            registro.getUltimaMensagemId(),
                                            usuario.getId()))
                            .orElseGet(() -> mensagemRepository
                                    .countByOfertaIdAndUsuarioIdNot(
                                            oferta.getId(),
                                            usuario.getId()));
                    return new MensagemOfertaAjudaNaoLidaDTO(
                            oferta.getId(),
                            oferta.getRelatorioId(),
                            quantidade);
                })
                .filter(notificacao -> notificacao.quantidade() > 0)
                .toList();
    }

    @Transactional
    public void marcarComoLidas(Long ofertaId, String emailUsuario) {
        var usuario = buscarUsuario(emailUsuario);
        var oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Oferta de ajuda não encontrada."));
        var relatorio = relatorioRepository.findById(oferta.getRelatorioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reporte não encontrado."));
        boolean ajudante = usuario.getId().equals(oferta.getUsuarioId());
        boolean responsavel = usuario.getId().equals(relatorio.getUsuarioId());
        if (!ajudante && !responsavel) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente os participantes podem marcar esta conversa como lida.");
        }

        var leitura = leituraRepository.findByOfertaIdAndUsuarioId(ofertaId, usuario.getId())
                .orElseGet(() -> LeituraMensagemOfertaAjuda.builder()
                        .ofertaId(ofertaId)
                        .usuarioId(usuario.getId())
                        .build());
        leitura.setLidaEm(LocalDateTime.now());
        leitura.setUltimaMensagemId(mensagemRepository
                .findFirstByOfertaIdOrderByIdDesc(ofertaId)
                .map(com.limpacao.entity.MensagemOfertaAjuda::getId)
                .orElse(0L));
        leituraRepository.save(leitura);
    }

    private com.limpacao.entity.Usuario buscarUsuario(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."));
    }
}
