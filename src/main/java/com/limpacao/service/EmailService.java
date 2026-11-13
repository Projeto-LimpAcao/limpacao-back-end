package com.limpacao.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class EmailService {

    private static final String MENSAGEM_FALHA =
            "Não foi possível enviar o e-mail de recuperação. Tente novamente mais tarde.";

    private final RestClient restClient;
    private final String apiKey;
    private final String remetente;
    private final String nomeRemetente;

    @Autowired
    public EmailService(
            @Value("${limpacao.mail.brevo.url:https://api.brevo.com/v3}") String urlBrevo,
            @Value("${limpacao.mail.brevo.api-key:}") String apiKey,
            @Value("${limpacao.mail.remetente:}") String remetente,
            @Value("${limpacao.mail.nome-remetente:LimpAção}") String nomeRemetente) {
        this(RestClient.builder().baseUrl(urlBrevo), apiKey, remetente, nomeRemetente);
    }

    EmailService(RestClient.Builder builder, String apiKey, String remetente, String nomeRemetente) {
        this.restClient = builder.build();
        this.apiKey = apiKey;
        this.remetente = remetente;
        this.nomeRemetente = nomeRemetente;
    }

    public void enviarCodigoRecuperacaoSenha(String destinatario, String nome, String codigo, int validadeMinutos) {
        String texto = String.format("""
                Olá, %s!

                Recebemos uma solicitação para redefinir a senha da sua conta no %s.

                Seu código de verificação é: %s

                Ele é válido por %d minutos. Se você não fez essa solicitação, ignore este e-mail; sua senha continuará a mesma.
                """, nome, nomeRemetente, codigo, validadeMinutos);

        enviar(destinatario, nome, "Código para redefinir sua senha", texto);
    }

    private void enviar(String destinatario, String nomeDestinatario, String assunto, String texto) {
        if (apiKey == null || apiKey.isBlank() || remetente == null || remetente.isBlank()) {
            log.error("Envio de e-mail não configurado: defina BREVO_API_KEY e MAIL_REMETENTE.");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, MENSAGEM_FALHA);
        }

        Map<String, Object> corpo = Map.of(
                "sender", Map.of("name", nomeRemetente, "email", remetente),
                "to", List.of(Map.of("email", destinatario, "name", nomeDestinatario == null ? destinatario : nomeDestinatario)),
                "subject", assunto,
                "textContent", texto);

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Falha ao enviar e-mail pelo Brevo para {}", destinatario, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, MENSAGEM_FALHA);
        }
    }
}