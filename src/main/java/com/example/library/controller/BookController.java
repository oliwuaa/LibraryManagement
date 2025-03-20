package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(books);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<Book> getBookById(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(bookService.getBookById(bookId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> getBooksBySearchCriteria(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn) {
        List<Book> books = bookService.getBooksByParams(title, author, isbn);
        if (books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(books);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<String> updateBook(
            @PathVariable Long bookId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn) {
        try {
            bookService.updateBook(bookId, title, author, isbn);
            return ResponseEntity.ok("Book updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody Book book) {
        try {
            bookService.addBook(book);
            return ResponseEntity.ok("Book added successfully");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // TO CHANGE
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
}
