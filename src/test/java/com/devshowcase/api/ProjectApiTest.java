package com.devshowcase.api;

import com.devshowcase.api.repository.FeedbackRepository;
import com.devshowcase.api.repository.ProfileRepository;
import com.devshowcase.api.repository.ProjectRepository;
import com.devshowcase.api.repository.TechnologyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TechnologyRepository technologyRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void cleanDatabase() {
        feedbackRepository.deleteAll();
        projectRepository.deleteAll();
        technologyRepository.deleteAll();
        profileRepository.deleteAll();
    }

    private Map<?, ?> createProfile(String email) throws Exception {
        Map<String, Object> body = Map.of("name", "Ana Souza", "email", email);
        MvcResult res = mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(), Map.class);
    }

    private Map<?, ?> createTechnology(String name) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/technologies").contentType("application/json").content(objectMapper.writeValueAsString(Map.of("name", name))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(), Map.class);
    }

    private MvcResult createProject(Map<String, Object> body) throws Exception {
        return mockMvc.perform(post("/api/projects").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    @Test
    void criaProjetoComDadosValidosETecnologias() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        Map<?, ?> tech = createTechnology("Node.js");

        Map<String, Object> body = new HashMap<>();
        body.put("title", "DevShowcase API");
        body.put("description", "Backend do projeto");
        body.put("repositoryUrl", "https://github.com/ana/devshowcase");
        body.put("profileId", profile.get("id"));
        body.put("technologyIds", List.of(tech.get("id")));

        mockMvc.perform(post("/api/projects").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("DevShowcase API"))
                .andExpect(jsonPath("$.profile.id").value(((Number) profile.get("id")).intValue()))
                .andExpect(jsonPath("$.technologies.length()").value(1))
                .andExpect(jsonPath("$.technologies[0].name").value("Node.js"));
    }

    @Test
    void rejeitaTituloVazio() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        Map<String, Object> body = Map.of(
                "title", "",
                "repositoryUrl", "https://github.com/ana/devshowcase",
                "profileId", profile.get("id"));

        mockMvc.perform(post("/api/projects").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaRepositoryUrlInvalida() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        Map<String, Object> body = Map.of(
                "title", "Projeto X",
                "repositoryUrl", "nao-e-url",
                "profileId", profile.get("id"));

        mockMvc.perform(post("/api/projects").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaProfileIdInexistente() throws Exception {
        Map<String, Object> body = Map.of(
                "title", "Projeto X",
                "repositoryUrl", "https://github.com/ana/devshowcase",
                "profileId", 999999);

        mockMvc.perform(post("/api/projects").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaTechnologyIdsInexistentes() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Projeto X");
        body.put("repositoryUrl", "https://github.com/ana/devshowcase");
        body.put("profileId", profile.get("id"));
        body.put("technologyIds", List.of(999999));

        mockMvc.perform(post("/api/projects").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaTodosOsProjetosPaginados() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        createProject(Map.of("title", "Projeto 1", "repositoryUrl", "https://github.com/ana/projeto1", "profileId", profile.get("id")));
        createProject(Map.of("title", "Projeto 2", "repositoryUrl", "https://github.com/ana/projeto2", "profileId", profile.get("id")));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(10))
                .andExpect(jsonPath("$.pagination.total").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    void filtraProjetosPorProfileId() throws Exception {
        Map<?, ?> profile1 = createProfile("ana1@example.com");
        Map<?, ?> profile2 = createProfile("ana2@example.com");

        createProject(Map.of("title", "Projeto do perfil 1", "repositoryUrl", "https://github.com/ana/p1", "profileId", profile1.get("id")));
        createProject(Map.of("title", "Projeto do perfil 2", "repositoryUrl", "https://github.com/ana/p2", "profileId", profile2.get("id")));

        mockMvc.perform(get("/api/projects").param("profileId", String.valueOf(profile1.get("id"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Projeto do perfil 1"));
    }

    @Test
    void filtraProjetosPorTecnologia() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        Map<?, ?> node = createTechnology("Node.js");
        Map<?, ?> react = createTechnology("React");

        Map<String, Object> p1 = new HashMap<>();
        p1.put("title", "API em Node");
        p1.put("repositoryUrl", "https://github.com/ana/api-node");
        p1.put("profileId", profile.get("id"));
        p1.put("technologyIds", List.of(node.get("id")));
        createProject(p1);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("title", "Front em React");
        p2.put("repositoryUrl", "https://github.com/ana/front-react");
        p2.put("profileId", profile.get("id"));
        p2.put("technologyIds", List.of(react.get("id")));
        createProject(p2);

        mockMvc.perform(get("/api/projects").param("technology", "Node"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("API em Node"));
    }

    @Test
    void paginaOsResultadosComPageELimit() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        for (int i = 1; i <= 3; i++) {
            createProject(Map.of("title", "Projeto " + i, "repositoryUrl", "https://github.com/ana/projeto" + i, "profileId", profile.get("id")));
        }

        mockMvc.perform(get("/api/projects").param("page", "1").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(2))
                .andExpect(jsonPath("$.pagination.total").value(3))
                .andExpect(jsonPath("$.pagination.totalPages").value(2));
    }

    @Test
    void cadastraFeedbackEAtualizaANotaMediaDoProjeto() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        MvcResult projectRes = createProject(Map.of("title", "Projeto com feedback", "repositoryUrl", "https://github.com/ana/projeto-feedback", "profileId", profile.get("id")));
        Map<?, ?> project = objectMapper.readValue(projectRes.getResponse().getContentAsString(), Map.class);

        mockMvc.perform(post("/api/projects/{id}/feedbacks", project.get("id"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("rating", 5, "comment", "Excelente!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectAverageRating").value(5.0));

        mockMvc.perform(post("/api/projects/{id}/feedbacks", project.get("id"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("rating", 3, "comment", "Bom, mas pode melhorar."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectAverageRating").value(4.0));
    }

    @Test
    void rejeitaRatingForaDoIntervalo() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        MvcResult projectRes = createProject(Map.of("title", "Projeto X", "repositoryUrl", "https://github.com/ana/projeto-x", "profileId", profile.get("id")));
        Map<?, ?> project = objectMapper.readValue(projectRes.getResponse().getContentAsString(), Map.class);

        mockMvc.perform(post("/api/projects/{id}/feedbacks", project.get("id"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("rating", 10, "comment", "Nota inválida"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retorna404ParaProjetoInexistenteAoRegistrarFeedback() throws Exception {
        mockMvc.perform(post("/api/projects/999999/feedbacks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("rating", 5, "comment", "Ótimo"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void incrementaOContadorDeUpvotesDoProjeto() throws Exception {
        Map<?, ?> profile = createProfile("ana@example.com");
        MvcResult projectRes = createProject(Map.of("title", "Projeto para upvote", "repositoryUrl", "https://github.com/ana/projeto-upvote", "profileId", profile.get("id")));
        Map<?, ?> project = objectMapper.readValue(projectRes.getResponse().getContentAsString(), Map.class);

        mockMvc.perform(put("/api/projects/{id}/upvote", project.get("id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(1));

        mockMvc.perform(put("/api/projects/{id}/upvote", project.get("id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(2));
    }

    @Test
    void retorna404ParaProjetoInexistenteAoDarUpvote() throws Exception {
        mockMvc.perform(put("/api/projects/999999/upvote"))
                .andExpect(status().isNotFound());
    }
}
