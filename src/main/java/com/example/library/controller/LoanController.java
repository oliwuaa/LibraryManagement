package com.example.library.controller;

import com.example.library.dto.LoanDTO;
import com.example.library.model.Loan;
import com.example.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("loans")
public class LoanController {
    private final LoanService loanService;

    @Operation(
            summary = "Get all loans.",
            description = "Returns a list of all loans in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of loans returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Loan.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No loans found",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        List<LoanDTO> loans = loanService.getAllLoans();
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }


    @Operation(
            summary = "Get all user's loans.",
            description = "Returns a list of all loans associated with a specific user in the system. The loans will be returned based on the user's ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of loans returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Loan.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "User exists, but no loans found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"User with ID 42 does not exist\"}")
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('LIBRARIAN') and @authorizationService.isUserInLibrarianLibrary(#userId)) or " +
            "(hasRole('USER') and @authorizationService.isSelf(#userId))")
    @GetMapping("user/{userId}")
    public ResponseEntity<List<LoanDTO>> getUserLoans(
            @Parameter(description = "ID of the user whose loans will be fetched", example = "5")
            @PathVariable Long userId
    ) {
        List<LoanDTO> loans = loanService.getAllUserLoan(userId);
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Get loan by ID",
            description = "Returns the loan with the given ID. If no loan is found, a 404 error is returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Loan with ID 10 does not exist\"}")
                    )
            )
    })
    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "hasRole('USER') and @authorizationService.isUserLoan(#loanId) or " +
            "(hasRole('LIBRARIAN') and @authorizationService.isLoanInLibrarianLibrary(#loanId))")
    public ResponseEntity<LoanDTO> getLoanById(
            @Parameter(description = "ID of the loan to fetch", example = "10")
            @PathVariable Long loanId
    ) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    @Operation(
            summary = "Create a loan.",
            description = "Creates a new loan by borrowing a book. The loan is associated with a user and a specific copy of the book. " +
                    "The user provides the book's copy ID and their user ID. If the operation is successful, " +
                    "the copy will be marked as borrowed, and the loan will be saved in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book borrowed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Book borrowed successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request – copy not available or already borrowed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"This copy isn't available\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or copy not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "User Not Found", value = "{\"error\": \"User with ID 10 does not exist\"}"),
                                    @ExampleObject(name = "Copy Not Found", value = "{\"error\": \"Copy with ID 33 does not exist\"}")
                            }
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('LIBRARIAN') and @authorizationService.isCopyInLibrarianLibrary(#copyId))")
    public ResponseEntity<String> createLoan(
            @Parameter(description = "ID of the user borrowing the book", example = "5") @RequestParam Long userId,
            @Parameter(description = "ID of the book copy being borrowed", example = "12") @RequestParam Long copyId
    ) {
        loanService.borrowBook(userId, copyId);
        return ResponseEntity.ok("Book borrowed successfully");
    }

    @Operation(
            summary = "Return a borrowed book.",
            description = "Allows a user to return a book they have borrowed. The loan is identified by its ID. " +
                    "Once the book is returned, its status is updated to 'AVAILABLE', and the loan is marked as returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book returned successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Book returned successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Book has already been returned or loan doesn't exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"This book has already been returned or loan doesn't exist\"}")
                    )
            )
    })
    @PostMapping("/{loanId}/return")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('LIBRARIAN') and @authorizationService.isLoanInLibrarianLibrary(#loanId))")
    public ResponseEntity<String> returnBook(
            @Parameter(description = "ID of the loan to return", example = "1")
            @PathVariable Long loanId
    ) {
        loanService.returnBook(loanId);
        return ResponseEntity.ok("Book returned successfully");
    }

    @Operation(
            summary = "Extend a loan.",
            description = "Extends the return date of a borrowed copy. Requires the loan's ID and a new return date. " +
                    "The new date must be after the current due date. If the loan doesn't exist or is already returned, " +
                    "an appropriate error message will be returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan extended successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Loan extended successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request (e.g., return date before current endDate or invalid format)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Invalid Date Order", value = "{\"error\": \"The new date must be after endDate\"}"),
                                    @ExampleObject(name = "Invalid Date Format", value = "{\"error\": \"Invalid value '1' for parameter 'returnDate'. Expected type: LocalDate\"}")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found or already returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"This loan doesn't exist\"}")
                    )
            )
    })
    @PostMapping("/{loanId}/extend")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('LIBRARIAN') and @authorizationService.isLoanInLibrarianLibrary(#loanId))")
    public ResponseEntity<String> extendLoan(
            @Parameter(description = "ID of the loan to extend", example = "10")
            @PathVariable Long loanId,

            @Parameter(description = "New return date in ISO format (yyyy-MM-dd)", example = "2025-04-30")
            @RequestParam LocalDate returnDate
    ) {
        loanService.extendLoan(returnDate, loanId);
        return ResponseEntity.ok("Loan extended successfully");
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER')")
    @Operation(
            summary = "Get authenticated user's loans",
            description = "Returns a list of all loans for the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of loans returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoanDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - user not authenticated",
                    content = @Content
            )
    })
    public ResponseEntity<List<LoanDTO>> getMyLoans() {
        List<LoanDTO> loans = loanService.getMyLoans();
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/me/active")
    @PreAuthorize("hasAnyRole('USER')")
    @Operation(
            summary = "Get authenticated user's active loans",
            description = "Returns a list of all active (not returned) loans for the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of active loans returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoanDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - user not authenticated",
                    content = @Content
            )
    })
    public ResponseEntity<List<LoanDTO>> getMyActiveLoans() {
        List<LoanDTO> loans = loanService.getMyActiveLoans();
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Get all loans in a library",
            description = "Returns a list of all loans associated with a specific library, identified by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of loans returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoanDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No loans found for the specified library",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user not authorized to access this library's loans",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Library with ID 1 does not exist\"}")
                    )
            )
    })
    @GetMapping("/library/{libraryId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('LIBRARIAN') and @authorizationService.isLibrarianOfLibrary(#libraryId))")
    public ResponseEntity<List<LoanDTO>> getLoansByLibrary(
            @Parameter(description = "Library ID", example = "1") @PathVariable Long libraryId
    ) {
        List<LoanDTO> loans = loanService.getLoansByLibrary(libraryId);
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }
}