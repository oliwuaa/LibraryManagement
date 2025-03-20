package com.example.library.controller;

import com.example.library.model.Library;
import com.example.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/libraries")
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<List<Library>> getAllLibraries() {
        List<Library> libraries = libraryService.getAllLibraries();
        if (libraries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(libraries);
    }

    @GetMapping("/{libraryId}")
    public ResponseEntity<Library> getLibraryById(@PathVariable("libraryId") Long libraryID) {
        try {
            return ResponseEntity.ok(libraryService.getLibraryById(libraryID));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<String> addLibrary(@RequestBody Library library) {
        try {
            libraryService.addLibrary(library);
            return ResponseEntity.ok("Library added successfully");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

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

    @PutMapping("/{libraryId}")
    public ResponseEntity<String> updateLibrary(@PathVariable Long libraryId, @RequestBody Library library) {
        try {
            libraryService.updateLibrary(libraryId, library);
            return ResponseEntity.ok("Library deleted successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

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
