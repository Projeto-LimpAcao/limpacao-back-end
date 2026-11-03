package com.limpacao.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.MensagemOfertaAjudaRequestDTO;
import com.limpacao.dto.MensagemOfertaAjudaResponseDTO;
import com.limpacao.entity.MensagemOfertaAjuda;
import com.limpacao.repository.MensagemOfertaAjudaRepository;
import com.limpacao.repository.OfertaAjudaRepository;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversaOfertaAjudaService {

    private final MensagemOfertaAjudaRepository mensagemRepository;
    private final OfertaAjudaRepository ofertaRepository;
    private final RelatorioRepository relatorioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<MensagemOfertaAjudaResponseDTO> listar(Long ofertaId, String emailUsuario) {
        var usuario = validarParticipante(ofertaId, emailUsuario);
        return mensagemRepository.findByOfertaIdOrderByCriadoEmAsc(ofertaId).stream()
                .map(mensagem -> mapear(mensagem, usuario.getId()))
                .toList();
    }

    @Transactional
    public MensagemOfertaAjudaResponseDTO enviar(
            Long ofertaId,
            MensagemOfertaAjudaRequestDTO request,
            String emailUsuario) {
        var usuario = validarParticipante(ofertaId, emailUsuario);
        if (request == null || request.texto() == null || request.texto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escreva uma mensagem.");
        }
        String texto = request.texto().trim();
        if (texto.length() > 4000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A mensagem deve ter no máximo 4000 caracteres.");
        }

        var mensagem = mensagemRepository.save(MensagemOfertaAjuda.builder()
                .ofertaId(ofertaId)
                .usuarioId(usuario.getId())
                .texto(texto)
                .build());
        return mapear(mensagem, usuario.getId());
    }

    private com.limpacao.entity.Usuario validarParticipante(Long ofertaId, String emailUsuario) {
        var usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."));
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
                    "Somente quem ofereceu ajuda e o responsável pelo reporte podem acessar esta conversa.");
        }
        return usuario;
    }

    private MensagemOfertaAjudaResponseDTO mapear(MensagemOfertaAjuda mensagem, Long usuarioId) {
        String autor = usuarioRepository.findById(mensagem.getUsuarioId())
                .map(com.limpacao.entity.Usuario::getNome)
                .orElse("Usuário");
        return new MensagemOfertaAjudaResponseDTO(
                mensagem.getId(),
                mensagem.getOfertaId(),
                autor,
                mensagem.getTexto(),
                mensagem.getCriadoEm(),
                usuarioId.equals(mensagem.getUsuarioId()));
    }
}
