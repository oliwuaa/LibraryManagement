package com.example.library.controller;

import com.example.library.dto.UserDTO;
import com.example.library.dto.UserRegistrationDTO;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get all users.", description = "Returns a list of all users in the system.")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by Id.", description = "Returns an user with the given Id.")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Get users by role.",
            description = "Returns a list of users with the specified role. Available roles: USER, LIBRARIAN."
    )
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.getUsersByRole(role);

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get all librarians from a library.", description = "Returns a list of all librarians belonging to the library with the given Id.")
    @GetMapping("/library/{libraryId}/librarians")
    public ResponseEntity<List<User>> getLibrariansByLibraryId(@PathVariable Long libraryId) {
        List<User> librarians = userService.getLibrariansFromLibrary(libraryId);

        if (librarians.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(librarians);
    }

    @Operation(summary = "Register a user.", description = "Registers a user. If you want to register a librarian, it requires the ID of an existing library.")
    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO user, @RequestParam(required = false) Long libraryId) {
        try {
            userService.addUser(user, libraryId);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Update a user.", description = "Update the name, surname or e-mail. You can update one or more fields.")
    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody UserDTO user) {
        try {
            userService.updateUser(userId, user);
            return ResponseEntity.ok("User updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Change user's role.", description = "Changes the role of a user with the given ID. If changing the role to LIBRARIAN, a library ID must be provided."
    )
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


    @Operation(summary = "Delete a user.", description = "Deletes a user from the system based on the provided user Id. The user will be removed from the database.")
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

    @Operation(summary = "Get users using search criteria.", description = "Returns a list of users that match the provided name, email, or role. You can search using any combination of these criteria.")
    @GetMapping("/search")
    public ResponseEntity<List<User>> getUsersBySearchCriteria(@RequestParam(required = false) String name, @RequestParam(required = false) String email, @RequestParam(required = false) UserRole role) {

        List<User> users = userService.searchUsers(name, email, role);

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }

}
