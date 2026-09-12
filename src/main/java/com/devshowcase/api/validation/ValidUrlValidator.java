package com.devshowcase.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.util.regex.Pattern;

public class ValidUrlValidator implements ConstraintValidator<ValidUrl, String> {

    private static final Pattern SCHEME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://.+");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (!SCHEME_PATTERN.matcher(value).matches()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            return uri.getHost() != null || uri.getScheme() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
