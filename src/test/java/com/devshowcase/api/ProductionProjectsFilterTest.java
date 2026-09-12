package com.devshowcase.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Teste de integração contra a API publicada em produção (Render).
 * Desabilitado por padrão pois depende de rede e de dados já existentes no
 * ambiente; remova o @Disabled para rodar manualmente contra produção.
 */
@Disabled("Depende da API de produção estar no ar; rode manualmente removendo esta anotação")
class ProductionProjectsFilterTest {

    private static final String API_BASE_URL = "https://devshowcase-api-nldk.onrender.com";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void filtraProjetosPorUsuarioNaProducao() {
        JsonNode allProjects = fetchProjects(API_BASE_URL + "/api/projects?page=1&limit=100");
        JsonNode data = allProjects.get("data");
        assumeTrue(data != null && data.size() > 0, "Não há projetos cadastrados em produção para testar o filtro");

        long profileId = data.get(0).get("profile").get("id").asLong();

        JsonNode filtered = fetchProjects(API_BASE_URL + "/api/projects?profileId=" + profileId + "&page=1&limit=100");
        JsonNode filteredData = filtered.get("data");

        assertTrue(filteredData.size() > 0, "O filtro por profileId deveria retornar ao menos um projeto");
        filteredData.forEach(project -> assertEquals(profileId, project.get("profile").get("id").asLong(),
                "Todos os projetos retornados devem pertencer ao usuário filtrado"));
    }

    private JsonNode fetchProjects(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
