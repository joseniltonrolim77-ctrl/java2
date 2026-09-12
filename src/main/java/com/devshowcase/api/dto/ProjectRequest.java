package com.devshowcase.api.dto;

import com.devshowcase.api.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ProjectRequest {

    @NotBlank(message = "O campo \"title\" é obrigatório.")
    @Size(max = 150, message = "O campo \"title\" deve ter no máximo 150 caracteres.")
    private String title;

    @Size(max = 2000, message = "O campo \"description\" deve ter no máximo 2000 caracteres.")
    private String description;

    @NotBlank(message = "O campo \"repositoryUrl\" é obrigatório.")
    @ValidUrl(message = "O campo \"repositoryUrl\" deve ser uma URL válida.")
    private String repositoryUrl;

    @NotNull(message = "O campo \"profileId\" é obrigatório.")
    @Positive(message = "O campo \"profileId\" deve ser um número positivo.")
    private Long profileId;

    private List<@Positive Long> technologyIds = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public List<Long> getTechnologyIds() { return technologyIds == null ? new ArrayList<>() : technologyIds; }
    public void setTechnologyIds(List<Long> technologyIds) { this.technologyIds = technologyIds; }
}
