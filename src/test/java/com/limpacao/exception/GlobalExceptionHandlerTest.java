package com.limpacao.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/teste");
    }

    @Test
    void deveTratarRecursoNaoEncontrado() {
        var ex = new RecursoNaoEncontradoException("Item não encontrado");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleRecursoNaoEncontrado(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(404, resposta.getBody().status());
        assertEquals("Item não encontrado", resposta.getBody().mensagem());
        assertEquals("/api/teste", resposta.getBody().caminho());
    }

    @Test
    void deveTratarNoSuchElement() {
        var ex = new NoSuchElementException("Elemento ausente");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleNoSuchElement(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(404, resposta.getBody().status());
        assertEquals("Elemento ausente", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarRegraDeNegocio() {
        var ex = new RegraDeNegocioException("Dado inconsistente");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleRegraDeNegocio(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().status());
        assertEquals("Dado inconsistente", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarIllegalArgument() {
        var ex = new IllegalArgumentException("Parâmetro inválido");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().status());
        assertEquals("Parâmetro inválido", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarConflito() {
        var ex = new ConflitoException("E-mail já cadastrado");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleConflito(ex, request);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(409, resposta.getBody().status());
        assertEquals("E-mail já cadastrado", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarCredenciaisInvalidas() {
        var ex = new CredenciaisInvalidasException("E-mail ou senha inválidos.");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleCredenciaisInvalidas(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(401, resposta.getBody().status());
        assertEquals("E-mail ou senha inválidos.", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarAuthenticationException() {
        var ex = new BadCredentialsException("Falha de autenticação");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleAuthentication(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(401, resposta.getBody().status());
        assertEquals("Credenciais inválidas ou sessão expirada.", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarAccessDeniedException() {
        var ex = new AccessDeniedException("Não autorizado");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(403, resposta.getBody().status());
        assertEquals("Não autorizado", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarResponseStatusException() {
        var ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Falha ao processar");
        ResponseEntity<ErroRespostaDTO> resposta = handler.handleResponseStatus(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(422, resposta.getBody().status());
        assertEquals("Falha ao processar", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("usuario", "email", "O e-mail é obrigatório.");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErroRespostaDTO> resposta = handler.handleMethodArgumentNotValid(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().status());
        assertNotNull(resposta.getBody().validacoes());
        assertFalse(resposta.getBody().validacoes().isEmpty());
        assertEquals("email", resposta.getBody().validacoes().get(0).campo());
        assertEquals("O e-mail é obrigatório.", resposta.getBody().validacoes().get(0).mensagem());
    }

    @Test
    void deveTratarHttpMessageNotReadable() {
        var ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("JSON parse error");

        ResponseEntity<ErroRespostaDTO> resposta = handler.handleHttpMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().status());
        assertEquals("Corpo da requisição ausente ou em formato inválido.", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarMethodArgumentTypeMismatch() {
        var ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<ErroRespostaDTO> resposta = handler.handleMethodArgumentTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().status());
        assertEquals("O parâmetro 'id' recebeu um valor inválido.", resposta.getBody().mensagem());
    }

    @Test
    void deveTratarHttpRequestMethodNotSupported() {
        var ex = new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<ErroRespostaDTO> resposta = handler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(405, resposta.getBody().status());
    }

    @Test
    void deveTratarErroInesperadoGenerico() {
        var ex = new RuntimeException("Erro inesperado no banco");

        ResponseEntity<ErroRespostaDTO> resposta = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(500, resposta.getBody().status());
        assertEquals("Ocorreu um erro interno inesperado no servidor.", resposta.getBody().mensagem());
    }
}
