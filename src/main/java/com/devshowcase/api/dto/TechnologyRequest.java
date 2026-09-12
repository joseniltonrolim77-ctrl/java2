package com.devshowcase.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TechnologyRequest {

    @NotBlank(message = "O campo \"name\" é obrigatório.")
    @Size(max = 60, message = "O campo \"name\" deve ter no máximo 60 caracteres.")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
