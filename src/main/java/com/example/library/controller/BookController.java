package com.example.library.controller;

import com.example.library.dto.BookDTO;
import com.example.library.model.Book;
import com.example.library.service.BookService;
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
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Operation(
            summary = "Add a new book.",
            description = "Adds a new book to the system by providing the title, author, and ISBN. The book will be saved to the database."
    )
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
                    description = "Bad request – missing required fields or book already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Empty Title", value = "{\"error\": \"Title cannot be empty\"}"),
                                    @ExampleObject(name = "Empty Author", value = "{\"error\": \"Author cannot be empty\"}"),
                                    @ExampleObject(name = "Empty ISBN", value = "{\"error\": \"ISBN cannot be empty\"}"),
                                    @ExampleObject(name = "Duplicate ISBN", value = "{\"error\": \"This book has already been added\"}")
                            }
                    )
            )
    })
    @PostMapping
    public ResponseEntity<String> addBook(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Book details including title, author, and ISBN",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookDTO.class),
                            examples = @ExampleObject(
                                    name = "New Book",
                                    value = """
                                        {
                                          "title": "The Hobbit",
                                          "author": "J.R.R. Tolkien",
                                          "isbn": "9780261102217"
                                        }
                                        """
                            )
                    )
            )
            @RequestBody BookDTO book
    ) {
        bookService.addBook(book);
        return ResponseEntity.ok("Book added successfully");
    }


    @Operation(
            summary = "Update a book.",
            description = "Updates the title, author, or ISBN of the book with the given ID. " +
                    "You can update one or more fields. If ISBN already exists for another book, update is rejected. " +
                    "At least one field must be provided."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Book updated successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or ISBN already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "ISBN Already Exists", value = "{\"error\": \"This ISBN already exists\"}"),
                                    @ExampleObject(name = "No Fields Provided", value = "{\"error\": \"At least one field (title, author, or ISBN) must be provided\"}"),
                                    @ExampleObject(name = "No Changes", value = "{\"error\": \"No changes detected. Provided data is identical to existing.\"}")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Book with ID 3 does not exist\"}")
                    )
            )
    })
    @PatchMapping("/{bookId}")
    public ResponseEntity<String> updateBook(
            @Parameter(description = "ID of the book to update", example = "3")
            @PathVariable Long bookId,
            @Parameter(description = "New title of the book", example = "The Lord of the Rings")
            @RequestParam(required = false) String title,
            @Parameter(description = "New author of the book", example = "J.R.R. Tolkien")
            @RequestParam(required = false) String author,
            @Parameter(description = "New ISBN of the book", example = "9780261102385")
            @RequestParam(required = false) String isbn
    ) {
        bookService.updateBook(bookId, title, author, isbn);
        return ResponseEntity.ok("Book updated successfully");
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
    public ResponseEntity<String> deleteBook(
            @Parameter(description = "ID of the book to delete", example = "1")
            @PathVariable Long bookId
    ) {
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
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

}

