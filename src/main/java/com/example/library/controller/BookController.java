package com.example.library.controller;

import com.example.library.dto.BookDTO;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Operation(summary = "Add a new book.", description = "Adds a new book to the system by providing the title, author, and ISBN. The book will be saved to the database.")

    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody BookDTO book) {
        try {
            bookService.addBook(book);
            return ResponseEntity.ok("Book added successfully");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Update a book.", description = "Update the title, author, or ISBN of the book with the given ID. You can update one or more fields.")
    @PatchMapping("/{bookId}")
    public ResponseEntity<String> updateBook(@PathVariable Long bookId, @RequestParam(required = false) String title, @RequestParam(required = false) String author, @RequestParam(required = false) String isbn) {
        try {
            bookService.updateBook(bookId, title, author, isbn);
            return ResponseEntity.ok("Book updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete a book.", description = "Deletes a book from the system based on the provided book ID. The book will be removed from the database.")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        try {
            bookService.deleteBook(bookId);
            return ResponseEntity.ok("Book deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Book not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all books.", description = "Returns a list of all books in the system.")
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Get book by Id.", description = "Returns a book with the given Id.")
    @GetMapping("/{bookId}")
    public ResponseEntity<Book> getBookById(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(bookService.getBookById(bookId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get books using search criteria.", description = "Returns a list of books that match the provided title, author, or ISBN. You can search using any combination of these criteria.")
    @GetMapping("/search")
    public ResponseEntity<List<Book>> getBooksBySearchCriteria(@RequestParam(required = false) String title, @RequestParam(required = false) String author, @RequestParam(required = false) String isbn) {
        List<Book> books = bookService.getBooksByParams(title, author, isbn);
        if (books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

}

