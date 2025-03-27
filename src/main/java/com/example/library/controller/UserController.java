package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.getUsersByRole(role);

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(users);
    }

    @GetMapping("/library/{libraryId}/librarians")
    public ResponseEntity<List<User>> getLibrariansByLibraryId(@PathVariable Long libraryId) {
        List<User> librarians = userService.getLibrariansFromLibrary(libraryId);

        if (librarians.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(librarians);
    }

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody User user, @RequestParam(required = false) Long libraryId) {
        try {
            userService.addUser(user, libraryId);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody User user) {
        try {
            userService.updateUser(userId, user);
            return ResponseEntity.ok("User updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<String> changeRole(@PathVariable Long userId, @RequestBody UserRole role, @RequestParam(required = false) Long libraryId) {
        try {
            userService.changeRole(userId, role, libraryId);
            return ResponseEntity.ok("User updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> getUsersBySearchCriteria(@RequestParam(required = false) String name, @RequestParam(required = false) String email, @RequestParam(required = false) UserRole role) {

        List<User> users = userService.searchUsers(name, email, role);

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }

}
