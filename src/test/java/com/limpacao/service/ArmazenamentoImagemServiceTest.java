package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.limpacao.exception.RegraDeNegocioException;

class ArmazenamentoImagemServiceTest {

    /* PNG de 1x1 pixel. */
    private static final String PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @TempDir
    Path pasta;

    private ArmazenamentoImagemService service;

    @BeforeEach
    void setUp() {
        service = new ArmazenamentoImagemService(pasta.toString());
    }

    @Test
    void gravaImagemEmDiscoEDevolveCaminho() throws Exception {
        String resultado = service.salvarLista("[\"data:image/png;base64," + PNG_BASE64 + "\"]");

        String caminho = resultado.substring(2, resultado.length() - 2);
        assertTrue(caminho.startsWith("/api/arquivos/"));
        assertTrue(caminho.endsWith(".png"));
        String nome = caminho.substring("/api/arquivos/".length());
        assertTrue(service.localizar(nome).isPresent());
        assertEquals(Base64.getDecoder().decode(PNG_BASE64).length, Files.size(pasta.resolve(nome)));
    }

    @Test
    void mantemImagemJaSalvaInclusiveComUrlCompleta() {
        String salvo = service.salvarLista("[\"data:image/png;base64," + PNG_BASE64 + "\"]");
        String caminho = salvo.substring(2, salvo.length() - 2);

        String resultado = service.salvarLista("[\"http://localhost:5173" + caminho + "\"]");

        assertEquals("[\"" + caminho + "\"]", resultado);
    }

    @Test
    void rejeitaArquivoQueNaoEImagem() {
        String texto = Base64.getEncoder().encodeToString("nao sou imagem".getBytes());

        assertThrows(RegraDeNegocioException.class,
                () -> service.salvarLista("[\"data:image/png;base64," + texto + "\"]"));
    }

    @Test
    void rejeitaCaminhoDesconhecido() {
        assertThrows(RegraDeNegocioException.class,
                () -> service.salvarLista("[\"/api/arquivos/../../application.properties\"]"));
    }

    @Test
    void naoGravaNadaQuandoUmaDasImagensEInvalida() throws Exception {
        String invalida = Base64.getEncoder().encodeToString("x".getBytes());

        assertThrows(RegraDeNegocioException.class, () -> service.salvarLista(
                "[\"data:image/png;base64," + PNG_BASE64 + "\",\"data:image/png;base64," + invalida + "\"]"));

        try (var arquivos = Files.list(pasta)) {
            assertEquals(0, arquivos.count());
        }
    }

    @Test
    void apagaSomenteImagensRemovidas() {
        String primeira = service.salvarLista("[\"data:image/png;base64," + PNG_BASE64 + "\"]");
        String segunda = service.salvarLista("[\"data:image/png;base64," + PNG_BASE64 + "\"]");
        String nomePrimeira = primeira.substring(2 + "/api/arquivos/".length(), primeira.length() - 2);
        String nomeSegunda = segunda.substring(2 + "/api/arquivos/".length(), segunda.length() - 2);

        String antigas = "[\"/api/arquivos/" + nomePrimeira + "\",\"/api/arquivos/" + nomeSegunda + "\"]";
        service.apagarRemovidas(antigas, segunda);

        assertFalse(service.localizar(nomePrimeira).isPresent());
        assertTrue(service.localizar(nomeSegunda).isPresent());
    }

    @Test
    void dentroDeTransacaoSoApagaDepoisDoCommit() {
        String salvo = service.salvarLista("[\"data:image/png;base64," + PNG_BASE64 + "\"]");
        String nome = salvo.substring(2 + "/api/arquivos/".length(), salvo.length() - 2);

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.apagarTodas(salvo);
            assertTrue(service.localizar(nome).isPresent());

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        assertFalse(service.localizar(nome).isPresent());
    }

    @Test
    void localizarRecusaNomesForaDoPadrao() {
        assertTrue(service.localizar("../segredo.png").isEmpty());
        assertTrue(service.localizar("arquivo.exe").isEmpty());
    }
}
