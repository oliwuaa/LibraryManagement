package com.example.library;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.repository.*;
import com.example.library.service.CopyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CopyServiceTest {

    @Mock
    private CopyRepository copyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LibraryRepository libraryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private CopyService copyService;

    @Test
    void shouldReturnAllCopies() {
        Book book = new Book(1L, "Title", "Author", "12345");
        Library library = new Library(1L, "Library", "Długa 23", LibraryStatus.ACTIVE);

        Copy copy1 = Copy.builder()
                .id(1L)
                .book(book)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();

        Copy copy2 = Copy.builder()
                .id(2L)
                .book(book)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();

        List<Copy> copies = List.of(copy1, copy2);

        when(copyRepository.findAll()).thenReturn(copies);
        List<Copy> result = copyService.getAllCopies();
        assertEquals(2, result.size());
        assertEquals(copy1, result.get(0));
        assertEquals(copy2, result.get(1));

        verify(copyRepository).findAll();
    }

    @Test
    void shouldReturnCopyByID() {
        Book book = new Book(1L, "Title", "Author", "12345");
        Library library = new Library(1L, "Library", "Długa 23", LibraryStatus.ACTIVE);

        Copy copy = Copy.builder()
                .id(1L)
                .book(book)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();

        when(copyRepository.findById(1L)).thenReturn(Optional.of(copy));
        Copy result = copyService.getCopyById(1L);
        assertEquals(copy, result);

        verify(copyRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCopyNotExists() {
        when(copyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> copyService.getCopyById(999L));
    }

    @Test
    void shouldReturnCopiesFromLibrary() {
        Book book1 = new Book("Title", "Author", "12345");
        Book book2 = new Book("Title2", "Author2", "123452");
        Library library = new Library(1L, "Library", "Długa 23", LibraryStatus.ACTIVE);

        Copy copy1 = Copy.builder()
                .id(1L)
                .book(book1)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();

        Copy copy2 = Copy.builder()
                .id(2L)
                .book(book2)
                .library(library)
                .status(CopyStatus.BORROWED)
                .build();

        List<Copy> copies = List.of(copy1, copy2);

        when(libraryRepository.existsById(1L)).thenReturn(Boolean.TRUE);
        when(copyRepository.findByLibraryId(1L)).thenReturn(copies);

        List<Copy> result = copyService.getCopiesByLibrary(1L);

        for (int i = 0; i < copies.size(); i++) {
            assertEquals(copies.get(i), result.get(i));
        }
        verify(libraryRepository).existsById(1L);
        verify(copyRepository).findByLibraryId(1L);
    }

    @Test
    void shouldReturnNotFoundExceptionLibrary() {
        when(libraryRepository.existsById(1L)).thenReturn(Boolean.FALSE);
        assertThrows(NotFoundException.class, () -> copyService.getCopiesByLibrary(1L));
        verify(libraryRepository).existsById(1L);
        verifyNoInteractions(copyRepository);
    }

    @Test
    void shouldReturnEmptyListWhenLibraryExistsButNoCopies() {
        Long libraryId = 1L;

        when(libraryRepository.existsById(libraryId)).thenReturn(true);
        when(copyRepository.findByLibraryId(libraryId)).thenReturn(Collections.emptyList());

        List<Copy> result = copyService.getCopiesByLibrary(libraryId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(libraryRepository).existsById(libraryId);
        verify(copyRepository).findByLibraryId(libraryId);
    }

    @Test
    void shouldReturnAvailableCopiesFromLibrary() {
        Book book1 = new Book("Title", "Author", "12345");
        Book book2 = new Book("Title2", "Author2", "123452");
        Library library = new Library(1L, "Library", "Długa 23", LibraryStatus.ACTIVE);

        Copy copy1 = Copy.builder()
                .id(1L)
                .book(book1)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();

        Copy copy2 = Copy.builder()
                .id(2L)
                .book(book2)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();

        List<Copy> copies = List.of(copy1, copy2);

        when(copyRepository.findByLibraryIdAndStatus(1L,CopyStatus.AVAILABLE)).thenReturn(copies);

        List<Copy> result = copyService.getAvailableCopiesByLibrary(1L);

        for (int i = 0; i < copies.size(); i++) {
            assertEquals(copies.get(i), result.get(i));
        }
        verify(copyRepository).findByLibraryIdAndStatus(1L,CopyStatus.AVAILABLE);
    }

    @Test
    void shouldReturnCopiesByBookWhenBookExists() {
        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        List<Copy> copies = List.of(new Copy());
        when(copyRepository.findByBookId(bookId)).thenReturn(copies);

        List<Copy> result = copyService.getCopiesByBook(bookId);

        assertEquals(1, result.size());
        verify(bookRepository).existsById(bookId);
        verify(copyRepository).findByBookId(bookId);
    }

    @Test
    void shouldThrowWhenBookDoesNotExist() {
        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> copyService.getCopiesByBook(bookId));
        verify(bookRepository).existsById(bookId);
        verifyNoInteractions(copyRepository);
    }

    @Test
    void shouldReturnAvailableCopiesByBook() {
        Long bookId = 1L;
        List<Copy> copies = List.of(new Copy());
        when(copyRepository.findByBookIdAndStatus(bookId, CopyStatus.AVAILABLE)).thenReturn(copies);

        List<Copy> result = copyService.getAvailableCopiesByBook(bookId);

        assertEquals(1, result.size());
        verify(copyRepository).findByBookIdAndStatus(bookId, CopyStatus.AVAILABLE);
    }

    @Test
    void shouldReturnAvailableCopiesByBookAndLibrary() {
        Long bookId = 1L;
        Long libraryId = 2L;
        List<Copy> copies = List.of(new Copy());
        when(copyRepository.findByBookIdAndLibraryId(bookId, libraryId)).thenReturn(copies);

        List<Copy> result = copyService.getAvailableCopiesOfBookByLibrary(bookId, libraryId);

        assertEquals(1, result.size());
        verify(copyRepository).findByBookIdAndLibraryId(bookId, libraryId);
    }

    @Test
    void shouldAddCopySuccessfully() {
        Long bookId = 1L;
        Long libraryId = 2L;

        Book book = new Book();
        Library library = new Library();
        User user = new User();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        copyService.addCopy(bookId, libraryId);

        verify(copyRepository).save(any(Copy.class));
    }

    @Test
    void shouldUpdateCopyStatus() {
        Long copyId = 1L;
        Copy copy = new Copy();
        copy.setStatus(CopyStatus.BORROWED);

        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));

        copyService.updateCopyStatus(copyId, CopyStatus.AVAILABLE);

        assertEquals(CopyStatus.AVAILABLE, copy.getStatus());
        verify(copyRepository).save(copy);
    }

    @Test
    void shouldThrowWhenStatusIsSame() {
        Long copyId = 1L;
        Copy copy = new Copy();
        copy.setStatus(CopyStatus.AVAILABLE);

        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));

        assertThrows(BadRequestException.class, () -> copyService.updateCopyStatus(copyId, CopyStatus.AVAILABLE));
    }

    @Test
    void shouldDeleteCopyWhenNoLoanOrReservation() {
        Long copyId = 1L;
        Copy copy = new Copy();
        copy.setStatus(CopyStatus.AVAILABLE);

        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));
        when(loanRepository.existsLoanByCopy_Id(copyId)).thenReturn(false);
        when(reservationRepository.existsReservationByCopy_Id(copyId)).thenReturn(false);

        copyService.deleteCopy(copyId);

        verify(copyRepository).delete(copy);
    }

    @Test
    void shouldSoftDeleteCopyWhenLoanOrReservationExists() {
        Long copyId = 1L;
        Copy copy = new Copy();
        copy.setStatus(CopyStatus.AVAILABLE);

        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));
        when(loanRepository.existsLoanByCopy_Id(copyId)).thenReturn(true);

        copyService.deleteCopy(copyId);

        assertEquals(CopyStatus.REMOVED, copy.getStatus());
        verify(copyRepository).save(copy);
    }

    @Test
    void shouldThrowWhenCopyIsNotAvailable() {
        Long copyId = 1L;
        Copy copy = new Copy();
        copy.setStatus(CopyStatus.BORROWED);

        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));

        assertThrows(BadRequestException.class, () -> copyService.deleteCopy(copyId));
    }
}