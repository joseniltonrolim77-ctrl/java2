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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TechnologyApiTest {

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
    void criaTecnologiaComNomeValido() throws Exception {
        Map<String, Object> body = Map.of("name", "Node.js");
        mockMvc.perform(post("/api/technologies").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Node.js"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void rejeitaNomeVazio() throws Exception {
        Map<String, Object> body = Map.of("name", "");
        mockMvc.perform(post("/api/technologies").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaNomeDuplicado() throws Exception {
        Map<String, Object> body = Map.of("name", "Node.js");
        mockMvc.perform(post("/api/technologies").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/technologies").contentType("application/json").content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void listaTodasAsTecnologiasCadastradas() throws Exception {
        mockMvc.perform(post("/api/technologies").contentType("application/json").content(objectMapper.writeValueAsString(Map.of("name", "Node.js"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/technologies").contentType("application/json").content(objectMapper.writeValueAsString(Map.of("name", "React"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/technologies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Node.js"))
                .andExpect(jsonPath("$[1].name").value("React"));
    }
}
