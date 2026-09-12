package com.devshowcase.api.controller;

import com.devshowcase.api.dto.*;
import com.devshowcase.api.entity.Project;
import com.devshowcase.api.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@Validated
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo projeto")
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        Project project = projectService.create(request);
        return ProjectResponse.of(project);
    }

    @GetMapping
    @Operation(summary = "Lista projetos com filtro por tecnologia e paginação")
    public PagedProjectResponse findAll(
            @RequestParam(required = false) @Positive Long profileId,
            @RequestParam(required = false) String technology,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        Page<Project> result = projectService.list(profileId, technology, page, limit);
        List<ProjectResponse> data = result.getContent().stream().map(ProjectResponse::of).collect(Collectors.toList());
        PaginationResponse pagination = new PaginationResponse(
                page, limit, result.getTotalElements(),
                result.getTotalElements() == 0 ? 0 : result.getTotalPages());
        return new PagedProjectResponse(data, pagination);
    }

    @PostMapping("/{id}/feedbacks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma nota (1 a 5) e comentário para o projeto, recalculando a nota média")
    public FeedbackCreatedResponse createFeedback(@PathVariable @Positive Long id,
                                                   @Valid @RequestBody FeedbackRequest request) {
        ProjectService.FeedbackResult result = projectService.addFeedback(id, request);
        return new FeedbackCreatedResponse(FeedbackResponse.of(result.feedback()), result.averageRating());
    }

    @PutMapping("/{id}/upvote")
    @Operation(summary = "Incrementa em 1 a contagem de curtidas/estrelas (upvotes) do projeto")
    public ProjectResponse upvote(@PathVariable @Positive Long id) {
        Project project = projectService.upvote(id);
        return ProjectResponse.of(project);
    }
}
