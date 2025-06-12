package com.example.library.service;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.specification.BookSpecification;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CopyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final CopyRepository copyRepository;
    private final RestTemplate restTemplate;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new NotFoundException("Book with ID " + bookId + " does not exist"));
    }

    public void addBookWithIsbn(String isbn) {
        String url = "http://openlibrary.org/api/volumes/brief/isbn/" + isbn + ".json";
        String jsonResponse = restTemplate.getForObject(url, String.class);
        Book book = mapToBook(jsonResponse, isbn);
        bookRepository.save(book);
    }

    private Book mapToBook(String jsonResponse, String isbn) {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            JsonNode bookData = rootNode.path("records").elements().next();
            JsonNode bookInfo = bookData.path("data");

            String title = bookInfo.path("title").asText();
            String author = bookInfo.path("authors").get(0).path("name").asText();

            return new Book(title, author, isbn);

        } catch (Exception e) {
            throw new RuntimeException("Couldn't get the response.", e);
        }
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

    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book with ID " + bookId + " does not exist"));

        if (copyRepository.existsByBook(book)) {
            throw new BadRequestException("Cannot delete book, because there are existing copies.");
        }

        bookRepository.delete(book);
    }
}