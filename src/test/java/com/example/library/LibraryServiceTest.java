package com.example.library;

import com.example.library.dto.LibraryDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.repository.*;
import com.example.library.service.LibraryService;
import com.example.library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibraryServiceTest {

    @Mock
    private LibraryRepository libraryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CopyRepository copyRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserService userService;
    @Mock
    private Clock clock;

    @InjectMocks
    private LibraryService libraryService;

    private final LocalDate now = LocalDate.of(2024, 5, 10);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(Instant.parse("2024-05-10T00:00:00Z"), ZoneId.of("UTC"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    void shouldGetAllLibraries() {
        when(libraryRepository.findAll()).thenReturn(List.of(new Library()));
        List<Library> result = libraryService.getAllLibraries();
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnLibraryById_whenExists() {
        Library library = new Library();
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));
        assertEquals(library, libraryService.getLibraryById(1L));
    }

    @Test
    void shouldThrow_whenLibraryNotFoundById() {
        assertThrows(NotFoundException.class, () -> libraryService.getLibraryById(1L));
    }

    @Test
    void shouldReturnLibraryForCurrentLibrarian() {
        Library library = new Library();
        User user = new User();
        user.setRole(UserRole.LIBRARIAN);
        user.setLibrary(library);
        when(userService.getCurrentUser()).thenReturn(user);

        assertEquals(library, libraryService.getLibraryForCurrentLibrarian());
    }

    @Test
    void shouldThrowIfNotLibrarian() {
        User user = new User();
        user.setRole(UserRole.USER);
        when(userService.getCurrentUser()).thenReturn(user);
        assertThrows(BadRequestException.class, () -> libraryService.getLibraryForCurrentLibrarian());
    }

    @Test
    void shouldThrowIfLibrarianHasNoLibrary() {
        User user = new User();
        user.setRole(UserRole.LIBRARIAN);
        when(userService.getCurrentUser()).thenReturn(user);
        assertThrows(NotFoundException.class, () -> libraryService.getLibraryForCurrentLibrarian());
    }

    @Test
    void shouldAddLibrary() {
        LibraryDTO dto = new LibraryDTO("Central", "Main St 1");
        when(libraryRepository.findByAddress("Main St 1")).thenReturn(Optional.empty());
        libraryService.addLibrary(dto);
        verify(libraryRepository).save(any(Library.class));
    }

    @Test
    void shouldThrowIfLibraryAddressExists() {
        LibraryDTO dto = new LibraryDTO("Central", "Main St 1");
        when(libraryRepository.findByAddress("Main St 1")).thenReturn(Optional.of(new Library()));
        assertThrows(BadRequestException.class, () -> libraryService.addLibrary(dto));
    }

    @Test
    void shouldEndLoanIfExists() {
        Loan loan = new Loan();
        when(loanRepository.findLoanByCopy_Id(1L)).thenReturn(Optional.of(loan));
        libraryService.endLoan(1L);
        verify(loanRepository).save(loan);
        assertEquals(now, loan.getReturnDate());
    }

    @Test
    void shouldEndReservationIfExists() {
        Reservation reservation = new Reservation();
        when(reservationRepository.findReservationByCopy_Id(1L)).thenReturn(Optional.of(reservation));
        libraryService.endReservation(1L);
        verify(reservationRepository).save(reservation);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void shouldDeleteLibraryCompletely() {
        Library lib = new Library();
        lib.setId(1L);
        Copy copy = new Copy(); copy.setId(1L);

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));
        when(copyRepository.findByLibraryId(1L)).thenReturn(List.of(copy));
        when(loanRepository.existsLoanByCopy_Id(1L)).thenReturn(false);
        when(reservationRepository.existsReservationByCopy_Id(1L)).thenReturn(false);

        boolean result = libraryService.deleteLibrary(1L);

        assertTrue(result);
        verify(copyRepository).delete(copy);
        verify(libraryRepository).delete(lib);
    }

    @Test
    void shouldCloseLibraryWhenActiveDataExists() {
        Library lib = new Library();
        lib.setId(1L);
        Copy copy = new Copy(); copy.setId(1L);

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));
        when(copyRepository.findByLibraryId(1L)).thenReturn(List.of(copy));
        when(loanRepository.existsLoanByCopy_Id(1L)).thenReturn(true);
        when(reservationRepository.existsReservationByCopy_Id(1L)).thenReturn(false);

        boolean result = libraryService.deleteLibrary(1L);

        assertFalse(result);
        verify(copyRepository).save(copy);
        verify(libraryRepository).save(lib);
        assertEquals(CopyStatus.REMOVED, copy.getStatus());
        assertEquals(LibraryStatus.CLOSED, lib.getStatus());
    }

    @Test
    void shouldUpdateLibraryName() {
        Library lib = new Library();
        lib.setName("Old");
        lib.setAddress("Street");
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));

        libraryService.updateLibrary(1L, new LibraryDTO("New", null));

        assertEquals("New", lib.getName());
        verify(libraryRepository).save(lib);
    }

    @Test
    void shouldUpdateLibraryAddress() {
        Library lib = new Library();
        lib.setName("Central");
        lib.setAddress("Old Street");
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));

        libraryService.updateLibrary(1L, new LibraryDTO(null, "New Street"));

        assertEquals("New Street", lib.getAddress());
        verify(libraryRepository).save(lib);
    }

    @Test
    void shouldThrowWhenUpdateLibraryWithNoFields() {
        assertThrows(BadRequestException.class, () -> libraryService.updateLibrary(1L, new LibraryDTO(" ", " ")));
    }
}
