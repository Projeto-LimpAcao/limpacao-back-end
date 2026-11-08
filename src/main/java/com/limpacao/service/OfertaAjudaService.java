package com.limpacao.service;

import java.util.List;
import com.limpacao.exception.RecursoNaoEncontradoException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.OfertaAjudaRequestDTO;
import com.limpacao.dto.OfertaAjudaResponseDTO;
import com.limpacao.entity.OfertaAjuda;
import com.limpacao.entity.Relatorio;
import com.limpacao.mapper.OfertaAjudaMapper;
import com.limpacao.repository.OfertaAjudaRepository;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfertaAjudaService {

    private final OfertaAjudaRepository repository;
    private final OfertaAjudaMapper mapper;
    private final RelatorioRepository relatorioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<OfertaAjudaResponseDTO> listarPorRelatorio(
            Long relatorioId,
            String emailUsuario) {
        validarDonoDoRelatorio(relatorioId, emailUsuario);
        return mapper.toDTOList(repository.findByRelatorioIdOrderByCriadoEm(relatorioId));
    }

    @Transactional(readOnly = true)
    public List<OfertaAjudaResponseDTO> listarMinhas(String emailUsuario) {
        var usuario = buscarUsuario(emailUsuario);
        List<Long> relatorioIds = relatorioRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(relatorio -> relatorio.getId())
                .toList();
        if (relatorioIds.isEmpty()) {
            return List.of();
        }
        return mapper.toDTOList(repository.findByRelatorioIdInOrderByCriadoEmDesc(relatorioIds));
    }

    @Transactional(readOnly = true)
    public List<OfertaAjudaResponseDTO> listarEnviadas(String emailUsuario) {
        var usuario = buscarUsuario(emailUsuario);
        return mapper.toDTOList(repository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId()));
    }

    @Transactional
    public OfertaAjudaResponseDTO salvar(
            Long relatorioId,
            OfertaAjudaRequestDTO dto,
            String emailUsuario) {
        if (dto == null || dto.telefone() == null || dto.telefone().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe seu telefone para enviar a oferta de ajuda.");
        }
        var relatorio = relatorioRepository.findById(relatorioId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Reporte não encontrado."));
        var usuario = buscarUsuario(emailUsuario);
        if (usuario.getId().equals(relatorio.getUsuarioId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Você não pode oferecer ajuda no seu próprio reporte.");
        }
        if (repository.existsByUsuarioIdAndRelatorioId(usuario.getId(), relatorioId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Você já enviou uma oferta de ajuda para este reporte.");
        }
        OfertaAjuda oferta = OfertaAjuda.builder()
                .relatorioId(relatorioId)
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .telefone(dto.telefone().trim())
                .mensagem(dto.mensagem() == null ? "" : dto.mensagem().trim())
                .build();
        OfertaAjudaResponseDTO salva = mapper.toDTO(repository.saveAndFlush(oferta));
        atualizarContagemAjudantes(relatorio);
        return salva;
    }

    @Transactional
    public void deletar(Long id, String emailUsuario) {
        OfertaAjuda oferta = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Oferta de ajuda nao encontrada com id: " + id));
        validarDonoDoRelatorio(oferta.getRelatorioId(), emailUsuario);
        repository.delete(oferta);
        repository.flush();
        relatorioRepository.findById(oferta.getRelatorioId())
                .ifPresent(this::atualizarContagemAjudantes);
    }

    /* Cada oferta de ajuda conta como uma pessoa ajudando; o total fica
       gravado no reporte para aparecer para todos os usuários. */
    private void atualizarContagemAjudantes(Relatorio relatorio) {
        relatorio.setAjudantes((int) repository.countByRelatorioId(relatorio.getId()));
        relatorioRepository.save(relatorio);
    }

    private void validarDonoDoRelatorio(Long relatorioId, String emailUsuario) {
        var relatorio = relatorioRepository.findById(relatorioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reporte não encontrado."));
        var usuario = buscarUsuario(emailUsuario);
        if (!usuario.getId().equals(relatorio.getUsuarioId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente o responsável pelo reporte pode ver esta oferta.");
        }
    }

    private com.limpacao.entity.Usuario buscarUsuario(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."));
    }
}
