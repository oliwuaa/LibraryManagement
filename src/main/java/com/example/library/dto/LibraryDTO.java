package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;

public record LibraryDTO(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address
) {}
