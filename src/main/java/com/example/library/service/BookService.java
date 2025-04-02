package com.example.library.service;

import com.example.library.dto.BookDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
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
        return bookRepository.findById(bookId).orElseThrow(() -> new NotFoundException("Book with ID " + bookId + " does not exist"));
    }

    public Book getBookByISBN(String isbn) {
        return bookRepository.findByIsbn(isbn).orElseThrow(() -> new NotFoundException("Book with ISBN" + isbn + " does not exist"));
    }

    public List<Book> getBooksByAuthor(String author) {
        if (!bookRepository.existsByAuthor(author))
            throw new NotFoundException("There are no books written by this author");

        return bookRepository.findByAuthor(author);
    }

    public List<Book> getBooksByParams(String title, String author, String isbn) {
        Specification<Book> specification = Specification.where(null);

        if (title != null && !title.isBlank()) {
            specification = specification.and(BookSpecification.hasTitle(title));
        }
        if (author != null && !author.isBlank()) {
            specification = specification.and(BookSpecification.hasAuthor(author));
        }
        if (isbn != null && !isbn.isBlank()) {
            specification = specification.and(BookSpecification.hasIsbn(isbn));
        }

        return bookRepository.findAll(specification);
    }

    public void addBook(BookDTO book) {
        if (book.title() == null || book.title().isBlank()) {
            throw new BadRequestException("Title cannot be empty");
        }
        if (book.author() == null || book.author().isBlank()) {
            throw new BadRequestException("Author cannot be empty");
        }
        if (book.isbn() == null || book.isbn().isBlank()) {
            throw new BadRequestException("ISBN cannot be empty");
        }

        if (bookRepository.existsByIsbn(book.isbn())) {
            throw new BadRequestException("This book has already been added");
        }

        Book newBook = new Book();
        newBook.setTitle(book.title());
        newBook.setAuthor(book.author());
        newBook.setIsbn(book.isbn());
        bookRepository.save(newBook);
    }

    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book with ID " + bookId + " does not exist"));

        if (copyRepository.existsByBook(book)) {
            throw new BadRequestException("Cannot delete book, because there are existing copies.");
        }

        bookRepository.delete(book);
    }

    @Transactional
    public void updateBook(Long bookId, String title, String author, String isbn) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book with ID " + bookId + " does not exist"));

        boolean modified = false;

        if (title != null && !title.isBlank() && !title.equals(book.getTitle())) {
            book.setTitle(title);
            modified = true;
        }

        if (author != null && !author.isBlank() && !author.equals(book.getAuthor())) {
            book.setAuthor(author);
            modified = true;
        }

        if (isbn != null && !isbn.isBlank()) {
            if (!isbn.equals(book.getIsbn())) {
                if (bookRepository.existsByIsbn(isbn)) {
                    throw new BadRequestException("This ISBN already exists");
                }
                book.setIsbn(isbn);
                modified = true;
            }
        }

        if (!modified) {
            throw new BadRequestException("No changes detected. Provided data is identical to existing.");
        }

        bookRepository.save(book);
    }


}
