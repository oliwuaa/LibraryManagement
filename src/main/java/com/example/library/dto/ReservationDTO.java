package com.example.library.dto;

import com.example.library.model.ReservationStatus;

import java.time.LocalDate;

public record ReservationDTO(Long id, Long userId, String email, Long copyId, String title, Long libraryId, String libraryName, LocalDate createdAt, LocalDate expirationDate, ReservationStatus status) {
}
