package com.example.library.controller;

import com.example.library.dto.UserDTO;
import com.example.library.dto.UserInfoDTO;
import com.example.library.dto.UserRegistrationDTO;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.service.UserService;
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
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get active users.", description = "Returns a list of all aactive users in the system.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No users found",
                    content = @Content
            )
    })
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserInfoDTO>> getActiveUsers() {
        List<UserInfoDTO> users = userService.getActiveUsers();
        if (users.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get all users.", description = "Returns a list of all users in the system.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No users found",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserInfoDTO>> getAllUsers() {
        List<UserInfoDTO> users = userService.getAllUsers();
        if (users.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get user by Id",
            description = "Returns a user with the given Id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "USER",
                                    description = "User with role USER (no library assigned)",
                                    value = """
                                            {
                                                "id": 0,
                                                "password": "string",
                                                "email": "string",
                                                "name": "string",
                                                "surname": "string",
                                                "role": "USER",
                                                "library": null
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "LIBRARIAN",
                                    description = "User with role LIBRARIAN (has library assigned)",
                                    value = """
                                            {
                                                "id": 0,
                                                "password": "string",
                                                "email": "string",
                                                "name": "string",
                                                "surname": "string",
                                                "role": "LIBRARIAN",
                                                "library": {
                                                  "id": 0,
                                                  "name": "string",
                                                  "address": "string",
                                                  "status": "ACTIVE"
                                                }
                                            }
                                            """
                            )
                    }
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"error\": \"User with ID 6 does not exist\"}")
            ))
    })
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('LIBRARIAN') and @authorizationService.isUserInLibrarianLibrary(#userId)) or " +
            "hasAnyRole('USER','LIBRARIAN') and @authorizationService.isSelf(#userId) ")
    public ResponseEntity<UserInfoDTO> getUserById(
            @Parameter(description = "ID of the user to fetch", example = "6")
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(
            summary = "Get all librarians from a library.",
            description = "Returns a list of all librarians belonging to the library with the given ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of librarians returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No librarians found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Library with ID 7 does not exist\"}")
                    )
            )
    })
    @GetMapping("/library/{libraryId}/librarians")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserInfoDTO>> getLibrariansByLibraryId(
            @Parameter(description = "ID of the library", example = "3")
            @PathVariable Long libraryId
    ) {
        List<UserInfoDTO> librarians = userService.getLibrariansFromLibrary(libraryId);

        if (librarians.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(librarians);
    }

    @Operation(
            summary = "Register a user.",
            description = "Registers a user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "User registered successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Email Taken",
                                            value = "{\"error\": \"Email already taken\"}"
                                    )
                            }
                    )
            ),
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestBody @Valid UserRegistrationDTO user
    ) {
        userService.addUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(
            summary = "Update a user.",
            description = "Update the name, surname or e-mail. You can update one or more fields."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(example = "User updated successfully")
            )
            ),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"error\": \"Email already taken\"}")
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"error\": \"User with ID 6 does not exist\"}")
            ))
    })
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUser(
            @Parameter(description = "ID of the user to update", example = "6")
            @PathVariable Long userId,
            @RequestBody @Valid UserInfoDTO user
    ) {
        userService.updateUser(userId, user);
        return ResponseEntity.ok("User updated successfully");
    }

    @Operation(
            summary = "Change user's role.",
            description = "Changes the role of a user with the given ID. If changing the role to LIBRARIAN, a library ID must be provided."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "User updated successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Duplicate Role", value = "{\"error\": \"User already has this role.\"}"),
                                    @ExampleObject(name = "Missing Library Id", value = "{\"error\": \"Library Id needed.\"}"),
                                    @ExampleObject(name = "Invalid Role", value = "{\"error\": \"Invalid value 'STUDENT' for parameter. Expected type: UserRole. Allowed values: USER, LIBRARIAN, ADMIN\"}")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"User with ID 6 does not exist\"}")
                    )
            )
    })
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole( 'ADMIN')")
    public ResponseEntity<String> changeRole(
            @Parameter(description = "ID of the user to change the role", example = "6")
            @PathVariable Long userId,

            @RequestBody UserRole role,

            @Parameter(description = "Library ID to assign when role is LIBRARIAN", example = "3")
            @RequestParam(required = false) Long libraryId
    ) {
        userService.changeRole(userId, role, libraryId);
        return ResponseEntity.ok("User updated successfully");
    }

    @Operation(
            summary = "Delete a user.",
            description = "Deletes a user from the system based on the provided user Id. The user will be removed from the database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(example = "User deleted successfully")
            )
            ),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"error\": \"User with ID 6 does not exist\"}")
            ))
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "ID of the user to be deleted", example = "6")
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(
            summary = "Get users using search criteria.",
            description = "Returns a list of users that match the provided name, email, or role. You can search using any combination of these criteria."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of matching users returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No users matched the search criteria",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid value for 'role' parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid value 'READER' for parameter. Expected one of: USER, LIBRARIAN, ADMIN\"}")
                    )
            )
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<UserInfoDTO>> getUsersBySearchCriteria(
            @Parameter(description = "Filter by user's name (partial match)", example = "John")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by user's email (partial match)", example = "example@email.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "Filter by user role (exact match)", example = "LIBRARIAN")
            @RequestParam(required = false) UserRole role
    ) {
        List<UserInfoDTO> users = userService.searchUsers(name, email, role);
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get current user info.",
            description = "Returns information about the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user info retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - user not authenticated",
                    content = @Content
            )
    })
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','USER')")
    public UserInfoDTO getCurrentUserInfo() {
        User user = userService.getCurrentUser();
        return new UserInfoDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getRole().name(),
                user.getLibrary() != null ? user.getLibrary().getId() : null
        );
    }
}