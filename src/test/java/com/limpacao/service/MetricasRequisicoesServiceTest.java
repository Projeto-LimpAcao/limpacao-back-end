package com.limpacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.limpacao.dto.MetricasRequisicoesDTO;
import com.limpacao.dto.MetricasRequisicoesDTO.MetodoDTO;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class MetricasRequisicoesServiceTest {

    private SimpleMeterRegistry registry;
    private MetricasRequisicoesService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        service = new MetricasRequisicoesService(registry);
    }

    private void requisicoes(String metodo, String rota, String status, String resultado,
                             int quantidade, long ms) {
        Timer timer = Timer.builder(MetricasRequisicoesService.METRICA)
                .tag("method", metodo)
                .tag("uri", rota)
                .tag("status", status)
                .tag("outcome", resultado)
                .register(registry);
        for (int i = 0; i < quantidade; i++) {
            timer.record(Duration.ofMillis(ms));
        }
    }

    @Test
    void somaRequisicoesPorResultado() {
        requisicoes("GET", "/api/relatorios", "200", "SUCCESS", 6, 10);
        requisicoes("POST", "/api/relatorios", "400", "CLIENT_ERROR", 3, 20);
        requisicoes("GET", "/api/relatorios", "500", "SERVER_ERROR", 1, 50);

        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(10, resumo.total());
        assertEquals(6, resumo.sucesso());
        assertEquals(3, resumo.errosCliente());
        assertEquals(1, resumo.errosServidor());
        assertEquals(17, resumo.tempoMedioMs());
    }

    @Test
    void agrupaPorRotaEOrdenaPelasMaisAcessadas() {
        requisicoes("POST", "/api/auth/login", "200", "SUCCESS", 2, 30);
        requisicoes("GET", "/api/relatorios", "200", "SUCCESS", 4, 10);
        requisicoes("GET", "/api/relatorios", "500", "SERVER_ERROR", 1, 10);

        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(2, resumo.rotas().size());
        var maisAcessada = resumo.rotas().get(0);
        assertEquals("GET", maisAcessada.metodo());
        assertEquals("/api/relatorios", maisAcessada.rota());
        assertEquals(5, maisAcessada.total());
        assertEquals(1, maisAcessada.erros());
    }

    @Test
    void ignoraAsRequisicoesDoMonitoramento() {
        requisicoes("GET", "/actuator/health", "200", "SUCCESS", 50, 5);
        requisicoes("GET", "/api/metricas/requisicoes", "200", "SUCCESS", 50, 5);
        requisicoes("GET", "/api/relatorios", "200", "SUCCESS", 2, 10);

        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(2, resumo.total());
        assertEquals(1, resumo.rotas().size());
    }

    @Test
    void semRequisicoesDevolveZeros() {
        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(0, resumo.total());
        assertEquals(0, resumo.tempoMedioMs());
        assertEquals(0, resumo.rotas().size());
        assertEquals(List.of("GET", "POST", "PUT", "DELETE"),
                resumo.metodos().stream().map(MetodoDTO::metodo).toList());
    }

    @Test
    void contaCadaTipoDeAcao() {
        requisicoes("GET", "/api/relatorios", "200", "SUCCESS", 5, 10);
        requisicoes("POST", "/api/relatorios", "201", "SUCCESS", 2, 10);
        requisicoes("PUT", "/api/relatorios/{id}", "200", "SUCCESS", 3, 10);
        requisicoes("PATCH", "/api/relatorios/{id}/status", "400", "CLIENT_ERROR", 1, 10);
        requisicoes("DELETE", "/api/relatorios/{id}", "204", "SUCCESS", 4, 10);

        Map<String, MetodoDTO> metodos = resumo().metodos().stream()
                .collect(Collectors.toMap(MetodoDTO::metodo, Function.identity()));

        assertEquals(5, metodos.get("GET").total());
        assertEquals(2, metodos.get("POST").total());
        assertEquals(3, metodos.get("PUT").total());
        assertEquals(4, metodos.get("DELETE").total());
        assertEquals(false, metodos.containsKey("PATCH"));
    }

    @Test
    void ignoraOptionsDoNavegador() {
        requisicoes("OPTIONS", "/api/relatorios", "200", "SUCCESS", 30, 1);
        requisicoes("GET", "/api/relatorios", "200", "SUCCESS", 2, 10);

        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(2, resumo.total());
        assertEquals(4, resumo.metodos().size());
    }

    @Test
    void listaTodasAsRotasSemLimite() {
        for (int i = 1; i <= 12; i++) {
            requisicoes("GET", "/api/rota" + i, "200", "SUCCESS", i, 10);
        }

        assertEquals(12, service.resumo().rotas().size());
    }

    @Test
    void requisicoesSemRotaContamNoTotalMasNaoNaLista() {
        requisicoes("PATCH", "/**", "404", "CLIENT_ERROR", 2, 3);
        requisicoes("GET", "/api/relatorios", "200", "SUCCESS", 1, 3);

        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(3, resumo.total());
        assertEquals(1, resumo.rotas().size());
        assertEquals("/api/relatorios", resumo.rotas().get(0).rota());
    }

    @Test
    void ignoraRequisicoes401() {
        requisicoes("GET", "UNKNOWN", "401", "CLIENT_ERROR", 76, 3);
        requisicoes("GET", "/api/relatorios/{id}", "401", "CLIENT_ERROR", 4, 3);
        requisicoes("GET", "/api/relatorios", "200", "SUCCESS", 2, 3);

        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(2, resumo.total());
        assertEquals(0, resumo.errosCliente());
        assertEquals(1, resumo.rotas().size());
        assertEquals(2, resumo.metodos().get(0).total());
    }

    @Test
    void contaO401DoLoginMasIgnoraOsOutros() {
        requisicoes("POST", "/api/auth/login", "401", "CLIENT_ERROR", 3, 20);
        requisicoes("GET", "UNKNOWN", "401", "CLIENT_ERROR", 76, 3);
        requisicoes("GET", "/api/relatorios/meus", "401", "CLIENT_ERROR", 5, 3);

        MetricasRequisicoesDTO resumo = service.resumo();

        assertEquals(3, resumo.total());
        assertEquals(1, resumo.rotas().size());
        assertEquals("/api/auth/login", resumo.rotas().get(0).rota());
        assertEquals(3, resumo.rotas().get(0).erros());
    }

    private MetricasRequisicoesDTO resumo() {
        return service.resumo();
    }
}
