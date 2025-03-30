package com.example.library.controller;

import com.example.library.model.Copy;
import com.example.library.model.CopyStatus;
import com.example.library.service.CopyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/copies")
public class CopyController {
    private final CopyService copyService;

    @Operation(summary = "Get all copies.", description = "Returns a list of all copies in the system.")
    @GetMapping
    public ResponseEntity<List<Copy>> getAllCopies() {
        List<Copy> copies = copyService.getAllCopies();
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(copies);
    }

    @Operation(summary = "Get all available copies.", description = "Returns a list of all available copies in the system.")
    @GetMapping("/available")
    public ResponseEntity<List<Copy>> getAllAvailableCopies() {
        List<Copy> copies = copyService.getAvailableCopies();
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(copies);
    }

    @Operation(summary = "Get copy by Id.", description = "Returns a copy with the given Id.")
    @GetMapping("/{copyId}")
    public ResponseEntity<Copy> getCopyById(@PathVariable Long copyId) {
        try {
            return ResponseEntity.ok(copyService.getCopyById(copyId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Add a copy to the library.",
            description = "Adds a new copy of an existing book (identified by its book Id) to a specific library (identified by its library Id). " +
                    "The new copy will be saved in the database, making it available for borrowing or reserving."
    )
    @PostMapping("/{bookId}/{libraryId}")
    public ResponseEntity<String> addCopy(@PathVariable Long bookId,
                                          @PathVariable Long libraryId) {
        try {
            copyService.addCopy(bookId, libraryId);
            return ResponseEntity.ok("Copy added successfully");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred");
        }
    }

    @Operation(
            summary = "Change the status of a copy.",
            description = "Updates the status of a specific copy (identified by its Id). " +
                    "The status can be changed to one of the following: AVAILABLE, BORROWED, REMOVED, or RESERVED. "
    )
    @PutMapping("/{copyId}")
    public ResponseEntity<String> changeStatus(@PathVariable Long copyId, @RequestBody CopyStatus status) {
        try {
            copyService.updateCopyStatus(copyId, status);
            return ResponseEntity.ok("Copy status changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred");
        }
    }

    @Operation(summary = "Delete a copy.", description = "Deletes a copy from the system based on the provided copy Id. The copy will be removed from the database.")
    @DeleteMapping("/{copyId}")
    public ResponseEntity<String> deleteCopy(@PathVariable Long copyId) {
        try {
            copyService.deleteCopy(copyId);
            return ResponseEntity.ok("Copy deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Copy not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

}
