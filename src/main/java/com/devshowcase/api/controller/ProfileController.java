package com.devshowcase.api.controller;

import com.devshowcase.api.dto.ProfileRequest;
import com.devshowcase.api.dto.ProfileResponse;
import com.devshowcase.api.entity.Profile;
import com.devshowcase.api.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@Validated
@Tag(name = "Profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo perfil de desenvolvedor")
    public ProfileResponse create(@Valid @RequestBody ProfileRequest request) {
        Profile profile = profileService.create(request);
        return ProfileResponse.of(profile, false);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um perfil por id")
    public ProfileResponse findById(@PathVariable @Positive Long id) {
        Profile profile = profileService.getById(id);
        return ProfileResponse.of(profile, true);
    }
}
