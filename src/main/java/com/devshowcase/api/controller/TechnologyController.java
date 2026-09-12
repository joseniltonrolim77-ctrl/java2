package com.devshowcase.api.controller;

import com.devshowcase.api.dto.TechnologyRequest;
import com.devshowcase.api.dto.TechnologyResponse;
import com.devshowcase.api.entity.Technology;
import com.devshowcase.api.service.TechnologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/technologies")
@Tag(name = "Technologies")
public class TechnologyController {

    private final TechnologyService technologyService;

    public TechnologyController(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma nova tecnologia")
    public TechnologyResponse create(@Valid @RequestBody TechnologyRequest request) {
        Technology technology = technologyService.create(request);
        return TechnologyResponse.of(technology);
    }

    @GetMapping
    @Operation(summary = "Lista todas as tecnologias cadastradas")
    public List<TechnologyResponse> findAll() {
        return technologyService.findAll().stream().map(TechnologyResponse::of).collect(Collectors.toList());
    }
}
