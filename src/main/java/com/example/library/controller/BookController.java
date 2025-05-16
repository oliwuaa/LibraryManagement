package com.example.library.controller;

import com.example.library.exception.NotFoundException;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Operation(summary = "Add a new book using API.", description = "Adds a new book to the system by providing the ISBN. The book will be saved to the database.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book added successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Book added successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A book with this ISBN has already been added.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"A book with this ISBN already exists in the database\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Book with this ISBN does not exist\"}")
                    )
            )
    })
    @PostMapping("/{isbn}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<String> addBook(
            @Parameter(description = "ISBN of the book", example = "9781984896391")
            @PathVariable String isbn) {
        try {
            bookService.addBookWithIsbn(isbn);

            return ResponseEntity.ok("Book added successfully");

        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body("{\"error\": \"A book with this ISBN already exists in the database\"}");

        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body("{\"error\": \"Book with this ISBN does not exist\"}");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}");
        }
    }


    @Operation(
            summary = "Delete a book.",
            description = "Deletes a book from the system based on the provided book ID. " +
                    "If the book has existing copies, the operation fails with a 400 error."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Book deleted successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Book cannot be deleted due to existing copies",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Cannot delete book, because there are existing copies.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Book with ID 12 does not exist\"}")
                    )
            )
    })
    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<String> deleteBook(
            @Parameter(description = "ID of the book to delete", example = "1")
            @PathVariable Long bookId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated User: " + authentication.getName());
        System.out.println("User Roles: " + authentication.getAuthorities());
        bookService.deleteBook(bookId);
        return ResponseEntity.ok("Book deleted successfully");
    }

    @Operation(
            summary = "Get all books.",
            description = "Returns a list of all books stored in the system. If there are no books, the response returns 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of books returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Book.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No books found",
                    content = @Content()
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

    @Operation(
            summary = "Get book by Id.",
            description = "Returns a book with the given ID if it exists. If the book is not found, a 404 error is returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Book.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Book with ID 99 does not exist\"}")
                    )
            )
    })
    @GetMapping("/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Book> getBookById(
            @Parameter(description = "ID of the book to retrieve", example = "5")
            @PathVariable Long bookId
    ) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    @Operation(
            summary = "Get books using search criteria.",
            description = "Returns a list of books that match the provided title, author, or ISBN. " +
                    "You can search using any combination of these criteria. If no books match, a 204 No Content is returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Books found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Book.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No books found matching the criteria",
                    content = @Content()
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<Book>> getBooksBySearchCriteria(
            @Parameter(description = "Title to search for", example = "The Great Gatsby")
            @RequestParam(required = false) String title,
            @Parameter(description = "Author to search for", example = "F. Scott Fitzgerald")
            @RequestParam(required = false) String author,
            @Parameter(description = "ISBN to search for", example = "9780743273565")
            @RequestParam(required = false) String isbn
    ) {

        List<Book> books = bookService.getBooksByParams(title, author, isbn);
        if (books.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(books);
    }
}