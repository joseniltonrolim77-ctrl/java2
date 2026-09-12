package com.devshowcase.api.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Hidden
public class HealthController {

    @GetMapping("/api")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "DevShowcase API");
        body.put("status", "ok");
        body.put("docs", "/api/docs");
        return body;
    }
}
