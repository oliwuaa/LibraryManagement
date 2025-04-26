package com.example.library.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiresIn) {
}