package com.devshowcase.api.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Permite configurar a conexão com o banco de dados através de uma única variável de ambiente
 * DATABASE_URL (no formato postgres://usuario:senha@host:porta/banco), assim como fazia a
 * implementação original em Node.js/Sequelize (src/config/database.js).
 *
 * Se DATABASE_URL estiver definida, ela é convertida para spring.datasource.url/username/password.
 * Caso contrário, os valores default/SPRING_DATASOURCE_* do application.yml continuam valendo.
 *
 * DB_SSL=true adiciona sslmode=require à connection string (equivalente ao dialectOptions.ssl
 * usado no Sequelize em produção).
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        try {
            URI uri = URI.create(databaseUrl);
            String userInfo = uri.getUserInfo();
            String username = null;
            String password = null;
            if (userInfo != null && userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                username = parts[0];
                password = parts[1];
            }

            boolean sslRequired = "true".equalsIgnoreCase(environment.getProperty("DB_SSL"));
            String query = uri.getQuery();

            StringBuilder queryParams = new StringBuilder();
            if (query != null && !query.isBlank()) {
                queryParams.append(query);
            }
            if (sslRequired) {
                if (queryParams.length() > 0) queryParams.append("&");
                queryParams.append("sslmode=require");
            }

            StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                    .append(uri.getHost())
                    .append(":")
                    .append(uri.getPort() == -1 ? 5432 : uri.getPort())
                    .append(uri.getPath());

            if (queryParams.length() > 0) {
                jdbcUrl.append("?").append(queryParams);
            }

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("spring.datasource.url", jdbcUrl.toString());
            if (username != null) props.put("spring.datasource.username", username);
            if (password != null) props.put("spring.datasource.password", password);

            environment.getPropertySources().addFirst(new MapPropertySource("databaseUrl", props));
        } catch (Exception ex) {
            // Se DATABASE_URL não puder ser interpretada, mantém a configuração default do application.yml.
        }
    }
}
