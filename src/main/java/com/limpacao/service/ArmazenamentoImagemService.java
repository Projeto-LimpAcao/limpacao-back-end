package com.limpacao.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.limpacao.exception.RegraDeNegocioException;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/*
 * Grava as imagens dos reportes em disco.
 * O navegador continua enviando as imagens como data URL (base64); aqui elas
 * viram arquivos na pasta configurada e o banco guarda só o caminho público,
 * por exemplo "/api/arquivos/3f2c...png".
 * Reportes antigos ainda em base64 continuam funcionando e são convertidos
 * para disco quando forem editados.
 */
@Slf4j
@Service
public class ArmazenamentoImagemService {

    public static final String PREFIXO_URL = "/api/arquivos/";
    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;

    private static final Pattern DATA_URL = Pattern.compile(
            "^data:image/(png|jpeg|jpg|gif|webp);base64,(.+)$", Pattern.DOTALL);
    private static final Pattern NOME_ARQUIVO = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(png|jpg|gif|webp)$");
    private static final Map<String, String> TIPOS_POR_EXTENSAO = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "gif", "image/gif",
            "webp", "image/webp");

    private final Path diretorio;
    private final JsonMapper json = JsonMapper.builder().build();

    public ArmazenamentoImagemService(
            @Value("${limpacao.uploads.diretorio:uploads}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.diretorio);
        } catch (IOException erro) {
            throw new UncheckedIOException("Não foi possível criar a pasta de uploads: " + this.diretorio, erro);
        }
        log.info("Imagens gravadas em {}", this.diretorio);
    }

    /*
     * Recebe a lista de imagens em JSON (data URLs novas e/ou caminhos já
     * salvos), grava as novas em disco e devolve a lista só com caminhos.
     * Se a transação falhar, os arquivos novos são apagados.
     */
    public String salvarLista(String imagensJson) {
        List<String> imagens = lerLista(imagensJson);
        List<String> caminhos = new ArrayList<>();
        List<String> novos = new ArrayList<>();

        try {
            for (String imagem : imagens) {
                if (imagem.startsWith("data:")) {
                    String caminho = gravarDataUrl(imagem);
                    novos.add(caminho);
                    caminhos.add(caminho);
                } else {
                    caminhos.add(validarCaminhoExistente(imagem));
                }
            }
        } catch (RuntimeException erro) {
            novos.forEach(this::apagarArquivo);
            throw erro;
        }

        aoDesfazerTransacao(novos);
        return json.writeValueAsString(caminhos);
    }

    /* Apaga, depois do commit, os arquivos que estavam na lista antiga e saíram da nova. */
    public void apagarRemovidas(String antigasJson, String novasJson) {
        Set<String> mantidas = new HashSet<>(lerListaSemValidar(novasJson));
        List<String> removidas = lerListaSemValidar(antigasJson).stream()
                .filter(caminho -> !mantidas.contains(caminho))
                .toList();
        aposConfirmarTransacao(removidas);
    }

    /* Apaga, depois do commit, todos os arquivos da lista. */
    public void apagarTodas(String imagensJson) {
        aposConfirmarTransacao(lerListaSemValidar(imagensJson));
    }

    public Optional<Path> localizar(String nome) {
        if (nome == null || !NOME_ARQUIVO.matcher(nome).matches()) {
            return Optional.empty();
        }
        Path arquivo = diretorio.resolve(nome).normalize();
        if (!arquivo.startsWith(diretorio) || !Files.isRegularFile(arquivo)) {
            return Optional.empty();
        }
        return Optional.of(arquivo);
    }

    public String tipoConteudo(String nome) {
        String extensao = nome.substring(nome.lastIndexOf('.') + 1);
        return TIPOS_POR_EXTENSAO.getOrDefault(extensao, "application/octet-stream");
    }

    private String gravarDataUrl(String dataUrl) {
        Matcher partes = DATA_URL.matcher(dataUrl.trim());
        if (!partes.matches()) {
            throw new RegraDeNegocioException("Formato de imagem não suportado. Use JPEG, PNG, GIF ou WebP.");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(partes.group(2).replaceAll("\\s", ""));
        } catch (IllegalArgumentException erro) {
            throw new RegraDeNegocioException("Não foi possível ler uma das imagens enviadas.");
        }
        if (bytes.length == 0 || bytes.length > TAMANHO_MAXIMO) {
            throw new RegraDeNegocioException("Cada imagem deve ter no máximo 5 MB.");
        }

        String extensao = extensaoPeloConteudo(bytes);
        if (extensao == null) {
            throw new RegraDeNegocioException("O arquivo enviado não é uma imagem válida.");
        }

        String nome = UUID.randomUUID() + "." + extensao;
        try {
            Files.write(diretorio.resolve(nome), bytes);
        } catch (IOException erro) {
            throw new UncheckedIOException("Não foi possível gravar a imagem em disco.", erro);
        }
        return PREFIXO_URL + nome;
    }

    /* Confere a assinatura do arquivo em vez de confiar só no tipo informado. */
    private String extensaoPeloConteudo(byte[] b) {
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') return "gif";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }

    /* Aceita o caminho salvo ("/api/arquivos/x.png") ou a URL completa com ele. */
    private String validarCaminhoExistente(String imagem) {
        int inicio = imagem.indexOf(PREFIXO_URL);
        String nome = inicio < 0 ? null : imagem.substring(inicio + PREFIXO_URL.length());
        if (localizar(nome).isEmpty()) {
            throw new RegraDeNegocioException("Uma das imagens informadas não foi encontrada.");
        }
        return PREFIXO_URL + nome;
    }

    private List<String> lerLista(String imagensJson) {
        if (imagensJson == null || imagensJson.isBlank()) {
            return List.of();
        }
        try {
            List<String> lista = json.readValue(imagensJson, new TypeReference<List<String>>() { });
            return lista.stream().filter(item -> item != null && !item.isBlank()).toList();
        } catch (JacksonException erro) {
            /* Formato bem antigo: uma única imagem fora de lista. */
            return List.of(imagensJson.trim());
        }
    }

    private List<String> lerListaSemValidar(String imagensJson) {
        return lerLista(imagensJson).stream()
                .filter(caminho -> caminho.startsWith(PREFIXO_URL))
                .toList();
    }

    private void apagarArquivo(String caminho) {
        if (caminho == null || !caminho.startsWith(PREFIXO_URL)) return;
        localizar(caminho.substring(PREFIXO_URL.length())).ifPresent(arquivo -> {
            try {
                Files.deleteIfExists(arquivo);
            } catch (IOException erro) {
                log.warn("Não foi possível apagar {}: {}", arquivo, erro.getMessage());
            }
        });
    }

    private void aposConfirmarTransacao(List<String> caminhos) {
        if (caminhos.isEmpty()) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            caminhos.forEach(this::apagarArquivo);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                caminhos.forEach(ArmazenamentoImagemService.this::apagarArquivo);
            }
        });
    }

    private void aoDesfazerTransacao(List<String> caminhos) {
        if (caminhos.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    caminhos.forEach(ArmazenamentoImagemService.this::apagarArquivo);
                }
            }
        });
    }
}
