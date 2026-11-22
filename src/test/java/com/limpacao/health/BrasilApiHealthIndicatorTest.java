package com.limpacao.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

class BrasilApiHealthIndicatorTest {

    private RestClient.Builder builder;
    private MockRestServiceServer servidor;

    @BeforeEach
    void configurar() {
        builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
    }

    @Test
    void retornaUpQuandoRespondeComOsDadosCertos() {
        servidor.expect(requestTo(BrasilApiHealthIndicator.URL))
                .andRespond(withSuccess(
                        "{\"cep\":\"01001000\",\"state\":\"SP\",\"city\":\"São Paulo\"}",
                        MediaType.APPLICATION_JSON));

        Health health = new BrasilApiHealthIndicator(builder.build()).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("OK", health.getDetails().get("Dados"));
        assertTrue(health.getDetails().containsKey("Tempo de Resposta (ms)"));
    }

    @Test
    void retornaDegradedQuandoOsDadosVemDiferentes() {
        servidor.expect(requestTo(BrasilApiHealthIndicator.URL))
                .andRespond(withSuccess(
                        "{\"cep\":\"01001000\",\"state\":\"RJ\",\"city\":\"São Paulo\"}",
                        MediaType.APPLICATION_JSON));

        Health health = new BrasilApiHealthIndicator(builder.build()).health();

        assertEquals("DEGRADED", health.getStatus().getCode());
        assertEquals("Resposta diferente do esperado", health.getDetails().get("Dados"));
    }

    @Test
    void retornaDegradedQuandoApiFalha() {
        servidor.expect(requestTo(BrasilApiHealthIndicator.URL)).andRespond(withServerError());

        Health health = new BrasilApiHealthIndicator(builder.build()).health();

        assertEquals("DEGRADED", health.getStatus().getCode());
        assertEquals("API indisponível", health.getDetails().get("Dados"));
        assertEquals("A BrasilAPI está com problema no servidor dela (HTTP 500).",
                health.getDetails().get("Motivo"));
        assertFalse(health.getDetails().containsKey("error"));
    }

    @Test
    void traduzTempoEsgotadoESemInternet() {
        var conexaoLenta = new ResourceAccessException(
                "I/O error on GET request: Connect timed out", new SocketTimeoutException("Connect timed out"));
        var leituraLenta = new ResourceAccessException(
                "I/O error on GET request: Read timed out", new SocketTimeoutException("Read timed out"));
        var semInternet = new ResourceAccessException(
                "I/O error on GET request: brasilapi.com.br", new UnknownHostException("brasilapi.com.br"));

        assertEquals("A BrasilAPI não respondeu a tempo (mais de 3 s para conectar).",
                BrasilApiHealthIndicator.motivo(conexaoLenta));
        assertEquals("A BrasilAPI conectou, mas demorou demais para responder (mais de 3 s).",
                BrasilApiHealthIndicator.motivo(leituraLenta));
        assertEquals("Não foi possível encontrar a BrasilAPI. Verifique a conexão com a internet.",
                BrasilApiHealthIndicator.motivo(semInternet));
    }

    @Test
    void cacheEvitaNovaChamadaDentroDoTempoDeVida() {
        servidor.expect(once(), requestTo(BrasilApiHealthIndicator.URL))
                .andRespond(withSuccess(
                        "{\"cep\":\"01001000\",\"state\":\"SP\",\"city\":\"São Paulo\"}",
                        MediaType.APPLICATION_JSON));
        BrasilApiHealthIndicator real = new BrasilApiHealthIndicator(builder.build());
        HealthIndicatorComCache comCache = new HealthIndicatorComCache(Duration.ofMinutes(1)) {
            @Override
            protected Health verificar() {
                return real.verificar();
            }
        };

        Health primeira = comCache.health();
        Health segunda = comCache.health();

        assertEquals(primeira, segunda);
        servidor.verify();
    }
}
