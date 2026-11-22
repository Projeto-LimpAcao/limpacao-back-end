package com.limpacao.health;

import java.time.Duration;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

public abstract class HealthIndicatorComCache implements HealthIndicator {

    private final long ttlMillis;
    private volatile Health ultimo;
    private volatile long ultimaVerificacao;

    protected HealthIndicatorComCache(Duration ttl) {
        this.ttlMillis = ttl.toMillis();
    }

    protected abstract Health verificar();

    @Override
    public Health health() {
        long agora = System.currentTimeMillis();
        if (ultimo == null || agora - ultimaVerificacao >= ttlMillis) {
            ultimo = verificar();
            ultimaVerificacao = agora;
        }
        return ultimo;
    }
}
