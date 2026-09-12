package com.devshowcase.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI devshowcaseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevShowcase API")
                        .version("1.0.0")
                        .description("API para desenvolvedores exibirem perfis, projetos, tecnologias e receberem feedback (notas e comentários) da comunidade."));
    }
}
