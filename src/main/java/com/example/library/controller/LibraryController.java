package com.example.library.controller;

import com.example.library.dto.LibraryDTO;
import com.example.library.model.Library;
import com.example.library.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/libraries")
public class LibraryController {

    private final LibraryService libraryService;

    @Operation(summary = "Get all libraries.", description = "Returns a list of all libraries in the system.")
    @GetMapping
    public ResponseEntity<List<Library>> getAllLibraries() {
        List<Library> libraries = libraryService.getAllLibraries();
        if (libraries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(libraries);
    }

    @Operation(summary = "Get library by Id.", description = "Returns a copy with the given Id.")
    @GetMapping("/{libraryId}")
    public ResponseEntity<Library> getLibraryById(@PathVariable("libraryId") Long libraryID) {
        try {
            return ResponseEntity.ok(libraryService.getLibraryById(libraryID));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Add a new library.",
            description = "Adds a new library by providing the name and address. The library will be saved to the database."
    )
    @PostMapping
    public ResponseEntity<String> addLibrary(@RequestBody LibraryDTO library) {
        try {
            libraryService.addLibrary(library);
            return ResponseEntity.ok("Library added successfully");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete a library.", description = "Deletes a library from the system based on the provided library Id. The library will be removed from the database.")
    @DeleteMapping("/{libraryId}")
    public ResponseEntity<String> deleteLibrary(@PathVariable Long libraryId) {
        try {
            libraryService.deleteLibrary(libraryId);
            return ResponseEntity.ok("Library deleted successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Update a library.", description = "Update the name, or address of the library with the given ID. You can update one or more fields.")
    @PutMapping("/{libraryId}")
    public ResponseEntity<String> updateLibrary(@PathVariable Long libraryId, @RequestBody LibraryDTO library) {
        try {
            libraryService.updateLibrary(libraryId, library);
            return ResponseEntity.ok("Library updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Get libraries using search criteria.", description = "Returns a list of libraries that match the provided name or address. You can search using any combination of these criteria.")
    @GetMapping("/search")
    public ResponseEntity<List<Library>> searchLibraries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address) {

        List<Library> libraries = libraryService.searchLibraries(name, address);

        if (libraries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(libraries);
    }
}
