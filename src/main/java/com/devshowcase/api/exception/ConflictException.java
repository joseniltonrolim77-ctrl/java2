package com.devshowcase.api.exception;

import java.util.List;

public class ConflictException extends RuntimeException {

    private final List<String> errors;

    public ConflictException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public ConflictException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
