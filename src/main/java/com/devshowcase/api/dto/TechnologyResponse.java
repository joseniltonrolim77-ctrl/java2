package com.devshowcase.api.dto;

import com.devshowcase.api.entity.Technology;

import java.time.Instant;

public class TechnologyResponse {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;

    public static TechnologyResponse of(Technology technology) {
        TechnologyResponse dto = new TechnologyResponse();
        dto.id = technology.getId();
        dto.name = technology.getName();
        dto.createdAt = technology.getCreatedAt();
        dto.updatedAt = technology.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
