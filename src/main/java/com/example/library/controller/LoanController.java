package com.example.library.controller;

import com.example.library.model.Loan;
import com.example.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Get all loans.", description = "Returns a list of all loans in the system.")
    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        List<Loan> loans = loanService.getAllLoans();
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "Get all user's loans.",
            description = "Returns a list of all loans associated with a specific user in the system. " +
                    "The loans will be returned based on the user's ID")
    @GetMapping("users/{userId}")
    public ResponseEntity<List<Loan>> getUserLoans(@PathVariable Long userId) {
        List<Loan> loans = loanService.getAllUserLoan(userId);
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Create a loan.",
            description = "Creates a new loan by borrowing a book. The loan is associated with a user and a specific copy of the book. " +
                    "The user provides the book's copy ID and their user ID. If the operation is successful, " +
                    "the copy will be marked as borrowed, and the loan will be saved in the system."
    )
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

    @Operation(
            summary = "Return a borrowed book.",
            description = "This operation allows a user to return a book they have borrowed. The loan is identified by its ID. " +
                    "Once the book is returned, its status is updated to 'available', and the loan is marked as returned. "
    )
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

    @Operation(
            summary = "Extend a loan.",
            description = "Extends the return date of a borrowed copy. Requires the loan's ID and a new return date."
    )
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
