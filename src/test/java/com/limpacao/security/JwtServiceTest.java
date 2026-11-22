package com.limpacao.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.limpacao.enums.PerfilUsuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-pelo-menos-32-caracteres";

    private JwtService service;

    @BeforeEach
    void setUp() {
        service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", SEGREDO);
        ReflectionTestUtils.setField(service, "expiration", 60_000L);
    }

    @Test
    void tokenCarregaEmailEPerfil() {
        String token = service.gerarToken("admin@limpacao.com", PerfilUsuario.ADMIN);

        assertEquals("admin@limpacao.com", service.extrairEmail(token));
        assertEquals(PerfilUsuario.ADMIN, service.extrairPerfil(token));
    }

    @Test
    void tokenAntigoSemPerfilValeComoUsuario() {
        String tokenAntigo = Jwts.builder()
                .subject("ana@exemplo.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SEGREDO.getBytes()))
                .compact();

        assertEquals(PerfilUsuario.USUARIO, service.extrairPerfil(tokenAntigo));
    }
}
