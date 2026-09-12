package com.devshowcase.api.dto;

public class FeedbackCreatedResponse {
    private FeedbackResponse feedback;
    private Double projectAverageRating;

    public FeedbackCreatedResponse(FeedbackResponse feedback, Double projectAverageRating) {
        this.feedback = feedback;
        this.projectAverageRating = projectAverageRating;
    }

    public FeedbackResponse getFeedback() { return feedback; }
    public Double getProjectAverageRating() { return projectAverageRating; }
}
