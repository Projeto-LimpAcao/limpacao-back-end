package com.limpacao.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.limpacao.enums.PerfilUsuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    static final String CLAIM_PERFIL = "perfil";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String gerarToken(String email, PerfilUsuario perfil) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_PERFIL, perfil.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extrairEmail(String token) {
        return lerClaims(token).getSubject();
    }

    public PerfilUsuario extrairPerfil(String token) {
        String perfil = lerClaims(token).get(CLAIM_PERFIL, String.class);
        if (perfil == null) {
            return PerfilUsuario.USUARIO;
        }
        try {
            return PerfilUsuario.valueOf(perfil);
        } catch (IllegalArgumentException e) {
            return PerfilUsuario.USUARIO;
        }
    }

    public boolean tokenValido(String token) {
        try {
            return !lerClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims lerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
