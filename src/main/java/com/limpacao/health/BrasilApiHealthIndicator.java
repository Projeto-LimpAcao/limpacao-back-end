package com.limpacao.health;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BrasilApiHealthIndicator extends HealthIndicatorComCache {

    static final String URL = "https://brasilapi.com.br/api/cep/v1/01001000";
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final Duration CACHE = Duration.ofSeconds(30);

    private final RestClient restClient;

    @Autowired
    public BrasilApiHealthIndicator() {
        super(CACHE);
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT);
        factory.setReadTimeout(TIMEOUT);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    BrasilApiHealthIndicator(RestClient restClient) {
        super(Duration.ZERO);
        this.restClient = restClient;
    }

    @Override
    protected Health verificar() {
        long inicio = System.currentTimeMillis();
        try {
            Map<?, ?> resposta = restClient.get().uri(URL).retrieve().body(Map.class);
            long tempo = System.currentTimeMillis() - inicio;

            boolean dadosCorretos = resposta != null
                    && "SP".equals(resposta.get("state"))
                    && "São Paulo".equals(resposta.get("city"));

            Health.Builder builder = dadosCorretos ? Health.up() : Health.status("DEGRADED");
            return builder
                    .withDetail("API", URL)
                    .withDetail("Tempo de Resposta (ms)", tempo)
                    .withDetail("Dados", dadosCorretos ? "OK" : "Resposta diferente do esperado")
                    .build();
        } catch (Exception e) {
            long tempo = System.currentTimeMillis() - inicio;
            log.warn("BrasilAPI indisponível no health check: {}", e.getMessage());
            return Health.status("DEGRADED")
                    .withDetail("API", URL)
                    .withDetail("Tempo de Resposta (ms)", tempo)
                    .withDetail("Dados", "API indisponível")
                    .withDetail("Motivo", motivo(e))
                    .build();
        }
    }

    static String motivo(Exception e) {
        if (e instanceof HttpStatusCodeException http) {
            int status = http.getStatusCode().value();
            if (status == 429) {
                return "A BrasilAPI recusou por excesso de consultas (HTTP 429). Tente mais tarde.";
            }
            if (status >= 500) {
                return "A BrasilAPI está com problema no servidor dela (HTTP " + status + ").";
            }
            return "A BrasilAPI recusou a consulta (HTTP " + status + ").";
        }

        String mensagem = String.valueOf(e.getMessage()).toLowerCase(Locale.ROOT);
        Throwable causa = e.getCause();
        if (causa instanceof UnknownHostException || mensagem.contains("unknownhost")) {
            return "Não foi possível encontrar a BrasilAPI. Verifique a conexão com a internet.";
        }
        if (mensagem.contains("connect timed out")) {
            return "A BrasilAPI não respondeu a tempo (mais de " + TIMEOUT.toSeconds() + " s para conectar).";
        }
        if (mensagem.contains("read timed out") || causa instanceof SocketTimeoutException) {
            return "A BrasilAPI conectou, mas demorou demais para responder (mais de "
                    + TIMEOUT.toSeconds() + " s).";
        }
        if (causa instanceof ConnectException || mensagem.contains("connection refused")) {
            return "A BrasilAPI recusou a conexão.";
        }
        return "Não foi possível consultar a BrasilAPI.";
    }
}
