package com.example.library.controller;

import com.example.library.model.Copy;
import com.example.library.model.CopyStatus;
import com.example.library.service.CopyService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/copies")
public class CopyController {
    private final CopyService copyService;

    @Operation(
            summary = "Get all copies.",
            description = "Returns a list of all book copies in the system. If no copies are found, returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of copies returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Copy.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No copies found",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Copy>> getAllCopies() {
        List<Copy> copies = copyService.getAllCopies();
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(copies);
    }

    @Operation(
            summary = "Get all available copies.",
            description = "Returns a list of all copies currently available for borrowing. If no available copies are found, returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of available copies returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Copy.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No available copies found",
                    content = @Content
            )
    })
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Copy>> getAllAvailableCopies() {
        List<Copy> copies = copyService.getAvailableCopies();
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(copies);
    }

    @Operation(
            summary = "Get copy by ID.",
            description = "Returns a copy with the given ID if it exists. If the copy is not found, returns 404."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Copy found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Copy.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Copy not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Copy with ID 5 does not exist\"}")
                    )
            )
    })
    @GetMapping("/{copyId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<Copy> getCopyById(
            @Parameter(description = "ID of the copy to retrieve", example = "5")
            @PathVariable Long copyId
    ) {
        return ResponseEntity.ok(copyService.getCopyById(copyId));
    }

    @Operation(
            summary = "Add a copy to the library.",
            description = "Adds a new copy of an existing book (by book ID) to a library (by library ID). " +
                    "The new copy will be saved and marked as AVAILABLE."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Copy added successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Copy added successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book or Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Book Not Found", value = "{\"error\": \"Book with ID 99 does not exist\"}"),
                                    @ExampleObject(name = "Library Not Found", value = "{\"error\": \"Library with ID 8 does not exist\"}")
                            }
                    )
            )
    })
    @PostMapping("/{bookId}/{libraryId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "hasRole('LIBRARIAN') and @authorizationService.isLibrarianOfLibrary(#libraryId) ")
    public ResponseEntity<String> addCopy(
            @Parameter(description = "ID of the book", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "ID of the library", example = "2")
            @PathVariable Long libraryId
    ) {
        copyService.addCopy(bookId, libraryId);
        return ResponseEntity.ok("Copy added successfully");
    }

    @Operation(
            summary = "Change the status of a copy.",
            description = "Updates the status of a specific copy (identified by its ID). " +
                    "Available statuses: AVAILABLE, BORROWED, REMOVED, RESERVED."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Copy status changed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Copy status changed successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request (invalid status value or status already set)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Invalid Status", value = "{\"error\": \"Invalid value 'ABC' for parameter. Expected type: CopyStatus. Allowed values: AVAILABLE, BORROWED, REMOVED, RESERVED\"}"),
                                    @ExampleObject(name = "Duplicate Status", value = "{\"error\": \"Copy already has this status.\"}")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Copy not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Copy with ID 42 does not exist\"}")
                    )
            )
    })

    @PutMapping("/{copyId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "hasRole('LIBRARIAN') and @authorizationService.isCopyInLibrarianLibrary(#copyId)")
    public ResponseEntity<String> changeStatus(
            @Parameter(description = "ID of the copy to update", example = "1")
            @PathVariable Long copyId,
            @Parameter(description = "New status for the copy (AVAILABLE, BORROWED, REMOVED, RESERVED)", example = "AVAILABLE")
            @RequestBody CopyStatus status
    ) {
        copyService.updateCopyStatus(copyId, status);
        return ResponseEntity.ok("Copy status changed successfully");
    }

    @Operation(
            summary = "Delete a copy.",
            description = "Deletes a copy identified by its ID. If the copy is AVAILABLE and not involved in any active loans or reservations, it's removed from the database. " +
                    "If loans or reservations exist, the copy is marked as REMOVED. If the copy is already BORROWED, RESERVED, or REMOVED, deletion fails."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Copy deleted successfully or marked as REMOVED",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Copy deleted successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cannot delete due to copy status",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Cannot delete copy – it is currently borrowed, reserved, or already removed.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Copy not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Copy with ID 42 does not exist\"}")
                    )
            )
    })
    @DeleteMapping("/{copyId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "hasRole('LIBRARIAN') and @authorizationService.isCopyInLibrarianLibrary(#copyId) ")
    public ResponseEntity<String> deleteCopy(
            @Parameter(description = "ID of the copy to delete", example = "1")
            @PathVariable Long copyId
    ) {
        copyService.deleteCopy(copyId);
        return ResponseEntity.ok("Copy deleted successfully");
    }

    @Operation(
            summary = "Get all available copies by library ID.",
            description = "Returns a list of all available copies belonging to a specific library. If no copies are found, returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of copies found for the library",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Copy.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No copies found for the library",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Library with ID 5 does not exist\"}")
                    )
            )
    })
    @GetMapping("/library/{libraryId}/available")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Copy>> getAvailableCopiesByLibrary(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable Long libraryId
    ) {
        List<Copy> copies = copyService.getAvailableCopiesByLibrary(libraryId);
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(copies);
    }

    @Operation(
            summary = "Get all copies by library ID.",
            description = "Returns a list of all copies belonging to a specific library. If no copies are found, returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of copies found for the library",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Copy.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No copies found for the library",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Library with ID 5 does not exist\"}")
                    )
            )
    })
    @GetMapping("/library/{libraryId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Copy>> getCopiesByLibrary(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable Long libraryId
    ) {
        List<Copy> copies = copyService.getCopiesByLibrary(libraryId);
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(copies);
    }

    @Operation(
            summary = "Get all available copies by book ID.",
            description = "Returns a list of all available copies belonging to a specific book. If no copies are found, returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of copies found for the book",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Copy.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No copies found for the book",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Book with ID 5 does not exist\"}")
                    )
            )
    })
    @GetMapping("/book/{bookId}/available")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Copy>> getAvailableCopiesByBook(
            @Parameter(description = "ID of the book", example = "1")
            @PathVariable Long bookId
    ) {
        List<Copy> copies = copyService.getAvailableCopiesByBook(bookId);
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(copies);
    }

    @Operation(
            summary = "Get all available copies by book ID in Library with specific ID.",
            description = "Returns a list of all copies belonging to a specific book in a specific library. If no copies are found, returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of copies found for the book",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Copy.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No copies found for the book",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Book with ID 5 does not exist\"}")
                    )
            )
    })
    @GetMapping("/library/{libraryId}/book/{bookId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Copy>> getCopiesByBookAndByLibrary(
            @Parameter(description = "ID of the book", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable Long libraryId
    ) {
        List<Copy> copies = copyService.getAvailableCopiesOfBookByLibrary(bookId, libraryId);
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(copies);
    }

    @Operation(
            summary = "Get all copies by book ID.",
            description = "Returns a list of all copies belonging to a specific book. If no copies are found, returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of copies found for the book",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Copy.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No copies found for the book",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Book with ID 5 does not exist\"}")
                    )
            )
    })
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Copy>> getCopiesByBook(
            @Parameter(description = "ID of the book", example = "1")
            @PathVariable Long bookId
    ) {
        List<Copy> copies = copyService.getCopiesByBook(bookId);
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(copies);
    }
}