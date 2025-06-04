package com.example.library;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CopyRepository;
import com.example.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CopyRepository copyRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldReturnAllBooks() {
        List<Book> mockBooks = List.of(new Book("Title", "Author", "12345"), new Book("Title2", "Author2", "123452"), new Book("Title3", "Author3", "123453"));
        when(bookRepository.findAll()).thenReturn(mockBooks);

        List<Book> result = bookService.getAllBooks();
        assertEquals(mockBooks.size(), result.size());

        for (int i = 0; i < mockBooks.size(); i++) {
            assertEquals(mockBooks.get(i).getTitle(), result.get(i).getTitle());
            assertEquals(mockBooks.get(i).getAuthor(), result.get(i).getAuthor());
            assertEquals(mockBooks.get(i).getIsbn(), result.get(i).getIsbn());
        }

        verify(bookRepository).findAll();
    }

    @Test
    void shouldReturnBookById() {
        Book mockBook = new Book("Title", "Author", "12345");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));

        Book result = bookService.getBookById(1L);

        assertEquals("Title", result.getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBookNotExists() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getBookById(999L));
    }

    @Test
    void shouldAddBookFromExternalApi() {
        String isbn = "123456";
        String mockJson = """
    {
        "records": {
            "record1": {
                "data": {
                    "title": "Sample Book",
                    "authors": [{ "name": "John Doe" }]
                }
            }
        }
    }""";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockJson);

        bookService.addBookWithIsbn(isbn);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book saved = captor.getValue();

        assertEquals("Sample Book", saved.getTitle());
        assertEquals("John Doe", saved.getAuthor());
        assertEquals("123456", saved.getIsbn());
    }

    @Test
    void shouldReturnBooksByTitle() {
        List<Book> mockBooks = List.of(new Book("Title1", "Author1", "111"));
        when(bookRepository.findAll(any(Specification.class))).thenReturn(mockBooks);

        List<Book> result = bookService.getBooksByParams("Title1", null, null);

        assertEquals(1, result.size());
        assertEquals("Title1", result.get(0).getTitle());
        verify(bookRepository).findAll(any(Specification.class));
    }

    @Test
    void shouldReturnBooksByAllParams() {
        List<Book> mockBooks = List.of(new Book("Title3", "Author3", "333"));
        when(bookRepository.findAll(any(Specification.class))).thenReturn(mockBooks);

        List<Book> result = bookService.getBooksByParams("Title3", "Author3", "333");

        assertEquals(1, result.size());
        assertEquals("333", result.get(0).getIsbn());
        verify(bookRepository).findAll(any(Specification.class));
    }


    @Test
    void shouldReturnAllBooksWhenNoParams() {
        List<Book> mockBooks = List.of(
                new Book("TitleA", "AuthorA", "AAA"),
                new Book("TitleB", "AuthorB", "BBB")
        );
        when(bookRepository.findAll(any(Specification.class))).thenReturn(mockBooks);


        List<Book> result = bookService.getBooksByParams(null, null, null);

        assertEquals(2, result.size());
        verify(bookRepository).findAll(any(Specification.class));
    }

    @Test
    void shouldReturnBooksByTitleAndAuthor() {
        List<Book> mockBooks = List.of(new Book("Title2", "Author2", "222"));
        when(bookRepository.findAll(any(Specification.class))).thenReturn(mockBooks);

        List<Book> result = bookService.getBooksByParams("Title2", "Author2", null);

        assertEquals(1, result.size());
        assertEquals("Author2", result.get(0).getAuthor());
        verify(bookRepository).findAll(any(Specification.class));
    }



    @Test
    void shouldDeleteBookWhenNoCopiesExist() {
        Book book = new Book("Test", "Author", "123");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(copyRepository.existsByBook(book)).thenReturn(false);

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void shouldThrowBadRequestWhenBookHasCopies() {
        Book book = new Book("Test", "Author", "123");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(copyRepository.existsByBook(book)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> bookService.deleteBook(1L));
    }

}
