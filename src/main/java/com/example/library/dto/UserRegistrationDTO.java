package com.example.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRegistrationDTO(@NotBlank String email, @NotBlank String password, String name, String surname) {
}