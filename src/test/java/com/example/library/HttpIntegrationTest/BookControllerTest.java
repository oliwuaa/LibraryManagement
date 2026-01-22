package com.example.library.HttpIntegrationTest;

import com.example.library.component.JwtAuthenticationFilter;
import com.example.library.controller.BookController;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import com.example.library.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;


    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    /* ---------- GET ---------- */

    @Test
    void shouldReturnOkWhenBookExists() throws Exception {
        Book mockBook = new Book("Title", "Author", "12345");
        when(bookService.getBookById(1L)).thenReturn(mockBook);

        mockMvc.perform(get("/books/{bookId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenBookDoesNotExist() throws Exception {
        when(bookService.getBookById(1L))
                .thenThrow(new NotFoundException("Book with ID 1 does not exist"));

        mockMvc.perform(get("/books/{bookId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book with ID 1 does not exist"));
    }

    @Test
    void shouldReturnListOfBooks() throws Exception {
        List<Book> books = List.of(
                new Book(1L, "Title1", "Author1", "ISBN1"),
                new Book(2L, "Title2", "Author2", "ISBN2"));

        when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Title1"))
                .andExpect(jsonPath("$[1].title").value("Title2"));
    }

    @Test
    void shouldReturnNoContentWhenNoBooks() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnEmptyListWhenNoBooksFound() throws Exception {
        when(bookService.getBooksByParams(null, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books/search"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnBooksMatchingCriteria() throws Exception {
        Book mockBook = new Book(1L,
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "9780743273565");

        when(bookService.getBooksByParams("The Great Gatsby", null, null))
                .thenReturn(List.of(mockBook));

        mockMvc.perform(get("/books/search").param("title", "The Great Gatsby"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Great Gatsby"))
                .andExpect(jsonPath("$[0].author").value("F. Scott Fitzgerald"))
                .andExpect(jsonPath("$[0].isbn").value("9780743273565"));
    }

    /* ---------- POST ---------- */

    @Test
    void shouldAddBookSuccessfully() throws Exception {
        mockMvc.perform(post("/books/9781984896391"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book added successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenAddingAlreadyExistingBook() throws Exception {
        doThrow(new IllegalStateException("Book already exists"))
                .when(bookService).addBookWithIsbn("9781984896391");

        mockMvc.perform(post("/books/9781984896391"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        "{\"error\": \"A book with this ISBN already exists in the database\"}"));
    }

    @Test
    void shouldReturnNotFoundWhenAddingNonExistingBook() throws Exception {
        doThrow(new NotFoundException("Book not found"))
                .when(bookService).addBookWithIsbn("9781984896391");

        mockMvc.perform(post("/books/9781984896391"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(
                        "{\"error\": \"Book with this ISBN does not exist\"}"));
    }

    /* ---------- DELETE ---------- */

    @Test
    void shouldDeleteBookSuccessfully() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book deleted successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenDeletingAlreadyUsedBook() throws Exception {
        doThrow(new BadRequestException("Book cannot be deleted due to existing copies."))
                .when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        "{\"error\": \"Book cannot be deleted due to existing copies.\"}"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingBook() throws Exception {
        doThrow(new NotFoundException("Book not found"))
                .when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\": \"Book not found\"}"));
    }
}
