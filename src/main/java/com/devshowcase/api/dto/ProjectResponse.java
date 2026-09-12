package com.devshowcase.api.dto;

import com.devshowcase.api.entity.Project;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String repositoryUrl;
    private Double averageRating;
    private Integer upvotes;
    private ProfileSummary profile;
    private List<TechnologyResponse> technologies;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProjectResponse of(Project project) {
        ProjectResponse dto = new ProjectResponse();
        dto.id = project.getId();
        dto.title = project.getTitle();
        dto.description = project.getDescription();
        dto.repositoryUrl = project.getRepositoryUrl();
        dto.averageRating = project.getAverageRating() != null ? project.getAverageRating() : 0d;
        dto.upvotes = project.getUpvotes() != null ? project.getUpvotes() : 0;
        dto.profile = project.getProfile() != null
                ? new ProfileSummary(project.getProfile().getId(), project.getProfile().getName(), project.getProfile().getEmail())
                : null;
        dto.technologies = project.getTechnologies().stream()
                .sorted(Comparator.comparing(t -> t.getId()))
                .map(TechnologyResponse::of)
                .collect(Collectors.toList());
        dto.createdAt = project.getCreatedAt();
        dto.updatedAt = project.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public Double getAverageRating() { return averageRating; }
    public Integer getUpvotes() { return upvotes; }
    public ProfileSummary getProfile() { return profile; }
    public List<TechnologyResponse> getTechnologies() { return technologies; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
