package com.limpacao.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.RelatorioRequestDTO;
import com.limpacao.dto.RelatorioResponseDTO;
import com.limpacao.entity.Relatorio;
import com.limpacao.enums.StatusRelatorio;
import com.limpacao.exception.RecursoNaoEncontradoException;
import com.limpacao.exception.RegraDeNegocioException;
import com.limpacao.mapper.RelatorioMapper;
import com.limpacao.repository.RelatorioRepository;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final int TAMANHO_MAXIMO_JUSTIFICATIVA = 500;

    private final RelatorioRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final RelatorioMapper mapper;

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> listar() {
        return mapper.toDTOList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public RelatorioResponseDTO buscarPorId(Long id) {
        Relatorio relatorio = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reporte não encontrado com id: " + id));
        return mapper.toDTO(relatorio);
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> listarPorUsuario(Long usuarioId) {
        return mapper.toDTOList(repository.findByUsuarioId(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> listarPorEmailUsuario(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
        return mapper.toDTOList(repository.findByUsuarioId(usuario.getId()));
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> buscarPorTermo(String termo) {
        return mapper.toDTOList(repository.findByTituloContainingIgnoreCaseOrLocalizacaoContainingIgnoreCase(termo, termo));
    }

    @Transactional
    public RelatorioResponseDTO salvar(RelatorioRequestDTO dto, String emailUsuario) {
        validarNovoReporte(dto);
        var usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
        Relatorio relatorio = mapper.toEntity(dto);
        relatorio.setUsuarioId(usuario.getId());
        relatorio.setAutor(usuario.getNome());
        return mapper.toDTO(repository.save(relatorio));
    }

    private void validarNovoReporte(RelatorioRequestDTO dto) {
        if (dto == null
                || vazio(dto.titulo())
                || vazio(dto.cep())
                || !dto.cep().replaceAll("\\D", "").matches("\\d{8}")
                || vazio(dto.rua())
                || vazio(dto.numero())
                || vazio(dto.cidade())
                || vazio(dto.estado())
                || vazio(dto.localizacao())
                || vazio(dto.descricao())
                || dto.urgencia() == null
                || dto.multirao() == null
                || vazio(dto.imagem())
                || "[]".equals(dto.imagem().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha todos os campos obrigatórios do reporte e adicione pelo menos uma imagem.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    @Transactional
    public RelatorioResponseDTO atualizar(Long id, RelatorioRequestDTO dto) {
        Relatorio existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reporte não encontrado com id: " + id));

        if (dto.titulo() != null) existente.setTitulo(dto.titulo());
        if (dto.localizacao() != null) existente.setLocalizacao(dto.localizacao());
        if (dto.cep() != null) existente.setCep(dto.cep());
        if (dto.rua() != null) existente.setRua(dto.rua());
        if (dto.cidade() != null) existente.setCidade(dto.cidade());
        if (dto.estado() != null) existente.setEstado(dto.estado());
        if (dto.numero() != null) existente.setNumero(dto.numero());
        if (dto.latitude() != null) existente.setLatitude(dto.latitude());
        if (dto.longitude() != null) existente.setLongitude(dto.longitude());
        if (dto.urgencia() != null) existente.setUrgencia(dto.urgencia());
        if (dto.status() != null && dto.status() != existente.getStatus()) {
            aplicarMudancaDeStatus(existente, dto);
        }
        if (dto.imagem() != null) existente.setImagem(dto.imagem());
        if (dto.descricao() != null) existente.setDescricao(dto.descricao());
        if (dto.autor() != null) existente.setAutor(dto.autor());
        if (dto.multirao() != null) existente.setMultirao(dto.multirao());

        return mapper.toDTO(repository.save(existente));
    }

    /*
     * Regras de troca de status:
     * - Para RESOLVIDO: exige pelo menos uma foto que comprove a limpeza.
     * - Saindo de RESOLVIDO: exige justificativa e descarta a foto da conclusão.
     */
    private void aplicarMudancaDeStatus(Relatorio relatorio, RelatorioRequestDTO dto) {
        StatusRelatorio atual = relatorio.getStatus();
        StatusRelatorio novo = dto.status();

        if (novo == StatusRelatorio.RESOLVIDO) {
            if (vazio(dto.imagemConclusao()) || "[]".equals(dto.imagemConclusao().trim())) {
                throw new RegraDeNegocioException(
                        "Anexe pelo menos uma foto comprovando a limpeza para marcar o reporte como resolvido.");
            }
            relatorio.setImagemConclusao(dto.imagemConclusao());
        } else if (atual == StatusRelatorio.RESOLVIDO) {
            if (vazio(dto.justificativa())) {
                throw new RegraDeNegocioException(
                        "Informe uma justificativa para reabrir um reporte resolvido.");
            }
            String justificativa = dto.justificativa().trim();
            if (justificativa.length() > TAMANHO_MAXIMO_JUSTIFICATIVA) {
                throw new RegraDeNegocioException(
                        "A justificativa deve ter no máximo " + TAMANHO_MAXIMO_JUSTIFICATIVA + " caracteres.");
            }
            relatorio.setJustificativaReabertura(justificativa);
            relatorio.setImagemConclusao(null);
        }

        relatorio.setStatusAnterior(atual.name());
        relatorio.setStatus(novo);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Reporte não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }
}
