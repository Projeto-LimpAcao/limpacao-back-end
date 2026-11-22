package com.limpacao.health;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.limpacao.enums.PerfilUsuario;
import com.limpacao.security.JwtService;

@SpringBootTest(properties = {
        "spring.flyway.locations=classpath:db/migration",
        "management.endpoint.health.cache.time-to-live=0s",
        "limpacao.admin.email="
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private BrasilApiHealthIndicator brasilApi;

    private String tokenAdmin;
    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        simularBrasilApi(Health.up().build());
        tokenAdmin = "Bearer " + jwtService.gerarToken("admin@limpacao.com", PerfilUsuario.ADMIN);
        tokenUsuario = "Bearer " + jwtService.gerarToken("ana@exemplo.com", PerfilUsuario.USUARIO);
    }

    private void simularBrasilApi(Health health) {
        when(brasilApi.health()).thenReturn(health);
        when(brasilApi.health(anyBoolean())).thenReturn(health);
    }

    @Test
    void publicoVeSoOStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void usuarioComumNaoVeOsComponentes() throws Exception {
        mockMvc.perform(get("/actuator/health").header("Authorization", tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void adminVeTodosOsComponentes() throws Exception {
        mockMvc.perform(get("/actuator/health").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.diskSpace.status").value("UP"))
                .andExpect(jsonPath("$.components.brasilApi.status").value("UP"))
                .andExpect(jsonPath("$.components.ssl").doesNotExist());
    }

    @Test
    void brasilApiDegradadaContinuaRespondendo200() throws Exception {
        simularBrasilApi(Health.status("DEGRADED").build());

        mockMvc.perform(get("/actuator/health").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"));
    }

    @Test
    void readinessDependeSoDoBanco() throws Exception {
        simularBrasilApi(Health.down().build());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isServiceUnavailable());
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void livenessResponde() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void emailDaListaViraAdminAoSeCadastrar() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Equipe\",\"email\":\"limpacao.projeto@gmail.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bia\",\"email\":\"bia@exemplo.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("USUARIO"));
    }

    @Test
    void metricasSoParaAdmin() throws Exception {
        mockMvc.perform(get("/api/metricas/requisicoes"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/metricas/requisicoes").header("Authorization", tokenUsuario))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/metricas/requisicoes").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists());
    }
}
