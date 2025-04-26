package com.example.library.dto;

public record UserInfoDTO(Long id, String email, String name, String surname, String role, Long libraryId) {
}