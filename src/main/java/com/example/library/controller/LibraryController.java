package com.example.library.controller;

import com.example.library.dto.LibraryDTO;
import com.example.library.dto.ReservationDTO;
import com.example.library.model.Library;
import com.example.library.model.User;
import com.example.library.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/libraries")
public class LibraryController {

    private final LibraryService libraryService;

    @Operation(
            summary = "Get all libraries.",
            description = "Returns a list of all libraries in the system. If no libraries exist, returns HTTP 204 with an empty body."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of libraries returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Library.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No libraries found",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Library>> getAllLibraries() {
        List<Library> libraries = libraryService.getAllLibraries();
        if (libraries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(libraries);
    }

    @Operation(
            summary = "Get library by Id.",
            description = "Returns a library with the given ID. If no such library exists, returns 404."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Library found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Library.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Library with ID 6 not found\"}")
                    )
            )
    })
    @GetMapping("/{libraryId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<Library> getLibraryById(
            @Parameter(description = "ID of the library to retrieve", example = "6")
            @PathVariable("libraryId") Long libraryID
    ) {
        return ResponseEntity.ok(libraryService.getLibraryById(libraryID));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('LIBRARIAN')")
    public ResponseEntity<Library> getMyLibrary() {
        Library library = libraryService.getLibraryForCurrentLibrarian();
        return ResponseEntity.ok(library);
    }

    @Operation(
            summary = "Add a new library.",
            description = "Adds a new library by providing the name and address. The library will be saved to the database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Library added successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Library added successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request (e.g. library already exists)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"This library already exists\"}")
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> addLibrary(@Valid @RequestBody LibraryDTO library) {
        libraryService.addLibrary(library);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete a library.",
            description = "Deletes a library from the system based on the provided library ID. If any books are currently borrowed or reserved, the library will be marked as CLOSED and books marked as REMOVED. Otherwise, the library will be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Library successfully deleted or marked as closed",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = {
                                    @ExampleObject(name = "Deleted", value = "Library deleted successfully"),
                                    @ExampleObject(name = "Closed", value = "Library could not be deleted due to active loans or reservations. Status set to CLOSED.")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Library with ID 12 doesn't exist.\"}")
                    )
            )
    })
    @DeleteMapping("/{libraryId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> deleteLibrary(@PathVariable Long libraryId) {
        boolean deleted = libraryService.deleteLibrary(libraryId);

        if (deleted) {
            return ResponseEntity.ok("Library deleted successfully");
        } else {
            return ResponseEntity.ok("Library could not be deleted due to active loans or reservations. Status set to CLOSED.");
        }
    }

    @Operation(
            summary = "Update a library.",
            description = "Update the name or address of the library with the given ID. You can update one or more fields."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Library updated successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Library updated successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"At least one field (name or address) must be provided for update.\"}")
                    )
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
    @PutMapping("/{libraryId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> updateLibrary(
            @Parameter(description = "ID of the library to update", example = "5")
            @PathVariable Long libraryId,
            @RequestBody LibraryDTO library
    ) {
        libraryService.updateLibrary(libraryId, library);
        return ResponseEntity.ok("Library updated successfully");
    }

    @Operation(
            summary = "Get libraries using search criteria.",
            description = "Returns a list of libraries that match the provided name or address. You can search using any combination of these criteria."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Libraries matching criteria found",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Library.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No libraries found matching criteria",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid request parameters\"}")
                    )
            )
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<List<Library>> searchLibraries(
            @Parameter(description = "Partial or full name of the library", example = "Central")
            @RequestParam(required = false) String name,
            @Parameter(description = "Partial or full address of the library", example = "Main St")
            @RequestParam(required = false) String address
    ) {
        List<Library> libraries = libraryService.searchLibraries(name, address);

        if (libraries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(libraries);
    }

}