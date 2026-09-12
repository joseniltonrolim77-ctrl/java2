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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileApiTest {

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

    @Test
    void criaPerfilComDadosValidos() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Carlos Silva",
                "email", "carlos@example.com",
                "bio", "Dev backend",
                "avatarUrl", "https://example.com/carlos.png");

        mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Carlos Silva"))
                .andExpect(jsonPath("$.email").value("carlos@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void rejeitaCadastroSemName() throws Exception {
        Map<String, Object> body = Map.of("email", "sem-nome@example.com");
        mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaEmailInvalido() throws Exception {
        Map<String, Object> body = Map.of("name", "Fulano", "email", "nao-e-email");
        mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaAvatarUrlInvalida() throws Exception {
        Map<String, Object> body = Map.of("name", "Fulano", "email", "fulano@example.com", "avatarUrl", "nao-e-url");
        mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaEmailDuplicado() throws Exception {
        Map<String, Object> body = Map.of("name", "Carlos Silva", "email", "carlos@example.com");
        mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        Map<String, Object> body2 = Map.of("name", "Outro Carlos", "email", "carlos@example.com");
        mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body2)))
                .andExpect(status().isConflict());
    }

    @Test
    void retornaPerfilExistente() throws Exception {
        Map<String, Object> body = Map.of("name", "Ana Souza", "email", "ana@example.com");
        MvcResult created = mockMvc.perform(post("/api/profiles").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> createdBody = objectMapper.readValue(created.getResponse().getContentAsString(), Map.class);
        Object id = createdBody.get("id");

        mockMvc.perform(get("/api/profiles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(((Number) id).intValue()))
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.projects").isEmpty());
    }

    @Test
    void retorna404ParaPerfilInexistente() throws Exception {
        mockMvc.perform(get("/api/profiles/999999"))
                .andExpect(status().isNotFound());
    }
}
