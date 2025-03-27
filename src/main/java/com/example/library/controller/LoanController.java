package com.example.library.controller;

import com.example.library.model.Loan;
import com.example.library.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("loans")
public class LoanController {
    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        List<Loan> loans = loanService.getAllLoans();
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(loans);
    }

    @GetMapping("users/{userId}")
    public ResponseEntity<List<Loan>> getUserLoans(@PathVariable Long userId) {
        List<Loan> loans = loanService.getAllUserLoan(userId);
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }

    // ADD CHECKING IF LIBRARIAN IS FROM THE SAME LIBRARY
    @PostMapping
    public ResponseEntity<String> createLoan(@RequestParam Long userId, @RequestParam Long copyId) {
        try {
            loanService.borrowBook(userId, copyId);
            return ResponseEntity.ok("Book borrowed successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{loanId}/return")
    public ResponseEntity<String> returnBook(@PathVariable Long loanId) {
        try {
            loanService.returnBook(loanId);
            return ResponseEntity.ok("Book returned successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{loanId}/extend")
    public ResponseEntity<String> extendLoan(@PathVariable Long loanId, @RequestParam LocalDate returnDate) {
        try {
            loanService.extendLoan(returnDate, loanId);
            return ResponseEntity.ok("Loan extended successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
