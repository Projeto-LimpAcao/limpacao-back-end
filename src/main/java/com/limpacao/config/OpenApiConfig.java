package com.limpacao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/*
 * Documentação da API (Swagger).
 * Interface: /swagger-ui.html  |  JSON OpenAPI: /v3/api-docs
 * Para testar rotas protegidas: faça login em /api/auth/login, copie o token
 * e cole no botão "Authorize".
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI limpacaoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LimpAção API")
                        .description("API da plataforma comunitária LimpAção: reportes, ofertas de ajuda, "
                                + "participações, chat e perfil de usuário.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
