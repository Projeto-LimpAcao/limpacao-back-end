package com.limpacao.exception;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroRespostaDTO> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso não encontrado [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroRespostaDTO> handleNoSuchElement(
            NoSuchElementException ex, HttpServletRequest request) {
        log.warn("Elemento não encontrado [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroRespostaDTO> handleRegraDeNegocio(
            RegraDeNegocioException ex, HttpServletRequest request) {
        log.warn("Violação de regra de negócio [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroRespostaDTO> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argumento inválido [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroRespostaDTO> handleConflito(
            ConflitoException ex, HttpServletRequest request) {
        log.warn("Conflito de dados [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroRespostaDTO> handleCredenciaisInvalidas(
            CredenciaisInvalidasException ex, HttpServletRequest request) {
        log.warn("Falha de autenticação [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroRespostaDTO> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Erro de autenticação [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Credenciais inválidas ou sessão expirada.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErroRespostaDTO> handleAccessDenied(
            Exception ex, HttpServletRequest request) {
        log.warn("Acesso negado [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage() != null && !ex.getMessage().isBlank()
                        ? ex.getMessage()
                        : "Acesso não autorizado para este recurso.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroRespostaDTO> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        int statusCode = ex.getStatusCode().value();
        String mensagem = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        log.warn("ResponseStatusException [{}] status {}: {}", request.getRequestURI(), statusCode, mensagem);
        var erro = new ErroRespostaDTO(
                statusCode,
                HttpStatus.valueOf(statusCode).getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<CampoErroDTO> validacoes = ex.getBindingResult().getFieldErrors().stream()
                .map(field -> new CampoErroDTO(field.getField(), field.getDefaultMessage()))
                .toList();

        String mensagem = validacoes.isEmpty()
                ? "Erro de validação nos campos da requisição."
                : validacoes.get(0).campo() + ": " + validacoes.get(0).mensagem();

        log.warn("Validação falhou [{}]: {}", request.getRequestURI(), validacoes);

        var erro = new ErroRespostaDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação",
                mensagem,
                request.getRequestURI(),
                validacoes
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroRespostaDTO> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Corpo da requisição ilegível [{}]: {}", request.getRequestURI(), ex.getMessage());
        var erro = new ErroRespostaDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Corpo da requisição ausente ou em formato inválido.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroRespostaDTO> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensagem = String.format("O parâmetro '%s' recebeu um valor inválido.", ex.getName());
        log.warn("Tipo de argumento incompatível [{}]: {}", request.getRequestURI(), mensagem);
        var erro = new ErroRespostaDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroRespostaDTO> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Método HTTP não suportado [{}]: {}", request.getRequestURI(), ex.getMethod());
        var erro = new ErroRespostaDTO(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                String.format("Método HTTP '%s' não é suportado para este endpoint.", ex.getMethod()),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroRespostaDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Erro interno não tratado [{}]: ", request.getRequestURI(), ex);
        var erro = new ErroRespostaDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocorreu um erro interno inesperado no servidor.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
