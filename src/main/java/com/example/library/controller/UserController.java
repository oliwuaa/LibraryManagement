package com.example.library.controller;

import com.example.library.dto.UserDTO;
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
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
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
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID of the user to fetch", example = "6")
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // I don't know if we really need this as it can be done using search endpoint:
    // curl -X 'GET' \
    //  'http://localhost:8080/users/search?role=USER' \
    //  -H 'accept: application/json'
    @Operation(
            summary = "Get users by role.",
            description = "Returns a list of users with the specified role. Allowed values: USER, LIBRARIAN, ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users with the specified role returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No users found with the specified role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid role value",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid value 'STUDENT' for parameter 'role'. Expected type: UserRole. Allowed values: USER, LIBRARIAN, ADMIN\"}")
                    )
            )
    })
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(
            @Parameter(description = "Role of the users to retrieve", example = "USER")
            @PathVariable UserRole role
    ) {
        List<User> users = userService.getUsersByRole(role);

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(users);
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
    public ResponseEntity<List<User>> getLibrariansByLibraryId(
            @Parameter(description = "ID of the library", example = "3")
            @PathVariable Long libraryId
    ) {
        List<User> librarians = userService.getLibrariansFromLibrary(libraryId);

        if (librarians.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(librarians);
    }

    @Operation(
            summary = "Register a user.",
            description = "Registers a user. If registering a librarian, you must provide a valid library ID via query parameter `libraryId`."
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
                                    ),
                                    @ExampleObject(
                                            name = "Missing Library Id",
                                            value = "{\"error\": \"Library ID is required for librarians\"}"
                                    )
                            }
                    )
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
    @PostMapping
    public ResponseEntity<String> registerUser(
            @RequestBody UserRegistrationDTO user,
            @Parameter(description = "Library ID to assign when role is LIBRARIAN", example = "3")
            @RequestParam(required = false) Long libraryId
    ) {
        userService.addUser(user, libraryId);
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
    public ResponseEntity<String> updateUser(
            @Parameter(description = "ID of the user to update", example = "6")
            @PathVariable Long userId,
            @RequestBody UserDTO user
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
    public ResponseEntity<List<User>> getUsersBySearchCriteria(
            @Parameter(description = "Filter by user's name (partial match)", example = "John")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by user's email (partial match)", example = "example@email.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "Filter by user role (exact match)", example = "LIBRARIAN")
            @RequestParam(required = false) UserRole role
    ) {
        List<User> users = userService.searchUsers(name, email, role);
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }

}
