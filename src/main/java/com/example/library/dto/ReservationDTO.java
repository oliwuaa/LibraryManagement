package com.example.library.dto;

import com.example.library.model.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationDTO(Long id, Long userId, Long copyId, LocalDateTime createdAt, LocalDateTime expirationDate, ReservationStatus status) {
}
