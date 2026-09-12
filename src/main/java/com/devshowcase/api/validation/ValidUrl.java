package com.devshowcase.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valida que o valor, quando presente, é uma URL válida (com esquema, ex.: http://, https://).
 * Valores nulos ou em branco são considerados válidos (use @NotBlank separadamente se o campo for obrigatório).
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidUrlValidator.class)
public @interface ValidUrl {
    String message() default "deve ser uma URL válida.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
