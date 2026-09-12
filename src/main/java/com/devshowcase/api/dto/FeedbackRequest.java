package com.devshowcase.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FeedbackRequest {

    @NotNull(message = "O campo \"rating\" é obrigatório.")
    @Min(value = 1, message = "O campo \"rating\" deve ser no mínimo 1.")
    @Max(value = 5, message = "O campo \"rating\" deve ser no máximo 5.")
    private Integer rating;

    @NotBlank(message = "O campo \"comment\" é obrigatório.")
    @Size(max = 1000, message = "O campo \"comment\" deve ter no máximo 1000 caracteres.")
    private String comment;

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
