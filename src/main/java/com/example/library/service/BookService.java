package com.example.library.service;

import com.example.library.specification.BookSpecification;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CopyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final CopyRepository copyRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " does not exist"));
    }

    public Book getBookByISBN(String isbn) {
        return bookRepository.findByIsbn(isbn).orElseThrow(() -> new IllegalStateException("Book with ISBN" + isbn + " does not exist"));
    }

    public List<Book> getBooksByAuthor(String author) throws IllegalStateException {
        if (!bookRepository.existsByAuthor(author))
            throw new IllegalStateException("There are no books written by this author");

        return bookRepository.findByAuthor(author);
    }

    public List<Book> getBooksByParams(String title, String author, String isbn) {
        Specification<Book> specification = Specification.where(null);

        if (title != null) {
            specification = specification.and(BookSpecification.hasTitle(title));
        }
        if (author != null) {
            specification = specification.and(BookSpecification.hasAuthor(author));
        }
        if (isbn != null) {
            specification = specification.and(BookSpecification.hasIsbn(isbn));
        }

        return bookRepository.findAll(specification);
    }

    public void addBook(Book newBook) throws IllegalAccessException {
        if (bookRepository.existsByIsbn(newBook.getIsbn()))
            throw new IllegalStateException("This book has already been added");
        bookRepository.save(newBook);
    }

    public void deleteBook(Long bookId) throws IllegalStateException {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " does not exist"));
        if (copyRepository.existsByBook(book))
            throw new IllegalStateException("Cannot delete book, because there are existing copies.");
        bookRepository.delete(book);
    }

    @Transactional
    public void updateBook(Long bookId, String title, String author, String isbn) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with ID " + bookId + " does not exist"));

        if (title != null && !title.isBlank()) {
            book.setTitle(title);
        }

        if (author != null && !author.isBlank()) {
            book.setAuthor(author);
        }

        if (isbn != null && !isbn.isBlank() && !bookRepository.existsByIsbn(isbn)) {
            book.setIsbn(isbn);
        }
    }

}
