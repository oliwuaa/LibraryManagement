package com.example.library.dto;

import java.time.LocalDate;

public record LoanDTO(Long id, Long userID, Long copyId, LocalDate startDate, LocalDate endDate, LocalDate returnDate) {
}
