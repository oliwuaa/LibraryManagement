package com.example.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRegistrationDTO(@NotNull String email, @NotNull String password, String name, String surname) {
}