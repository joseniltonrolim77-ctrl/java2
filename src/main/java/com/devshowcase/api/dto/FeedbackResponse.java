package com.devshowcase.api.dto;

import com.devshowcase.api.entity.Feedback;

import java.time.Instant;

public class FeedbackResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private Long projectId;
    private Instant createdAt;
    private Instant updatedAt;

    public static FeedbackResponse of(Feedback feedback) {
        FeedbackResponse dto = new FeedbackResponse();
        dto.id = feedback.getId();
        dto.rating = feedback.getRating();
        dto.comment = feedback.getComment();
        dto.projectId = feedback.getProject().getId();
        dto.createdAt = feedback.getCreatedAt();
        dto.updatedAt = feedback.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public Long getProjectId() { return projectId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
