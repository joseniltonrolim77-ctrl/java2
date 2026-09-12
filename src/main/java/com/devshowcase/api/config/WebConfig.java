package com.devshowcase.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // spring.web.resources.add-mappings=false desliga o serving automático de
        // estáticos (para não mascarar rotas de API inexistentes com um 404 genérico
        // do Spring). Aqui expomos explicitamente apenas o formulário de cadastro.
        registry.addResourceHandler("/cadastro-usuario.html")
                .addResourceLocations("classpath:/static/");
    }
}
