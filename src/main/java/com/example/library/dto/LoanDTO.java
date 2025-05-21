package com.example.library.dto;

import java.time.LocalDate;

public record LoanDTO(Long id, Long userID, String email, Long copyId, String title, Long libraryId, LocalDate startDate, LocalDate endDate, LocalDate returnDate) {
}
