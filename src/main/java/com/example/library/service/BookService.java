package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CopyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService {
    @Autowired
    private final BookRepository bookRepository;
    @Autowired
    private final CopyRepository copyRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " does not exist"));
    }

    public Book getBookByISBN(String isbn) {
        return bookRepository.findBookByIsbn(isbn).orElseThrow(() -> new IllegalStateException("Book with ISBN" + isbn + " does not exist"));
    }

    public void addBook(Book newBook) throws IllegalAccessException {
        if (bookRepository.existsBookByIsbn(newBook.getIsbn()))
            throw new IllegalAccessException("This book has already been added");
        bookRepository.save(newBook);
    }

    public void deleteBook(Book book) throws IllegalAccessException {
        if (!bookRepository.existsBookByIsbn(book.getIsbn()))
            throw new IllegalAccessException("Book with this ISBN hasn't been found.");
        if(copyRepository.existsByBook(book))
            throw new IllegalAccessException("Cannot delete book because there are existing copies.");
        bookRepository.delete(book);
    }

    @Transactional
    public void updateBook(Long bookId, String title, String author, String isbn) throws IllegalAccessException {
        Book changedBook = bookRepository.findById(bookId).orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " does not exist"));

        if (title != null && !title.isBlank() && !Objects.equals(changedBook.getTitle(), title)) {
            changedBook.setTitle(title);
        }

        if (author != null && !author.isBlank() && !Objects.equals(changedBook.getAuthor(), author)) {
            changedBook.setAuthor(author);
        }

        if (isbn != null && !isbn.isBlank() && !Objects.equals(changedBook.getIsbn(), isbn)) {
            if (bookRepository.existsBookByIsbn(isbn))
                throw new IllegalAccessException("Book with this ISBN has already exist!");
            changedBook.setIsbn(isbn);
        }

        bookRepository.save(changedBook);

    }

}
