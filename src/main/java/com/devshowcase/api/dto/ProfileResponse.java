package com.devshowcase.api.dto;

import com.devshowcase.api.entity.Profile;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private String avatarUrl;
    private List<ProjectSummary> projects;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProfileResponse of(Profile profile, boolean includeProjects) {
        ProfileResponse dto = new ProfileResponse();
        dto.id = profile.getId();
        dto.name = profile.getName();
        dto.email = profile.getEmail();
        dto.bio = profile.getBio();
        dto.avatarUrl = profile.getAvatarUrl();
        if (includeProjects) {
            dto.projects = profile.getProjects().stream()
                    .map(p -> new ProjectSummary(p.getId(), p.getTitle()))
                    .collect(Collectors.toList());
        }
        dto.createdAt = profile.getCreatedAt();
        dto.updatedAt = profile.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public List<ProjectSummary> getProjects() { return projects; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
