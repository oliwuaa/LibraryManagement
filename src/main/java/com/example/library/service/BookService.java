package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Objects;

@Service
public class BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long bookId)
    {
        return bookRepository.findById(bookId).orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " does not exist"));
    }

    public Book getBookByISBN(String isbn)
    {
        return bookRepository.findBookByIsbn(isbn).orElseThrow(() -> new IllegalStateException("Book with ISBN" + isbn + " does not exist"));
    }

    public void addBook(@RequestBody Book newBook) throws IllegalAccessException {
        if (!bookRepository.existsBookByIsbn(newBook.getIsbn()))
            throw new IllegalAccessException("This book has already been added");
        bookRepository.save(newBook);
    }

    public void deleteBook(@RequestBody Book newBook) throws IllegalAccessException {
        if (bookRepository.existsBookByIsbn(newBook.getIsbn()))
            throw new IllegalAccessException("Book with this ISBN hasn't been found.");
        bookRepository.delete(newBook);
    }

    @Transactional
    public void updateBook(Long bookId, String title, String author, String isbn) throws IllegalAccessException {
        Book changedBook = bookRepository.findById(bookId).orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " does not exist"));

        if (title != null && title.length() > 0 && !Objects.equals(changedBook.getTitle(), title)) {
            changedBook.setTitle(title);
        }

        if (author != null && author.length() > 0 && !Objects.equals(changedBook.getAuthor(), author)) {
            changedBook.setAuthor(author);
        }

        if (isbn != null && isbn.length() > 0 && !Objects.equals(changedBook.getIsbn(), isbn)) {
            if (bookRepository.existsBookByIsbn(changedBook.getIsbn()))
                throw new IllegalAccessException("Book with this ISBN has already exist!");
            changedBook.setIsbn(isbn);
        }

    }


}
