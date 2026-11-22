package com.limpacao.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.limpacao.dto.MetricasRequisicoesDTO;
import com.limpacao.dto.MetricasRequisicoesDTO.MetodoDTO;
import com.limpacao.dto.MetricasRequisicoesDTO.RotaDTO;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class MetricasRequisicoesService {

    static final String METRICA = "http.server.requests";
    private static final String ROTA_LOGIN = "/api/auth/login";

    private static final List<String> METODOS_PRINCIPAIS = List.of("GET", "POST", "PUT", "DELETE");

    private final MeterRegistry registry;

    public MetricasRequisicoesService(MeterRegistry registry) {
        this.registry = registry;
    }

    public MetricasRequisicoesDTO resumo() {
        long total = 0;
        long sucesso = 0;
        long errosCliente = 0;
        long errosServidor = 0;
        double tempoTotalMs = 0;

        Map<String, Acumulado> porMetodo = new LinkedHashMap<>();
        METODOS_PRINCIPAIS.forEach(metodo -> porMetodo.put(metodo, new Acumulado(metodo, "")));
        Map<String, Acumulado> porRota = new LinkedHashMap<>();

        for (Timer timer : registry.find(METRICA).timers()) {
            String rota = tag(timer, "uri");
            String metodo = tag(timer, "method");
            if (ehMonitoramento(rota) || "OPTIONS".equals(metodo) || ehIgnorado401(rota, tag(timer, "status"))) {
                continue;
            }

            long quantidade = timer.count();
            double tempoMs = timer.totalTime(TimeUnit.MILLISECONDS);
            String resultado = tag(timer, "outcome");
            long erros = "CLIENT_ERROR".equals(resultado) || "SERVER_ERROR".equals(resultado) ? quantidade : 0;

            total += quantidade;
            tempoTotalMs += tempoMs;
            switch (resultado) {
                case "SUCCESS", "REDIRECTION", "INFORMATIONAL" -> sucesso += quantidade;
                case "CLIENT_ERROR" -> errosCliente += quantidade;
                case "SERVER_ERROR" -> errosServidor += quantidade;
                default -> { }
            }

            Acumulado doMetodo = porMetodo.get(metodo);
            if (doMetodo != null) {
                doMetodo.somar(quantidade, erros, tempoMs);
            }

            if (!ehSemRota(rota)) {
                porRota.computeIfAbsent(metodo + " " + rota, chave -> new Acumulado(metodo, rota))
                        .somar(quantidade, erros, tempoMs);
            }
        }

        List<MetodoDTO> metodos = porMetodo.values().stream()
                .map(acumulado -> new MetodoDTO(acumulado.metodo, acumulado.total, acumulado.erros))
                .toList();

        List<RotaDTO> rotas = porRota.values().stream()
                .sorted(Comparator.comparingLong(Acumulado::total).reversed())
                .map(Acumulado::paraRota)
                .toList();

        return new MetricasRequisicoesDTO(
                total, sucesso, errosCliente, errosServidor, media(tempoTotalMs, total), metodos, rotas);
    }

    private static boolean ehIgnorado401(String rota, String status) {
        return "401".equals(status) && !ROTA_LOGIN.equals(rota);
    }

    private static boolean ehMonitoramento(String rota) {
        return rota.startsWith("/actuator") || rota.startsWith("/api/metricas");
    }

    private static boolean ehSemRota(String rota) {
        return rota.isEmpty() || "UNKNOWN".equals(rota) || "/**".equals(rota);
    }

    private static String tag(Timer timer, String nome) {
        String valor = timer.getId().getTag(nome);
        return valor == null ? "" : valor;
    }

    private static long media(double tempoTotalMs, long quantidade) {
        return quantidade == 0 ? 0 : Math.round(tempoTotalMs / quantidade);
    }

    private static final class Acumulado {
        private final String metodo;
        private final String rota;
        private long total;
        private long erros;
        private double tempoMs;

        Acumulado(String metodo, String rota) {
            this.metodo = metodo;
            this.rota = rota;
        }

        void somar(long quantidade, long errosDaVez, double tempoDaVez) {
            total += quantidade;
            erros += errosDaVez;
            tempoMs += tempoDaVez;
        }

        long total() {
            return total;
        }

        RotaDTO paraRota() {
            return new RotaDTO(metodo, rota, total, erros, media(tempoMs, total));
        }
    }
}
