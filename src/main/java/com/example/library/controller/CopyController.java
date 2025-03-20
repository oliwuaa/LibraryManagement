package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.Copy;
import com.example.library.model.CopyStatus;
import com.example.library.service.CopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/copies")
public class CopyController {
    private final CopyService copyService;

    @GetMapping
    public ResponseEntity<List<Copy>> getAllCopies() {
        List<Copy> copies = copyService.getAllCopies();
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(copies);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Copy>> getAllAvailableCopies() {
        List<Copy> copies = copyService.getAvailableCopies();
        if (copies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(copies);
    }

    @GetMapping("/{copyId}")
    public ResponseEntity<Copy> getCopyById(@PathVariable Long copyId) {
        try {
            return ResponseEntity.ok(copyService.getCopyById(copyId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

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

    @PutMapping("/{copyId}")
    public ResponseEntity<String> changeStatus(@PathVariable Long copyId, @RequestBody CopyStatus status) {
        try {
            copyService.updateCopyStatus(copyId, status);
            return ResponseEntity.ok("Copy status changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred");
        }
    }

    // TO CHANGE
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
