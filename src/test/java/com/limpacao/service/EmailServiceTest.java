package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

class EmailServiceTest {

    private RestClient.Builder builder;
    private MockRestServiceServer servidor;

    @BeforeEach
    void configurar() {
        builder = RestClient.builder().baseUrl("https://api.brevo.com/v3");
        servidor = MockRestServiceServer.bindTo(builder).build();
    }

    @Test
    void enviaCodigoPelaApiDoBrevo() {
        servidor.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "chave-teste"))
                .andExpect(jsonPath("$.sender.email").value("nao-responda@limpacao.org"))
                .andExpect(jsonPath("$.sender.name").value("LimpAção"))
                .andExpect(jsonPath("$.to[0].email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.subject").value("Código para redefinir sua senha"))
                .andRespond(withStatus(HttpStatus.CREATED));

        new EmailService(builder, "chave-teste", "nao-responda@limpacao.org", "LimpAção")
                .enviarCodigoRecuperacaoSenha("ana@exemplo.com", "Ana", "123456", 15);

        servidor.verify();
    }

    @Test
    void retorna503QuandoBrevoRecusa() {
        servidor.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withUnauthorizedRequest());

        var service = new EmailService(builder, "chave-invalida", "nao-responda@limpacao.org", "LimpAção");
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.enviarCodigoRecuperacaoSenha("ana@exemplo.com", "Ana", "123456", 15));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }

    @Test
    void retorna503SemChaveConfigurada() {
        var service = new EmailService(builder, "", "nao-responda@limpacao.org", "LimpAção");
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.enviarCodigoRecuperacaoSenha("ana@exemplo.com", "Ana", "123456", 15));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        servidor.verify();
    }
}