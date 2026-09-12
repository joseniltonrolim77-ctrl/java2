package com.devshowcase.api.dto;

import com.devshowcase.api.validation.ValidUrl;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileRequest {

    @NotBlank(message = "O campo \"name\" é obrigatório.")
    @Size(max = 120, message = "O campo \"name\" deve ter no máximo 120 caracteres.")
    private String name;

    @NotBlank(message = "O campo \"email\" é obrigatório.")
    @Email(message = "O campo \"email\" deve ser um e-mail válido.")
    private String email;

    @Size(max = 1000, message = "O campo \"bio\" deve ter no máximo 1000 caracteres.")
    private String bio;

    @ValidUrl(message = "O campo \"avatarUrl\" deve ser uma URL válida.")
    private String avatarUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
